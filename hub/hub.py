"""Hub local de LuminaWear.

Modo "simulated" (por defecto): solo librería estándar, guarda el estado en memoria.
Modo "cozylife": controla el bombillo CozyLife real por la red local (librería estándar,
IP en hub/config.json). GET lee el estado real del bombillo; un keepalive lo consulta cada 8 s
para la página.
Modo "tuya": controla un bombillo real con tinytuya leyendo hub/config.json. SIN PROBAR.

Uso:
    python hub/hub.py                 # simulado, puerto 8787
    python hub/hub.py --mode cozylife # bombillo CozyLife (requiere config.json)
    python hub/hub.py --mode tuya     # bombillo Tuya (requiere tinytuya y config.json)
    python hub/hub.py --host 0.0.0.0  # escucha en la red (reloj físico); por defecto solo 127.0.0.1
"""

import argparse
import json
import re
import sys
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

PORT = 8787
# Solo este PC por defecto: el emulador llega por 10.0.2.2, que es el 127.0.0.1 del PC.
HOST = "127.0.0.1"
# Única luz conectada: Lámpara de Sala (mismo id que en la app).
LAMP_ID = "living_lampara"
CONFIG_PATH = Path(__file__).with_name("config.json")
LIGHT_PATH = re.compile(r"^/api/lights/([^/]+)$")
# Keepalive del modo cozylife: cada cuánto consulta y cuántos fallos seguidos cuentan como pérdida.
KEEPALIVE_S = 8
KEEPALIVE_FAILURES = 2
KEEPALIVE_READ_S = 2.5  # corto: una orden puede tener que esperar a que termine
# Plazos de una orden en modo cozylife. La Wi-Fi del bombillo tiene cortes de varios segundos y a
# veces aplica la orden sin que llegue la confirmación. ORDER_DEADLINE_S es el tope de un POST;
# la app espera un poco más (HubProvider.TIMEOUT_MS = 10 s).
ORDER_DEADLINE_S = 8.0
ORDER_SEND_S = 3.5  # espera de la confirmación de cada envío
VERIFY_READS = 2  # lecturas para comprobar una orden sin confirmación
VERIFY_READ_S = 2.0
VERIFY_PAUSE_S = 0.5


def log(message):
    print(f"[hub {time.strftime('%H:%M:%S')}] {message}", flush=True)


def describe(on, brightness):
    return f"encendida {brightness} %" if on else "apagada"


class SimulatedBackend:
    """Bombillo en memoria. Estado inicial igual al de la app: encendida al 45 %."""

    mode = "simulated"

    def __init__(self):
        self.on = True
        self.brightness = 45

    def read(self):
        return self.on, self.brightness

    def apply(self, on, brightness):
        """Aplica la orden y devuelve el estado con el que responde el POST."""
        self.on = on
        self.brightness = brightness
        return self.read()

    def status(self):
        """Estado para la página. last_seen_s es None: aquí no hay bombillo que pueda fallar."""
        on, brightness = self.read()
        return {"known": True, "on": on, "brightness": brightness, "responding": True, "last_seen_s": None}

    def close(self):
        pass


