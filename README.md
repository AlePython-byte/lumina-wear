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

## Estado Actual (LW-003)
- Pantalla principal `HomeScreen` funcional con estado local.
- Navegación local hacia pantallas `RoomsScreen` y `RoomDetailScreen` sin librerías externas.
- El estado simulado de luces (5 de 8 encendidas) se centralizó a nivel de `MainActivity` manejando un listado unificado de 3 habitaciones y 8 luces (id, estado y nombre); "Apagar todo" apaga las luces reales.
- El estado sobrevive recreaciones de sistema mediante el uso de representaciones convertidas a cadenas vía `rememberSaveable`.
- Las pantallas `RoomsScreen` y `RoomDetailScreen` muestran listas de habitaciones o luces y permiten regresar usando los botones o el gesto físico interceptado por `BackHandler`. El gesto SwipeToDismiss propio de Wear OS sigue pendiente.
- Todos los textos extraídos a `strings.xml` de forma independiente (ej. separando "MI CASA" y "DEMO").

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
   - *Nota (2026-10-07):* La compilación desde PowerShell es exitosa (`BUILD SUCCESSFUL in 39s; 36 tareas ejecutadas: 13 executed, 23 up-to-date`). Se retiró `ANDROID_PREFS_ROOT` en esa sesión de PowerShell para evitar el conflicto con `ANDROID_USER_HOME`.

## Decisiones y Problemas Pendientes
- La versión de `androidx.wear.compose:compose-material3` está definida en `1.5.6`. Se mantendrá porque es la generada por la plantilla y compila correctamente.
- La familia tipográfica Roboto Flex sigue pendiente de integración.
- La ejecución en emulador todavía está pendiente de comprobar.
