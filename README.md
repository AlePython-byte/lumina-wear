# LuminaWear

App para **Wear OS** que controla las luces del hogar desde la muñeca. Proyecto individual de *Diseño de interfaces de software* (Universidad Cooperativa de Colombia, sede Pasto).

## Qué hace hoy

- **Luces**: encender, apagar y ajustar el brillo de cada luz, por habitación.
- **Escenas**: Descanso, Lectura, Concentración y Buenas noches, con sugerencia según la hora y *Deshacer*.
- **Favoritos**: accesos rápidos a luces y escenas, editables.
- **Asistente con IA**: se le pide en texto («pon la sala tranquila») y pide confirmación antes de actuar.
- **Primer uso**: bienvenida y elección entre una casa demo o empezar desde cero.
- **Añadir y quitar luces**, con confirmación al quitar.
- **Una luz real**: la «Lámpara con hub» enciende y apaga un bombillo físico a través del hub; las demás son simuladas.
- **Tema claro y oscuro**, y **español e inglés** con selector dentro de la app.

## Tecnología

- Kotlin, Jetpack Compose para Wear OS y Wear Compose Material 3.
- Hub local en Python (puente entre la app y el bombillo).
- Servidor intermedio del asistente: un Worker de Cloudflare que consulta a Gemini.

## Documentación

- [`docs/documentacion.md`](docs/documentacion.md): qué hace, arquitectura de la información, sistema de diseño, asistente, bombillo real, estado y límites.
- [`docs/design-tokens.md`](docs/design-tokens.md): tokens de diseño.
- [`docs/avances.md`](docs/avances.md): bitácora de avances.
- [`hub/README.md`](hub/README.md): hub local y bombillo real.
- [`worker/README.md`](worker/README.md): servidor intermedio del asistente.

## Requisitos

| Qué | Versión |
|---|---|
| JDK | 25 (`gradle/gradle-daemon-jvm.properties`) |
| Gradle | 9.6.0, con el wrapper (`gradlew.bat`) |
| Plugin de Android para Gradle (AGP) | 9.4.1 |
| Kotlin | 2.2.10 |
| Android SDK | plataforma 37 (`compileSdk` y `targetSdk` 37; `minSdk` 30) |
| Dispositivo | emulador Wear OS redondo (por ejemplo, *Wear OS Small Round*) |
| Python | 3, solo para el hub (sin dependencias en modo simulado) |

Se recomienda Android Studio para instalar el SDK y crear el emulador.

## Configuración local: `local.properties`

El archivo `local.properties` está en la raíz y **no se sube a Git**. Android Studio lo crea con `sdk.dir`. Las claves que lee el proyecto son:

| Clave | Para qué | ¿Obligatoria? |
|---|---|---|
| `sdk.dir` | Ruta del Android SDK | Sí (la escribe Android Studio) |
| `assistant.url` | URL (HTTPS) del servidor intermedio del asistente | No |
| `assistant.token` | Token compartido con ese servidor | No |

Sin `assistant.url` ni `assistant.token` la app compila y funciona; las reglas locales del asistente siguen respondiendo y las frases que requieren IA muestran «Sin configurar». Cómo obtener esos dos valores: [`worker/README.md`](worker/README.md). No pegues los valores en el README, en issues ni en commits.

## Compilar y ejecutar (Windows, PowerShell)

1. Si `ANDROID_PREFS_ROOT` está definida en tu sesión, quítala antes de usar Gradle. En este proyecto la compilación por terminal fallaba con el error `AndroidLocationsBuildService` por un conflicto con `ANDROID_USER_HOME` (ver [`docs/avances.md`](docs/avances.md)):

   ```
   Remove-Item Env:ANDROID_PREFS_ROOT -ErrorAction SilentlyContinue
   ```

   Solo afecta a esa sesión de PowerShell.

2. Abre un emulador. Desde Android Studio (*Device Manager*) o desde la terminal, con la ruta de `sdk.dir`:

   ```
   & "<sdk.dir>\emulator\emulator.exe" -list-avds
   & "<sdk.dir>\emulator\emulator.exe" -avd <nombre_del_avd>
   ```

3. Compila e instala la versión de depuración (con el emulador ya encendido):

   ```
   .\gradlew.bat :app:assembleDebug
   .\gradlew.bat :app:installDebug
   ```

   También puedes pulsar **Run 'app'** en Android Studio.

4. Abre **LuminaWear** en el emulador. Si la app pide «Dispositivos cercanos», permítelo: Android 17 lo exige para hablar con el hub (ver [`hub/README.md`](hub/README.md)).

### Debug y release

La demo se hace con la compilación de **debug**.

- `:app:assembleRelease` compila, pero el APK sale **sin firmar** (no hay configuración de firma en el proyecto), así que no se instala tal cual. Tampoco tiene minificación activada.
- Las compilaciones de debug permiten HTTP sin cifrar al hub local en `10.0.2.2` (emulador) y en las IPs de la LAN y del hotspot que figuran en `app/src/debug/res/xml/network_security_config.xml`. El release solo permite `10.0.2.2` (`app/src/main/res/xml/network_security_config.xml`): no sirve para hablar con un hub en la red de casa. Todo lo demás exige HTTPS.
- El extra `hub_url` para fijar la dirección del hub sin recompilar solo funciona en debug.