class CozyLifeBackend:
    """Bombillo CozyLife en la red local (ver hub/cozylife.py).

    GET consulta el bombillo (plazo del cliente, 5 s). POST sigue apply() en ORDER_DEADLINE_S
    como máximo. Si falla, el hub responde 502. Un hilo de fondo (keepalive) consulta el estado
    cada KEEPALIVE_S sin enviar órdenes y guarda el último estado conocido para la página. En
    consola se registran el resultado de cada orden, los cambios de estado y la pérdida o
    recuperación de la conexión.
    """

    mode = "cozylife"

    def __init__(self):
        from cozylife import CozyLifeClient, load_config, to_percent, to_raw

        ip, port = load_config()
        self.client = CozyLifeClient(ip, port)
        self._reported_percent = lambda brightness: to_percent(to_raw(brightness))
        self._lock = threading.Lock()
        self._last = None  # (on, brightness) del último contacto correcto
        self._last_at = None  # time.monotonic() de ese contacto
        self._failures = 0  # fallos seguidos
        self._stop = threading.Event()
        self._keepalive = threading.Thread(target=self._keepalive_loop, name="cozylife-keepalive", daemon=True)
        self._keepalive.start()

    def read(self, timeout=None):
        try:
            state = self.client.read_state(timeout)
        except Exception as e:
            self._record_failure(e)
            raise
        self._record_success(state)
        return state

    def apply(self, on, brightness):
        """Envía la orden y devuelve el estado para el POST; lanza CozyLifeError si no se logró.

        1. Envío con confirmación → se verifica leyendo (si la lectura falla, se responde lo pedido).
        2. Sin confirmación → NO se reenvía enseguida: se lee el estado. Si ya está lo pedido, listo.
        3. Si responde con otra cosa → se reenvía UNA vez (valores absolutos) y se vuelve a verificar.
        4. Sin respuesta o sigue distinto → error (502).
        """
        from cozylife import CozyLifeError

        deadline = time.monotonic() + ORDER_DEADLINE_S

        def left(cap):
            return min(cap, deadline - time.monotonic())

        # Lo que el bombillo debería reportar (encender con 0 % queda en 1 %; apagar no cambia el brillo).
        expected = (on, self._reported_percent(brightness) if on else brightness)
        order = describe(*expected)

        if self._send(on, brightness, left(ORDER_SEND_S)):
            try:
                state = self.read(left(self.client.timeout))
            except CozyLifeError as e:
                log(f"orden confirmada: {order} (la verificación no respondió: {e}; se responde con el estado pedido)")
                return expected
            log(f"orden confirmada: {order}")
            return state

        state = self._verify(on, brightness, left)
        if state is not None and self._matches(state, on, brightness):
            log(f"orden verificada sin confirmación: {order}")
            return state
        if state is None:
            log(f"orden fallida: {order} (sin confirmación y el bombillo no responde)")
            raise CozyLifeError("sin confirmación y el bombillo no responde")

        log(f"orden sin confirmación y el bombillo sigue {describe(*state)}: se reenvía una vez")
        confirmed = self._send(on, brightness, left(ORDER_SEND_S))
        state = self._verify(on, brightness, left)
        if state is not None and self._matches(state, on, brightness):
            log(f"orden reenviada: {order} ({'confirmada' if confirmed else 'verificada sin confirmación'})")
            return state
        if confirmed and state is None:
            log(f"orden reenviada: {order} (confirmada; la verificación no respondió, se responde con el estado pedido)")
            return expected
        detail = "el bombillo no responde" if state is None else f"el bombillo sigue {describe(*state)}"
        log(f"orden fallida: {order} (tras reenviar, {detail})")
        raise CozyLifeError(f"orden no aplicada: {detail}")

    def _send(self, on, brightness, timeout):
        """True si el bombillo confirmó la orden."""
        try:
            self.client.apply(on, brightness, timeout)
        except Exception as e:
            self._record_failure(e)
            return False
        return True

    def _verify(self, on, brightness, left):
        """Lee hasta VERIFY_READS veces; devuelve el último estado leído o None si no respondió."""
        state = None
        for attempt in range(VERIFY_READS):
            if attempt:
                time.sleep(max(0, left(VERIFY_PAUSE_S)))
            if left(VERIFY_READ_S) <= 0:
                break
            try:
                state = self.read(left(VERIFY_READ_S))
            except Exception:
                continue
            if self._matches(state, on, brightness):
                break
        return state

    def _matches(self, state, on, brightness):
        return state[0] == on and (not on or state[1] == self._reported_percent(brightness))

    def status(self):
        with self._lock:
            known = self._last is not None
            return {
                "known": known,
                "on": self._last[0] if known else False,
                "brightness": self._last[1] if known else 0,
                "responding": self._failures < KEEPALIVE_FAILURES,
                "last_seen_s": round(time.monotonic() - self._last_at) if known else None,
            }

    def close(self):
        self._stop.set()
        self._keepalive.join(timeout=self.client.timeout + 1)
        self.client.close()

    def _keepalive_loop(self):
        while not self._stop.is_set():
            try:
                self.read(KEEPALIVE_READ_S)
            except Exception:
                pass  # ya quedó registrado en _record_failure
            self._stop.wait(KEEPALIVE_S)

    def _record_success(self, state):
        with self._lock:
            recovered = self._failures >= KEEPALIVE_FAILURES
            self._failures = 0
            previous, self._last, self._last_at = self._last, state, time.monotonic()
        if recovered:
            log("bombillo: conexión recuperada")
        # "apagada" no incluye el brillo: un apagado no se registra dos veces si el brillo difiere.
        if previous is None or describe(*previous) != describe(*state):
            log(f"bombillo: {describe(*state)}")

    def _record_failure(self, error):
        with self._lock:
            self._failures += 1
            lost = self._failures == KEEPALIVE_FAILURES
        if lost:
            log(f"bombillo: se perdió la conexión ({error})")


