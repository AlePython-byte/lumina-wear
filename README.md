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
- Pantalla inicial básica integrada (`AppScaffold`, `ScreenScaffold`, `TransformingLazyColumn`). *Nota: Compilación exitosa, pero la ejecución en emulador está pendiente.*
- Módulo único (`app`) que incluye `play-services-wearable`.
- No hay integración de backend, Room, IA, Hilt o Retrofit hasta este momento.
- Proyecto aislado en el directorio `lumina-wear`.

## Estado Actual (LW-004)
- Pantalla `LightControlScreen` implementada con controles de encendido y ajuste porcentual (escalas de 10%).
- Gráfica semicircular dinámica creada mediante Canvas de Jetpack Compose indicando el nivel de brillo exacto y adaptada a Wear OS.
- Estado compartido extraído a `LightStateHolder.kt` con soporte para recordar independientemente el encendido y el porcentaje de cada luz individual (incluso el último brillo al apagar la luz) sobreviviendo a recreaciones de la UI mediante serialización en `rememberSaveable`.
- Navegación extendida desde la lista de habitación (`RoomDetailScreen`) a la configuración de las luces individuales.
- Todos los textos agregados en `strings.xml` y botones provistos con descripciones semánticas.

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
   - *Nota (2026-10-07):* La compilación desde PowerShell es exitosa (`BUILD SUCCESSFUL in 1m 6s; 36 tareas ejecutadas: 14 executed, 22 up-to-date`). Se retiró `ANDROID_PREFS_ROOT` en esa sesión de PowerShell para evitar el conflicto con `ANDROID_USER_HOME`.

## Decisiones y Problemas Pendientes
- La versión de `androidx.wear.compose:compose-material3` está definida en `1.5.6`. Se mantendrá porque es la generada por la plantilla y compila correctamente.
- La familia tipográfica Roboto Flex sigue pendiente de integración.
- La ejecución en emulador todavía está pendiente de comprobar.
