# Hub local de LuminaWear

Puente HTTP entre la app y la Lámpara de Sala (`living_lampara`). Python 3, sin dependencias en modo simulado.

## Arrancar

```
python hub/hub.py                  # modo simulado (por defecto), 127.0.0.1:8787
python hub/hub.py --host 0.0.0.0   # además escucha en la red (reloj físico en la misma Wi-Fi)
python hub/hub.py --mode cozylife  # bombillo CozyLife real (ver "Modo cozylife")
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
| GET | `/health` | `{"ok": true, "mode": "simulated" \| "cozylife" \| "tuya"}` |
| GET | `/api/lights/living_lampara` | `{"id", "on", "brightness"}` |
| POST | `/api/lights/living_lampara` | Cuerpo `{"on": bool, "brightness": 0-100}` → 200 con el estado nuevo. JSON inválido o fuera de rango → 400. |

Cualquier otro id devuelve 404. En modo cozylife, si el bombillo no responde, el GET y el POST devuelven 502 (`{"error": "device_error", "detail": …}`). En modo tuya, solo el POST.

## Modo cozylife (bombillo real)

Bombillo CozyLife RGBCW emparejado con la app CozyLife y en la misma Wi-Fi que el PC. Solo librería estándar.

1. Copia `config.example.json` a `config.json` (si no existe) y pon la IP del bombillo en `cozylife.ip`. `port` es 5555 (el del protocolo local de CozyLife).
2. Prueba el bombillo sin el hub:
   ```
   python hub/tools/cozylife_probe.py                  # info y estado
   python hub/tools/cozylife_probe.py --off
   python hub/tools/cozylife_probe.py --on
   python hub/tools/cozylife_probe.py --brightness 30  # 0-100, también enciende
   ```
3. Arranca el hub: `python hub/hub.py --mode cozylife`. `/health` debe decir `"mode": "cozylife"`.
4. http://localhost:8787 muestra el estado **real** del bombillo (se consulta cada segundo); si alguien lo cambia desde la app CozyLife, la página lo refleja. Si el bombillo no responde, dice "El bombillo no responde".

Detalles:
- Cada consulta u orden espera como máximo 2 s. Si la conexión se cae, el hub reconecta y reintenta una vez; si vuelve a fallar responde 502 y la app muestra "No se pudo".
- Brillo: la app usa 0-100 y el bombillo 0-1000 (se multiplica por 10). Encender con 0 % se envía como 1 %. Al encender se pone el modo blanco (si estaba en un color o efecto, cambia a blanco).
- Apagar no cambia el brillo guardado en el bombillo; la respuesta trae el brillo que reporta el bombillo.

### Si cambia la IP del bombillo

El router puede darle otra IP al bombillo (tras un corte de luz o de Wi-Fi). Síntoma: el probe dice `sin respuesta de …` y el hub responde 502.

1. Busca la IP nueva en la lista de clientes del router (o en la app CozyLife → dispositivo → información, si la muestra).
2. Cámbiala en `hub/config.json` → `cozylife.ip`.
3. Comprueba con `python hub/tools/cozylife_probe.py` y reinicia el hub (la IP se lee al arrancar).
4. Para que no vuelva a pasar, reserva la IP del bombillo en el router (DHCP estático).

No pongas la IP en `config.example.json` ni en ningún archivo que vaya a Git.

## Modo tuya (sin probar)

1. `pip install tinytuya` (solo para este modo).
2. Copia `config.example.json` a `config.json` y llena `device_id`, `ip`, `local_key` y `version`.
3. `config.json` está en `.gitignore`: **nunca** lo subas.
