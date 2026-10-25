"""Cliente mínimo del protocolo local de CozyLife (solo librería estándar).

Implementación propia; el protocolo se confirmó leyendo
https://github.com/cozylife/hass_cozylife_local_pull (sin licencia, no se copió código):
- TCP al puerto 5555. Cada mensaje es un JSON en una línea terminada en "\\r\\n".
- Envoltura: {"pv": 0, "cmd": N, "sn": "<ms>", "msg": {...}}. La respuesta trae el mismo "sn".
- cmd 0 = info, 2 = consultar ({"attr": [0]} = todo), 3 = cambiar ({"attr": [...], "data": {...}}),
  10 = reporte espontáneo del bombillo (se ignora).
- dpid "1" = encendido (255 / 0), "2" = modo (0 = blanco estático), "4" = brillo 0-1000.
"""

import json
import socket
import threading
import time
from pathlib import Path

DEFAULT_PORT = 5555
TIMEOUT_S = 5.0
CONFIG_PATH = Path(__file__).with_name("config.json")

CMD_INFO = 0
CMD_QUERY = 2
CMD_SET = 3

SWITCH = "1"
WORK_MODE = "2"
BRIGHT = "4"
BRIGHT_MAX = 1000


class CozyLifeError(Exception):
    """El bombillo no respondió o respondió algo inesperado."""


def load_config():
    """Devuelve (ip, puerto) de la sección "cozylife" de hub/config.json."""
    if not CONFIG_PATH.exists():
        raise CozyLifeError(f"falta {CONFIG_PATH}: copia config.example.json y pon la IP del bombillo")
    section = json.loads(CONFIG_PATH.read_text(encoding="utf-8")).get("cozylife") or {}
    ip = section.get("ip", "")
    if not ip or ip == "IP_LOCAL_DEL_BOMBILLO":
        raise CozyLifeError("falta cozylife.ip en hub/config.json")
    return ip, int(section.get("port", DEFAULT_PORT))


def to_percent(raw):
    return round(max(0, min(BRIGHT_MAX, raw)) * 100 / BRIGHT_MAX)


def to_raw(percent):
    # 0 % no es un brillo válido con la luz encendida: se usa el mínimo (1 %).
    return max(BRIGHT_MAX // 100, percent * BRIGHT_MAX // 100)


class CozyLifeClient:
    """Una conexión TCP reutilizable, con un plazo total de `timeout` segundos por petición.

    Si la conexión reutilizada estaba caída, se abre una nueva y se reintenta dentro del
    mismo plazo.
    """

    def __init__(self, ip, port=DEFAULT_PORT, timeout=TIMEOUT_S):
        self.ip, self.port, self.timeout = ip, port, timeout
        self._sock = None
        self._buffer = b""
        self._last_sn = 0
        self._lock = threading.Lock()

    def close(self):
        if self._sock is not None:
            try:
                self._sock.close()
            except OSError:
                pass
        self._sock = None
        self._buffer = b""

    def _next_sn(self):
        self._last_sn = max(self._last_sn + 1, int(time.time() * 1000))
        return str(self._last_sn)

    def _connect(self, deadline):
        remaining = deadline - time.monotonic()
        if remaining <= 0:
            raise TimeoutError("sin tiempo para conectar")
        self._sock = socket.create_connection((self.ip, self.port), timeout=remaining)
        self._buffer = b""

    def _read_line(self, deadline):
        while b"\r\n" not in self._buffer:
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                raise TimeoutError("el bombillo no respondió a tiempo")
            self._sock.settimeout(remaining)
            chunk = self._sock.recv(1024)
            if not chunk:
                raise ConnectionError("el bombillo cerró la conexión")
            self._buffer += chunk
            if len(self._buffer) > 64 * 1024:
                raise ValueError("respuesta demasiado grande")
        line, self._buffer = self._buffer.split(b"\r\n", 1)
        return line

    def _exchange(self, cmd, msg, deadline):
        if self._sock is None:
            self._connect(deadline)
        sn = self._next_sn()
        frame = json.dumps({"pv": 0, "cmd": cmd, "sn": sn, "msg": msg}, separators=(",", ":"))
        self._sock.settimeout(max(0.01, deadline - time.monotonic()))
        self._sock.sendall(frame.encode("utf-8") + b"\r\n")
        while True:
            line = self._read_line(deadline)
            if not line.strip():
                continue
            reply = json.loads(line)
            # Se descartan reportes espontáneos (cmd 10) y respuestas a otras peticiones.
            if isinstance(reply, dict) and str(reply.get("sn")) == sn and reply.get("cmd") == cmd:
                if reply.get("res", 0) != 0:
                    raise CozyLifeError(f"el bombillo rechazó la orden (res={reply.get('res')})")
                return reply.get("msg") or {}

    def request(self, cmd, msg):
        with self._lock:
            deadline = time.monotonic() + self.timeout
            while True:
                reused = self._sock is not None
                try:
                    return self._exchange(cmd, msg, deadline)
                except CozyLifeError:
                    raise
                except (OSError, ValueError) as e:  # timeout, conexión caída o JSON roto
                    self.close()
                    # Solo se reintenta si falló una conexión vieja y queda plazo.
                    if not reused or time.monotonic() >= deadline:
                        raise CozyLifeError(f"sin respuesta de {self.ip}:{self.port}: {e}") from e

    def info(self):
        return self.request(CMD_INFO, {})

    def query(self):
        data = self.request(CMD_QUERY, {"attr": [0]}).get("data")
        if not isinstance(data, dict):
            raise CozyLifeError("respuesta de estado sin 'data'")
        return data

    def set(self, data):
        self.request(CMD_SET, {"attr": [int(k) for k in data], "data": data})

    def read_state(self):
        """(encendido, brillo 0-100) según el bombillo."""
        data = self.query()
        return int(data.get(SWITCH, 0)) > 0, to_percent(int(data.get(BRIGHT, 0)))

    def apply(self, on, brightness):
        if on:
            self.set({SWITCH: 255, WORK_MODE: 0, BRIGHT: to_raw(brightness)})
        else:
            self.set({SWITCH: 0})
