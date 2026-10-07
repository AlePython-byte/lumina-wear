# Registro de Avances - LuminaWear

## TAREA LW-001 — Inspección y preparación de la base

**Fecha:** 2026-10-06

**Cambios realizados:**
- Se inspeccionó la estructura del proyecto, detectando que la plantilla generó un módulo único (`app`) configurado con `compileSdk 37` y `minSdk 30`.
- Se verificó el uso de Jetpack Compose para Wear OS con la librería de Material 3 (`androidx.wear.compose:compose-material3`).
- Se validó el archivo `libs.versions.toml`, manteniendo las versiones generadas por la plantilla (Kotlin 2.2.10, AGP 9.4.1, Compose BOM 2024.09.00). No se detectaron problemas para la compilación, por lo que no se realizaron actualizaciones forzadas de dependencias.
- Se actualizó el `.gitignore` del proyecto para garantizar la exclusión adecuada de archivos locales, credenciales (`*.jks`), metadatos del IDE (`.idea/`, `*.iml`) y carpetas de compilación (`build/`, `*/build/`), conservando los elementos necesarios para reproducir el proyecto, como el Gradle Wrapper.
- Se creó el archivo de documentación principal `README.md` en español, explicando el propósito del proyecto, instrucciones de ejecución y configuración.
- Se creó este registro documental (`docs/avances.md`) para llevar una bitácora del progreso.
- (LW-001A) El proyecto fue movido/copiado a un directorio exclusivo (`lumina-wear`) aislando los archivos de LuminaWear del resto de proyectos presentes en la carpeta de la universidad.

**Verificaciones realizadas:**
- La compilación desde Android Studio fue reportada como exitosa (Build finished successfully).
- **(2026-10-06):** Compilación desde PowerShell exitosa: `BUILD SUCCESSFUL in 50s; 36 tareas ejecutadas` usando el comando `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`.
  - *Antecedente:* La compilación por terminal fallaba con el error `AndroidLocationsBuildService`. Se resolvió retirando `ANDROID_PREFS_ROOT` únicamente de esa sesión de PowerShell para solucionar el conflicto con `ANDROID_USER_HOME`. La configuración permanente de Windows sigue pendiente.
- La ejecución en emulador todavía está pendiente de comprobar.
- La pantalla inicial se mantiene intacta en su estructura de plantilla, utilizando componentes como `AppScaffold`, `ScreenScaffold` y `TransformingLazyColumn` (la pantalla compila correctamente, pero su visualización real sigue pendiente de comprobación).

**Pendientes / Próximos pasos:**
- Reemplazar los textos de la pantalla inicial ("More", "Button A", etc.) por recursos en `strings.xml`.
- Diseñar y construir las pantallas principales del producto (control de luces, brillo, escenas, favoritos).
- Definir la arquitectura a usar para la integración de funcionalidades más complejas (Voz, IA, inyección de dependencias con Hilt, bases de datos con Room, consumo de APIs con Retrofit).

---

## TAREA LW-002A — Tema visual base de LuminaWear

**Fecha:** 2026-10-06

**Cambios realizados:**
- Se inspeccionaron `Theme.kt`, `MainActivity.kt` y la configuración de dependencias de UI.
- Se creó `app/src/main/java/com/alejandro/luminawear/presentation/theme/Color.kt` definiendo el esquema de colores base: fondo negro (`#000000`), color primario lima (`#C8FF37`) y superficies oscuras provisionales (`#1E1E1E`, `#2C2C2C`).
- Se actualizó el `LuminaWearTheme` en `Theme.kt` para usar `androidx.wear.compose.material3.ColorScheme` con el esquema personalizado, garantizando que el contenido principal resalte en lima y el fondo quede completamente negro.
- Se mantuvieron la tipografía por defecto y las previews, así como los componentes existentes en `MainActivity.kt`.

**Verificaciones realizadas:**
- **(2026-10-06):** Compilación tras aplicar el tema en PowerShell exitosa: `BUILD SUCCESSFUL in 34s; 36 tareas ejecutadas` mediante `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`.
- La pantalla de inicio compila con éxito aplicando `LuminaWearTheme`.
- **Aclaración importante:** Tal como en tareas anteriores, la app se encuentra compilando, pero la ejecución en emulador no se ha efectuado y su verificación sigue estando pendiente.

---

## TAREA LW-002B — Pantalla Inicio de LuminaWear con estado local simulado

