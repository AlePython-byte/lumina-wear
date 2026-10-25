"""Prueba directa del bombillo CozyLife, sin pasar por el hub.

Lee la IP de hub/config.json (sección "cozylife"); no contacta ninguna otra dirección.

Uso:
    python hub/tools/cozylife_probe.py                   # imprime info y estado
    python hub/tools/cozylife_probe.py --on
    python hub/tools/cozylife_probe.py --off
    python hub/tools/cozylife_probe.py --brightness 30   # 0-100; también enciende
"""

import argparse
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from cozylife import CozyLifeClient, CozyLifeError, load_config  # noqa: E402


def percent(value):
    n = int(value)
    if not 0 <= n <= 100:
        raise argparse.ArgumentTypeError("el brillo va de 0 a 100")
    return n


def main():
    parser = argparse.ArgumentParser(description="Prueba del bombillo CozyLife")
    action = parser.add_mutually_exclusive_group()
    action.add_argument("--on", action="store_true", help="encender (conserva el brillo actual)")
    action.add_argument("--off", action="store_true", help="apagar")
    action.add_argument("--brightness", type=percent, metavar="N", help="encender con brillo N (0-100)")
    args = parser.parse_args()

    phase = "conexión y consulta inicial"
    try:
        ip, port = load_config()
        client = CozyLifeClient(ip, port)
        info = client.info()
        print("info:", json.dumps({k: info.get(k) for k in ("did", "pid", "dmn", "dpid") if k in info}))
        raw = client.query()
        on, brightness = client.read_state()
        print("estado crudo:", json.dumps(raw, sort_keys=True))
        print(f"estado: {'encendido' if on else 'apagado'}, brillo {brightness} %")

        if args.on or args.off or args.brightness is not None:
            phase = "envío de la orden"
            if args.off:
                client.apply(False, brightness)
            else:
                client.apply(True, brightness if args.on else args.brightness)
            print("orden confirmada por el bombillo")
            phase = "lectura posterior del estado"
            on, brightness = client.read_state()
            print(f"después: {'encendido' if on else 'apagado'}, brillo {brightness} %")
        client.close()
    except CozyLifeError as e:
        print(f"error en {phase}: {e}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
