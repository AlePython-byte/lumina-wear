# Hub local de LuminaWear

Puente HTTP entre la app y la Lámpara de Sala (`living_lampara`). Python 3, sin dependencias en modo simulado.

## Arrancar

```
python hub/hub.py                  # modo simulado (por defecto), 127.0.0.1:8787
python hub/hub.py --host 0.0.0.0   # además escucha en la red (reloj físico en la misma Wi-Fi)
python hub/hub.py --mode tuya      # bombillo Tuya real — SIN PROBAR
```

- Navegador del PC: http://localhost:8787 (bombillo visual que se actualiza cada segundo).
- Emulador Wear OS: la app usa `http://10.0.2.2:8787` (clave `hub_url` en `luminawear_prefs`). `10.0.2.2` es el `127.0.0.1` del PC, así que el modo por defecto basta.
- **Por defecto solo escucha en `127.0.0.1`**: nadie más en la red puede cambiar la luz.
- `--host 0.0.0.0` solo hace falta si un dispositivo de la red llama al hub (un reloj físico). El hub no tiene autenticación: úsalo solo en una red de confianza y vuelve al valor por defecto después.
- El bombillo Tuya por Wi-Fi **no** necesita `--host 0.0.0.0`: es el hub quien se conecta al bombillo, no al revés.

## Antes del demo

1. Arranca el hub: `python hub/hub.py` y abre http://localhost:8787 (el bombillo debe verse encendido al 45 %).
2. Comprueba `http://localhost:8787/health` → `{"ok": true, "mode": "simulated"}`.
3. Arranca el emulador y la app; toca la Lámpara de Sala y concede **"Dispositivos cercanos"** (ver abajo).
4. Si el estado de la app y del bombillo no coinciden, usa **Favoritos → Restaurar demo**: vuelve a la Lámpara al 45 % y la envía al hub.
5. Reloj físico: hub con `--host 0.0.0.0`, la IP del PC en `hub_url` y en `network_security_config` (no subas IPs privadas a Git).

## Permiso de red local

En Android 17 (API 37) la app necesita **"Dispositivos cercanos" / `ACCESS_LOCAL_NETWORK`** para llegar a `10.0.2.2`. Sin él, la conexión hace timeout y la app muestra "No se pudo".

- La app lo pide la primera vez que se envía algo al hub (tocar la Lámpara, una escena, "Apagar todo"…): elige **Permitir**.
- Si se negó dos veces, Android ya no vuelve a preguntar: actívalo en Ajustes del reloj → Apps → LuminaWear → Permisos, o desde el PC:
  `adb shell pm grant com.alejandro.luminawear android.permission.ACCESS_LOCAL_NETWORK`

## Qué envía la app

- Control de la Lámpara: encender/apagar al instante; −/+ tras 300 ms sin tocar.
- Escenas, "Apagar todo", "Deshacer" y "Restaurar demo": un solo POST con el estado final de la Lámpara, solo si la acción la cambia. Si falla, todas las luces vuelven a como estaban.
- "Conectando" solo aparece si el hub tarda más de 400 ms.

## Endpoints

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/` | Página con el bombillo (gris = apagado; brillo proporcional). |
| GET | `/health` | `{"ok": true, "mode": "simulated" \| "tuya"}` |
| GET | `/api/lights/living_lampara` | `{"id", "on", "brightness"}` |
| POST | `/api/lights/living_lampara` | Cuerpo `{"on": bool, "brightness": 0-100}` → 200 con el estado nuevo. JSON inválido o fuera de rango → 400. |

Cualquier otro id devuelve 404. En modo tuya, si el bombillo falla, el POST devuelve 502.

## Modo tuya (sin probar)

1. `pip install tinytuya` (solo para este modo).
2. Copia `config.example.json` a `config.json` y llena `device_id`, `ip`, `local_key` y `version`.
3. `config.json` está en `.gitignore`: **nunca** lo subas.