class TuyaBackend(SimulatedBackend):
    """Bombillo Tuya en la red local. SIN PROBAR hasta tener el bombillo.

    Lee device_id, ip, local_key y version de hub/config.json (excluido de Git).
    GET devuelve el último estado enviado, no el que reporta el bombillo.
    """

    mode = "tuya"

    def __init__(self):
        super().__init__()
        import tinytuya  # import perezoso: el modo simulado no necesita la librería

        config = json.loads(CONFIG_PATH.read_text(encoding="utf-8"))
        self.device = tinytuya.BulbDevice(
            dev_id=config["device_id"],
            address=config["ip"],
            local_key=config["local_key"],
            version=float(config.get("version", 3.3)),
        )

    def apply(self, on, brightness):
        if on:
            self.device.turn_on()
            if brightness > 0:
                self.device.set_brightness_percentage(brightness)
        else:
            self.device.turn_off()
        return super().apply(on, brightness)


PAGE = """<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>LuminaWear Hub</title>
<style>
  body { margin: 0; min-height: 100vh; display: grid; place-items: center;
         background: #15171a; color: #e8e8e8; font-family: system-ui, sans-serif; }
  main { text-align: center; }
  #bulb { width: 180px; height: 180px; border-radius: 50%; margin: 0 auto 24px;
          transition: background-color .3s, box-shadow .3s, opacity .3s; }
  #state { font-size: 20px; }
  #seen { color: #9e9e9e; min-height: 1.5em; }
  #error { color: #ff8a80; min-height: 1.5em; }
</style>
</head>
<body>
<main>
  <div id="bulb" role="img" aria-label="Bombillo"></div>
  <div id="state">Cargando…</div>
  <div id="mode"></div>
  <div id="seen"></div>
  <div id="error"></div>
</main>
<script>
const bulb = document.getElementById("bulb");
const label = document.getElementById("state");
const error = document.getElementById("error");
const seen = document.getElementById("seen");
// /api/status devuelve el último estado conocido sin contactar al bombillo.
async function refresh() {
  try {
    const light = await (await fetch("/api/status")).json();
    error.textContent = light.responding ? "" : "El bombillo no responde";
    seen.textContent = light.last_seen_s === null ? "" : `Última respuesta hace ${light.last_seen_s} s`;
    if (!light.known) {
      label.textContent = "Esperando al bombillo…";
    } else if (light.on) {
      const level = 0.25 + 0.75 * (light.brightness / 100);
      bulb.style.backgroundColor = "#ffd84d";
      bulb.style.opacity = level;
      bulb.style.boxShadow = `0 0 ${20 + light.brightness}px ${light.brightness / 3}px rgba(255, 216, 77, ${level})`;
      label.textContent = `Lámpara · encendida · ${light.brightness} %`;
    } else {
      bulb.style.backgroundColor = "#555";
      bulb.style.opacity = 1;
      bulb.style.boxShadow = "none";
      label.textContent = "Lámpara · apagada";
    }
    bulb.setAttribute("aria-label", label.textContent);
  } catch (e) {
    error.textContent = "Sin conexión con el hub";
  }
}
fetch("/health").then(r => r.json()).then(h => {
  document.getElementById("mode").textContent = "Modo: " + h.mode;
});
refresh();
setInterval(refresh, 1000);
</script>
</body>
</html>
""".replace("LAMP_ID", LAMP_ID)


