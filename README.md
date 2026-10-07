# LuminaWear

LuminaWear es una aplicación universitaria para Wear OS enfocada en el diseño de interfaces. Su objetivo es permitir a los usuarios controlar luces simuladas por habitación, ajustar el brillo, activar escenas y gestionar favoritos desde su reloj inteligente. Más adelante se planea integrar control por voz e IA.

## Requisitos Detectados
- **Dispositivo de destino:** Wear OS (Reloj inteligente).
- **SDK de Android:** `compileSdk` y `targetSdk` están configurados en 37.
- **Minimum SDK:** 30 (Android 11).
- **Framework de UI:** Jetpack Compose para Wear OS (Material 3).
- **Lenguaje:** Kotlin (versión 2.2.10).

## Estado Inicial (LW-001)
- Proyecto generado correctamente y configurado con Jetpack Compose para Wear OS y Material 3.
- Pantalla inicial básica funcionando (`AppScaffold`, `ScreenScaffold`, `TransformingLazyColumn`).
- Módulo único (`app`) que incluye `play-services-wearable`.
- No hay integración de backend, Room, IA, Hilt o Retrofit hasta este momento.
- Proyecto aislado en el directorio `lumina-wear`.

## Instrucciones de Ejecución
Para ejecutar esta aplicación en un emulador o dispositivo físico Wear OS circular:

1. **Configurar el entorno:**
   - Asegúrate de tener Android Studio instalado y actualizado.
   - Crea un **Android Virtual Device (AVD)** seleccionando un dispositivo tipo **Wear OS Small Round** o **Wear OS Large Round** (preferiblemente con API 30 o superior).
2. **Abrir el proyecto:**
   - Abre la carpeta `lumina-wear` en Android Studio.
   - Deja que Gradle sincronice las dependencias.
3. **Ejecutar la app:**
   - En Android Studio, selecciona el dispositivo virtual de Wear OS en el menú desplegable de ejecución.
   - Haz clic en el botón **Run 'app'** (o presiona `Shift + F10`).
   - *Nota (2026-10-06):* La compilación desde PowerShell es exitosa (`BUILD SUCCESSFUL in 50s; 36 tareas ejecutadas`) usando el comando `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`. Anteriormente fallaba por un error vinculado a `AndroidLocationsBuildService`; esto se resolvió retirando `ANDROID_PREFS_ROOT` únicamente de esa sesión de PowerShell para evitar el conflicto con `ANDROID_USER_HOME`. La configuración permanente en Windows sigue pendiente.

## Decisiones y Problemas Pendientes
- La versión de `androidx.wear.compose:compose-material3` está definida en `1.5.6`. Se mantendrá porque es la generada por la plantilla y compila correctamente.
- Los textos base todavía no están centralizados en `strings.xml`. En el futuro, se refactorizará para soportar internacionalización (i18n).
- La ejecución en emulador todavía está pendiente de comprobar.