**Fecha:** 2026-10-07

**Cambios realizados:**
- Se centralizaron los textos visibles del diseño en `strings.xml`.
- Se completó el esquema de color confirmado por Figma en `Color.kt` y `Theme.kt`, añadiendo `SurfaceDark` (#101010), `BorderDark` (#363636), `TextPrimary` (#F4F4F0) y `TextSecondary` (#96968E).
- Se creó el componente `HomeScreen.kt` usando la estructura de `TransformingLazyColumn`. Éste despliega:
  - Encabezado con "MI CASA DEMO".
  - Contador centralizado formateado con acento lima.
  - Botón principal de "Apagar todo" adaptativo al estado.
  - Tarjeta de accesos "POR LA HORA" y botones secundarios simulados ("Habitaciones", "Escenas", etc.), deshabilitados intencionalmente sin callbacks vacíos para esperar funcionalidad futura.
- En `MainActivity.kt`, se sustituyó la pantalla de ejemplo genérica por `HomeScreen`.
- Se integró `rememberSaveable` en `MainActivity` para gestionar el estado del contador de luces (de 5 encendidas a 0), y pasar el estado y sus callbacks como parámetros a la UI separando la lógica del componente visual.
- Se mantuvieron previews predefinidas que ilustran la interfaz con las luces parcialmente encendidas y apagadas por completo.

**Verificaciones realizadas:**
- **(2026-10-07):** Compilación exitosa verificada usando `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace` (`BUILD SUCCESSFUL in 40s; 36 tareas ejecutadas`).
- Se corrigió un detalle de compilación respecto a la función de extensión `SurfaceTransformation` aplicándola a través del scope (`scope.SurfaceTransformation(...)`) debido a la naturaleza de la API de Material 3 para Wear OS.
- **Pendiente de comprobación:** La ejecución en emulador no se ha efectuado, por lo cual la correcta visualización e interacción con el diseño no ha sido comprobada visualmente más allá de los estándares sintácticos de Jetpack Compose.

---

## TAREA LW-003 — Habitaciones, detalle de habitación y estado compartido de luces

**Fecha:** 2026-10-07

**Cambios realizados:**
- Se crearon los modelos estáticos en `RoomsData.kt` para 3 habitaciones y 8 luces, con base en el diseño (Sala, Dormitorio y Estudio) con identificadores fijos y configuraciones base.
- Se modificó `MainActivity.kt` centralizando el estado (`lightsOnSet`) en una cadena mapeada (`living_plafon,living_lampara,...`) que sobrevive recreaciones mediante `rememberSaveable`.
- Se introdujo navegación local basada en estados simples (`currentScreen` y `selectedRoomId`), coordinando `HomeScreen`, `RoomsScreen` y `RoomDetailScreen`.
- Se crearon las vistas `RoomsScreen.kt` y `RoomDetailScreen.kt`, las cuales muestran los contadores dinámicos calculados a partir de los datos únicos ("2 de 3 encendidas", "Encendida · 72 %"). 
- Se conectó el botón simulado de "Habitaciones" en el inicio y los botones "Inicio" y "Habitaciones" en los retrocesos. Además, se añadió soporte para el botón o gesto del sistema vía `BackHandler` (el gesto real `SwipeToDismiss` nativo de Wear OS sigue pendiente).
- La gestión de pantallas se movió a `WearApp.kt` para dejar `MainActivity` como un punto de entrada limpio, integrando además `rememberSaveableStateHolder` de manera que cada pantalla mantenga su propio estado de desplazamiento sin compartir un `TransformingLazyColumnState` único ni reiniciarlo accidentalmente.
- Se separaron los textos "MI CASA" y "DEMO" en dos recursos visuales independientes según las correcciones del header y se ajustó el height de los botones mediante la función correcta `heightIn(min = 48.dp)` ubicando el padding por fuera para no recortar la altura real del botón.

**Verificaciones realizadas:**
- **(2026-10-07):** Compilación verificada exitosamente usando `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace` (`BUILD SUCCESSFUL in 17s; 36 tareas: 1 ejecutada, 35 up-to-date`).
- Todos los componentes y previews pre-configurados en `WearApp.kt` han sido organizados reflejando exactamente el contador inicial sin distorsiones ficticias.
- **Pendiente:** Compilación verificada; renderizado e interacción pendientes. La ejecución en emulador o AVD físico no ha podido ser comprobada hasta este momento.
