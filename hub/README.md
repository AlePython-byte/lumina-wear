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

Con el hub simulado:

1. Arranca el hub: `python hub/hub.py` y abre http://localhost:8787 (el bombillo debe verse encendido al 45 %).
2. Comprueba `http://localhost:8787/health` → `{"ok": true, "mode": "simulated"}`.
3. Arranca el emulador y la app; toca la Lámpara de Sala y concede **"Dispositivos cercanos"** (ver abajo).
4. Si el estado de la app y del bombillo no coinciden, usa **Favoritos → Restaurar demo**: vuelve a la Lámpara al 45 % y la envía al hub.
5. Reloj físico: hub con `--host 0.0.0.0`, la IP del PC en `hub_url` y en `network_security_config` (no subas IPs privadas a Git).

Con el bombillo CozyLife real (además de lo anterior):

1. El PC debe estar en **la misma Wi-Fi que el bombillo** (no en el cable si va a otra red, ni en el punto de acceso del celular). Comprueba con `ipconfig` que la IP del PC sea de la misma red que `cozylife.ip`.
2. `python hub/tools/cozylife_probe.py` debe imprimir `info` y `estado`. Si no, ver "Si el bombillo no responde".
3. `python hub/hub.py --mode cozylife`. En la consola debe aparecer `bombillo: encendida N %` (o `apagada`) en unos segundos, y la página no debe decir "El bombillo no responde".
4. La app no lee el estado del bombillo: la primera orden desde la app (o **Restaurar demo**) los pone de acuerdo.
5. Deja el bombillo con corriente (interruptor encendido) todo el demo: sin corriente el hub responde 502 y la app muestra "No se pudo".

## Permiso de red local

En Android 17 (API 37) la app necesita **"Dispositivos cercanos" / `ACCESS_LOCAL_NETWORK`** para llegar a `10.0.2.2`. Sin él, la conexión hace timeout y la app muestra "No se pudo".

- La app lo pide la primera vez que se envía algo al hub (tocar la Lámpara, una escena, "Apagar todo"…): elige **Permitir**.
- Si se negó dos veces, Android ya no vuelve a preguntar: actívalo en Ajustes del reloj → Apps → LuminaWear → Permisos, o desde el PC:
  `adb shell pm grant com.alejandro.luminawear android.permission.ACCESS_LOCAL_NETWORK`

## Qué envía la app

- Control de la Lámpara: encender/apagar al instante; −/+ tras 300 ms sin tocar.
- Escenas, "Apagar todo", "Deshacer" y "Restaurar demo": un solo POST con el estado final de la Lámpara, solo si la acción la cambia. Si falla, todas las luces vuelven a como estaban.
- "Conectando" solo aparece si el hub tarda más de 400 ms.
- La app espera la respuesta del hub hasta 10 s (`HubProvider.TIMEOUT_MS`), más que el tope de una orden en el hub (8 s), para no cortar antes que él.

