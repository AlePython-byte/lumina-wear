"""Hub local de LuminaWear.

Modo "simulated" (por defecto): solo librería estándar, guarda el estado en memoria.
Modo "cozylife": controla el bombillo CozyLife real por la red local (librería estándar,
IP en hub/config.json). GET lee el estado real del bombillo.
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
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

PORT = 8787
# Solo este PC por defecto: el emulador llega por 10.0.2.2, que es el 127.0.0.1 del PC.
HOST = "127.0.0.1"
# Única luz conectada: Lámpara de Sala (mismo id que en la app).
LAMP_ID = "living_lampara"
CONFIG_PATH = Path(__file__).with_name("config.json")
LIGHT_PATH = re.compile(r"^/api/lights/([^/]+)$")


class SimulatedBackend:
    """Bombillo en memoria. Estado inicial igual al de la app: encendida al 45 %."""

    mode = "simulated"

    def __init__(self):
        self.on = True
        self.brightness = 45

    def read(self):
        return self.on, self.brightness

    def apply(self, on, brightness):
        self.on = on
        self.brightness = brightness


class CozyLifeBackend:
    """Bombillo CozyLife en la red local (ver hub/cozylife.py).

    GET consulta el bombillo cada vez; si no responde en 2 s (con un reintento tras
    reconectar), el hub responde 502.
    """

    mode = "cozylife"

    def __init__(self):
        from cozylife import CozyLifeClient, load_config

        ip, port = load_config()
        self.client = CozyLifeClient(ip, port)

    def read(self):
        return self.client.read_state()

    def apply(self, on, brightness):
        self.client.apply(on, brightness)


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
        super().apply(on, brightness)


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
  #error { color: #ff8a80; min-height: 1.5em; }
</style>
</head>
<body>
<main>
  <div id="bulb" role="img" aria-label="Bombillo"></div>
  <div id="state">Cargando…</div>
  <div id="mode"></div>
  <div id="error"></div>
</main>
<script>
const bulb = document.getElementById("bulb");
const label = document.getElementById("state");
const error = document.getElementById("error");
async function refresh() {
  try {
    const res = await fetch("/api/lights/LAMP_ID");
    if (res.status === 502) { error.textContent = "El bombillo no responde"; return; }
    const light = await res.json();
    if (light.on) {
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
    error.textContent = "";
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
                    backend.apply(on, brightness)
                except Exception as e:  # el bombillo no respondió: la app muestra el error
                    self._send(502, {"error": "device_error", "detail": str(e)})
                    return
                self._send_state()

        def log_message(self, fmt, *args):
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
        pass
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
