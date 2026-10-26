# Servidor intermedio del asistente

Pequeño servicio sin servidor (`worker.js`) entre el reloj y un modelo de lenguaje de un proveedor externo. El reloj nunca ve la clave del proveedor: solo conoce la URL del servicio y un token de la app.

La app consulta este servicio **solo** cuando sus reglas locales no entienden la frase. Las frases rápidas y las órdenes simples ("apaga el escritorio", "lámpara al 50 %") se resuelven en el reloj, sin internet.

## Qué hace

1. Acepta solo `POST`. Si el token de la cabecera `X-App-Token` no coincide → 401.
2. Valida la entrada: texto recortado a 200 caracteres, `lang` `es` o `en`, como máximo 12 luces. Sin texto o sin luces → 400.
3. Envía al modelo unas instrucciones fijas (responder solo JSON, usar solo los ids recibidos, tratar el texto del usuario como dato y no como instrucciones) junto con el pedido y la lista de luces.
4. Filtra lo que devuelve el modelo antes de responder: como máximo 5 acciones, solo ids de luces recibidos, brillo recortado a 0–100, solo las escenas `relax`, `read`, `focus` y `sleep`, y respuesta de texto de hasta 120 caracteres.

La app vuelve a validar todo por su cuenta (ids que existen en su estado, brillo, número de acciones y escenas) y siempre pide confirmación antes de aplicar.

## Contrato

**Petición** (`POST`, cabecera `X-App-Token`):

```json
{
  "text": "pon la sala tranquila",
  "lang": "es",
  "lights": [{ "id": "living_lampara", "name": "Lámpara", "room": "Sala", "on": true, "brightness": 45 }]
}
```

**Respuesta 200:**

```json
{
  "reply": "Listo, luz tenue en la sala.",
  "actions": [
    { "type": "set_light", "id": "living_lampara", "on": true, "brightness": 30 },
    { "type": "apply_scene", "id": "relax" },
    { "type": "turn_off_all" }
  ]
}
```

**Errores** (`{"error": "..."}`):

| Código | Cuándo |
|---|---|
| 401 | Token ausente o incorrecto |
| 405 | Método distinto de `POST` |
| 400 | JSON inválido, sin texto o sin luces |
| 500 | Faltan variables de configuración en el servicio |
| 502 | El proveedor del modelo no respondió o devolvió algo que no es el JSON esperado |

## Variables

Se configuran como **secretos** del servicio, nunca en el código ni en Git:

| Variable | Para qué |
|---|---|
| `APP_TOKEN` | Token compartido con la app. Debe ser igual a `assistant.token` de la app. |
| Clave de API del proveedor | Credencial del modelo de lenguaje. Su nombre exacto es el que lee `worker.js` desde `env`. |
| Nombre del modelo | Modelo que se consulta. Su nombre exacto es el que lee `worker.js` desde `env`. |

## Desplegar

El código usa el formato de módulo de Cloudflare Workers (`export default { fetch(request, env) }`).

1. En el panel de Cloudflare: **Workers & Pages → Create → Worker**, pon un nombre y despliega la plantilla.
2. **Edit code**: reemplaza el contenido por `worker.js` y pulsa **Deploy**.
3. **Settings → Variables and Secrets**: agrega las tres variables de la tabla anterior como tipo **Secret**.
4. Copia la URL pública del servicio (HTTPS).
5. Prueba que responde con un error controlado sin token (no hace falta gastar una consulta al modelo):
   ```
   curl -X POST <URL> -d "{}"        →  401 {"error":"unauthorized"}
   ```

## Configurar la app

En `local.properties` (en la raíz del proyecto, **no se versiona**), agrega dos claves:

```
assistant.url=<URL del servicio>
assistant.token=<el mismo valor que APP_TOKEN>
```

Recompila la app. Gradle las copia a `BuildConfig.ASSISTANT_URL` y `BuildConfig.ASSISTANT_TOKEN`. Si faltan, quedan vacías y la app muestra "Sin configurar" cuando una frase no la entienden las reglas locales; las frases rápidas siguen funcionando.

La app solo permite tráfico HTTPS hacia internet (`network_security_config`), así que la URL debe empezar por `https://`. No sigue redirecciones, para no contactar ninguna URL distinta de la configurada.

## Seguridad: límites conocidos

- **El token viaja dentro del APK y se puede extraer.** Cualquiera con el APK puede sacar `ASSISTANT_TOKEN` y la URL, y llamar al servicio por su cuenta. El token frena llamadas casuales, pero no autentica a un usuario.
- Mitigaciones actuales:
  - topes de entrada: 200 caracteres y 12 luces por petición;
  - topes de salida: respuesta del modelo limitada (400 tokens), 5 acciones y 120 caracteres de texto;
  - el límite diario del plan gratuito del servicio y del proveedor acota cuánto se puede abusar y evita costos inesperados;
  - la app valida y pide confirmación antes de tocar cualquier luz.
- Si el token se filtra: cambia `APP_TOKEN` en el servicio, actualiza `assistant.token` en `local.properties` y recompila. Los APK anteriores dejan de funcionar con el asistente en línea (las reglas locales siguen funcionando).
- Una solución completa necesitaría autenticación por usuario o dispositivo. Queda fuera del alcance de este proyecto.