## Endpoints

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/` | Página con el bombillo (gris = apagado; brillo proporcional). |
| GET | `/health` | `{"ok": true, "mode": "simulated" \| "cozylife" \| "tuya"}` |
| GET | `/api/status` | Último estado conocido, sin contactar al bombillo: `{"id", "mode", "known", "on", "brightness", "responding", "last_seen_s"}`. Lo usa la página. |
| GET | `/api/lights/living_lampara` | `{"id", "on", "brightness"}` (en modo cozylife, consulta directa al bombillo) |
| POST | `/api/lights/living_lampara` | Cuerpo `{"on": bool, "brightness": 0-100}` → 200 con el estado nuevo. JSON inválido o fuera de rango → 400. |

Cualquier otro id devuelve 404. En modo cozylife, si el bombillo no responde (o la orden no se logra, ver "Cómo se envía una orden"), el GET y el POST devuelven 502 (`{"error": "device_error", "detail": …}`). En modo tuya, solo el POST.

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
4. http://localhost:8787 muestra el último estado conocido del bombillo y "Última respuesta hace N s". Si alguien lo cambia desde la app CozyLife, la página lo refleja en el siguiente keepalive. Si el keepalive lleva 2 consultas seguidas fallando, dice "El bombillo no responde".

### Keepalive

Mientras el hub corre, un hilo de fondo consulta el estado del bombillo cada 8 s (`KEEPALIVE_S`), sin enviar órdenes, y guarda el último estado con su hora. La página lo lee de `/api/status`; el GET de la luz sigue consultando al bombillo directamente (502 si no responde). Si el bombillo vuelve, el hub se recupera solo: no hace falta reiniciarlo.

En consola solo aparecen:
- cambios de estado: `bombillo: encendida 20 %`, `bombillo: apagada`;
- pérdida y recuperación: `bombillo: se perdió la conexión (…)`, `bombillo: conexión recuperada`;
- el resultado de cada orden (ver abajo) y las peticiones de la app (no las de la página).

Ctrl+C detiene el servidor, el keepalive y cierra la conexión con el bombillo.

### Cómo se envía una orden

La Wi-Fi del bombillo tiene cortes de varios segundos y a veces aplica la orden sin que llegue su confirmación. Para que la app no muestre "No se pudo" con el bombillo ya cambiado, un POST sigue estos pasos con un tope total de 8 s (`ORDER_DEADLINE_S`):

| Caso | Qué hace el hub | Respuesta | Consola |
|---|---|---|---|
| El bombillo confirma (espera hasta 3,5 s) | Lee el estado para verificar | 200 con el estado leído (o el pedido si la lectura falla) | `orden confirmada: …` |
| Sin confirmación, pero el bombillo ya tiene lo pedido | No reenvía; lee el estado hasta 2 veces | 200 | `orden verificada sin confirmación: …` |
| Sin confirmación y el bombillo tiene otra cosa | Reenvía la orden **una vez** (valores absolutos) y vuelve a verificar | 200 si quedó aplicada | `orden reenviada: … (confirmada \| verificada sin confirmación)` |
| No responde, o sigue distinto tras reenviar | — | 502 | `orden fallida: … (motivo)` |

Detalles:
- GET espera como máximo 5 s; el keepalive, 2,5 s. Si una conexión reutilizada estaba caída, el hub reconecta dentro del mismo plazo.
- Brillo: la app usa 0-100 y el bombillo 0-1000 (se multiplica por 10). Encender con 0 % se envía como 1 %. Al encender se pone el modo blanco (si estaba en un color o efecto, cambia a blanco).
- Apagar no cambia el brillo guardado en el bombillo; la respuesta trae el brillo que reporta el bombillo.
- Tras un corte de corriente, el bombillo vuelve con su último estado (no necesariamente al máximo) y su servicio local tarda ~1 min en responder, algo más que el ping.

### Si el bombillo no responde

Síntoma: el probe dice `sin respuesta de …` o `denegó expresamente dicha conexión`, el hub responde 502 y la página dice "El bombillo no responde". En orden:

1. **¿El PC está en la red del bombillo?** `ipconfig`: la IP del PC debe ser de la misma red que `cozylife.ip` (normalmente, los tres primeros números iguales). Si el PC cambió de red (cable, punto de acceso del celular), vuelve a la Wi-Fi de la casa.
2. **¿Responde al ping?** `ping <cozylife.ip>`. Si responde pero el puerto 5555 rechaza o no contesta durante minutos, corta la corriente del bombillo ~10 s y vuelve a encenderlo; el hub se recupera solo en ~1 min.
3. **¿Cambió la IP?** Ver la sección siguiente.

### Si cambia la IP del bombillo

El router puede darle otra IP al bombillo (tras un corte de luz o de Wi-Fi). Síntoma: el ping a la IP de `config.json` no responde aunque la app CozyLife muestra el bombillo en línea (y el PC sí está en la red de casa).

1. Busca la IP nueva en la lista de clientes del router por la MAC del bombillo (o en la app CozyLife → dispositivo → información, si la muestra). El `did` que imprime el probe termina en la MAC sin dos puntos: así confirmas que es el bombillo correcto.
2. Cámbiala en `hub/config.json` → `cozylife.ip`.
3. Comprueba con `python hub/tools/cozylife_probe.py` y **reinicia el hub** (la IP se lee al arrancar).
4. Para que no vuelva a pasar, reserva la IP del bombillo en el router (DHCP estático).

No pongas la IP en `config.example.json` ni en ningún archivo que vaya a Git. No escanees la red para buscarla.

## Modo tuya (sin probar)

1. `pip install tinytuya` (solo para este modo).
2. Copia `config.example.json` a `config.json` y llena `device_id`, `ip`, `local_key` y `version`.
3. `config.json` está en `.gitignore`: **nunca** lo subas.
