# Hub local de LuminaWear

Puente HTTP entre la app y la Lámpara de Sala (`living_lampara`). Python 3, sin dependencias en modo simulado.

## Arrancar

```
python hub/hub.py                # modo simulado (por defecto), puerto 8787
python hub/hub.py --mode tuya    # bombillo Tuya real — SIN PROBAR
```

- Navegador del PC: http://localhost:8787 (bombillo visual que se actualiza cada segundo).
- Emulador Wear OS: la app usa `http://10.0.2.2:8787` (clave `hub_url` en `luminawear_prefs`).
- Escucha en `0.0.0.0` y no tiene autenticación: úsalo solo en una red de confianza.

## Antes del demo: permiso de red local

En Android 17 (API 37) la app necesita **"Dispositivos cercanos" / `ACCESS_LOCAL_NETWORK`** para llegar a `10.0.2.2`. Sin él, la conexión hace timeout y la app muestra "No se pudo".

- La app lo pide la primera vez que se toca la Lámpara: elige **Permitir**.
- Si se negó dos veces, Android ya no vuelve a preguntar: actívalo en Ajustes del reloj → Apps → LuminaWear → Permisos, o desde el PC:
  `adb shell pm grant com.alejandro.luminawear android.permission.ACCESS_LOCAL_NETWORK`

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