def make_handler(backend):
    lock = threading.Lock()

    class Handler(BaseHTTPRequestHandler):
        def _send(self, status, payload, content_type="application/json"):
            body = payload if isinstance(payload, bytes) else json.dumps(payload).encode("utf-8")
            self.send_response(status)
            self.send_header("Content-Type", f"{content_type}; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(body)

        def _state(self):
            on, brightness = backend.read()
            return {"id": LAMP_ID, "on": on, "brightness": brightness}

        def _send_state(self):
            try:
                state = self._state()
            except Exception as e:  # el bombillo no respondió: la app muestra el error
                self._send(502, {"error": "device_error", "detail": str(e)})
                return
            self._send(200, state)

        def _light_id(self):
            match = LIGHT_PATH.match(self.path.split("?", 1)[0])
            return match.group(1) if match else None

        def do_GET(self):
            path = self.path.split("?", 1)[0]
            if path == "/":
                self._send(200, PAGE.encode("utf-8"), "text/html")
            elif path == "/health":
                self._send(200, {"ok": True, "mode": backend.mode})
            elif path == "/api/status":
                self._send(200, {"id": LAMP_ID, "mode": backend.mode, **backend.status()})
            elif self._light_id() == LAMP_ID:
                with lock:
                    self._send_state()
            else:
                self._send(404, {"error": "not_found"})

        def do_POST(self):
            light_id = self._light_id()
            if light_id is None or light_id != LAMP_ID:
                self._send(404, {"error": "not_found"})
                return
            try:
                length = int(self.headers.get("Content-Length", "0"))
                data = json.loads(self.rfile.read(length) or b"null")
            except (ValueError, UnicodeDecodeError):
                self._send(400, {"error": "invalid_json"})
                return
            on = data.get("on") if isinstance(data, dict) else None
            brightness = data.get("brightness") if isinstance(data, dict) else None
            # bool es subclase de int en Python: se descarta explícitamente.
            if (not isinstance(on, bool) or not isinstance(brightness, int)
                    or isinstance(brightness, bool) or not 0 <= brightness <= 100):
                self._send(400, {"error": "expected {\"on\": bool, \"brightness\": 0-100}"})
                return
            with lock:
                try:
                    new_on, new_brightness = backend.apply(on, brightness)
                except Exception as e:  # la orden no se logró: la app muestra el error
                    self._send(502, {"error": "device_error", "detail": str(e)})
                    return
                self._send(200, {"id": LAMP_ID, "on": new_on, "brightness": new_brightness})

        def log_message(self, fmt, *args):
            # La página consulta /api/status cada segundo: esas peticiones no se registran.
            if self.path.split("?", 1)[0] in ("/", "/health", "/api/status"):
                return
            sys.stderr.write("[hub] %s - %s\n" % (self.address_string(), fmt % args))

    return Handler


def main():
    parser = argparse.ArgumentParser(description="Hub local de LuminaWear")
    parser.add_argument("--mode", choices=["simulated", "cozylife", "tuya"], default="simulated")
    parser.add_argument("--port", type=int, default=PORT)
    parser.add_argument("--host", default=HOST,
                        help="dirección de escucha; 0.0.0.0 para un reloj físico en la misma Wi-Fi (sin autenticación)")
    args = parser.parse_args()

    backends = {"simulated": SimulatedBackend, "cozylife": CozyLifeBackend, "tuya": TuyaBackend}
    try:
        backend = backends[args.mode]()
    except Exception as e:
        sys.exit(f"No se pudo iniciar el modo {args.mode}: {e}")
    server = ThreadingHTTPServer((args.host, args.port), make_handler(backend))
    print(f"LuminaWear hub ({backend.mode}) escuchando en {args.host}:{args.port}  "
          f"(PC: http://localhost:{args.port} · emulador: http://10.0.2.2:{args.port})")
    if args.host != "127.0.0.1":
        print("Aviso: el hub es accesible desde la red y no tiene autenticación. Úsalo solo en una red de confianza.")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("Cerrando el hub…")
    finally:
        server.server_close()
        backend.close()


if __name__ == "__main__":
    main()
