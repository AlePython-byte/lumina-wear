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

---

## TAREA LW-004 — Control individual de luces y brillo simulado

**Fecha:** 2026-10-07

**Cambios realizados:**
- Se amplió la persistencia del estado en `LightStateHolder.kt` mediante `rememberSaveable` serializado, registrando el estado de encendido y un porcentaje de intensidad por cada luz y reaccionando a la manipulación en tiempo real.
- Las luces predeterminadamente apagadas se instanciaron con 50% de brillo guardado, lo que les permite recordar un nivel cuando se les encienda.
- Se habilitó la lista de luces por habitación en `RoomDetailScreen.kt`, conectando con el destino `LIGHT_CONTROL` bajo `WearApp.kt`. Se proveen previews que demuestran el filtrado real.
- Se implementó `LightControlScreen.kt` como la interfaz individual de las luces que dibuja un dial de brillo semicircular con un arco usando `Canvas` nativo, excluyendo imágenes quemadas.
- Esta pantalla permite subir o bajar brillo en fracciones de 10 (con límites de rango) y provee de las respuestas semánticas elementales en Compose sin acoplar lógicas exclusivas del Wear OS.
- Todos los textos complementarios (ej. "Bajar brillo", "Volver a Sala") han sido inyectados al archivo `strings.xml`.

**Verificaciones realizadas:**
- **(2026-10-07):** Compilación verificada exitosamente usando `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace` (`BUILD SUCCESSFUL in 1m 6s`).
- Todos los componentes renderizan adecuadamente sus previews en el árbol composable sin dependencias extrañas y manteniendo la estabilidad del Canvas.
- **Pendiente:** Al igual que en todos los pasos previos, la renderización visual en emulador o reloj físico está pendiente. El estado y la gestión visual en los rangos y el control fueron construidos en base a la inspección sintáctica, por lo cual la validación real en pantalla dependerá de que la interacción en emulador o AVD se reactive.

---

## TAREA LW-005 — Cierre de defectos LW-004

**Fecha:** 2026-10-18

**Defectos corregidos:**

- **D1 (Brillo con luz apagada):** `setBrightness` en `LightStateHolder.kt` ahora retorna inmediatamente si `current.isOn` es falso. No hay llamadores que invocaran `setBrightness` antes de encender la luz. `turnOffAll()` y `setLightOn()` ya preservaban el brillo correctamente.

- **D2 (Tamaños fijos en LightControlScreen):** El `Box` ya no tiene `.height(120.dp)` y envuelve su contenido. El `Canvas` usa `fillMaxWidth().widthIn(max = 180.dp).aspectRatio(2f)`, conservando el tamaño de referencia en relojes grandes y adaptándose a los pequeños. El arco calcula `inset = strokeWidth / 2` y `arcSize = Size(W - strokeWidth, W - strokeWidth)` para que el trazo no se recorte en pantallas circulares. Los botones −/+ usan `.widthIn(min = 56.dp).heightIn(min = 48.dp)`.

- **D3 (Accesibilidad del indicador):** El `Column` del indicador usa `.semantics(mergeDescendants = true)`, evitando la doble lectura del valor. La descripción de estado apagado usa el nuevo string `light_control_off_accessible` ("Luz apagada") en lugar del símbolo "—". Los botones −/+ ya tenían `contentDescription` con los strings `light_control_decrease`/`light_control_increase`. El botón de encendido/apagado muestra texto visible, por lo que no requiere `contentDescription` redundante.

- **D4 (Textos en Kotlin):** El porcentaje usa el nuevo string `light_control_brightness_pct` (`%1$d%%`, sin espacio como en Figma), derivado como `brightnessDesc` en el scope composable. Los símbolos −/+ usan los nuevos strings `btn_decrease` y `btn_increase`.

- **D5 (Navegación con IDs inválidos):** La pantalla `LIGHT_CONTROL` con `light == null` navega a Habitaciones dentro de `LaunchedEffect` y no renderiza nada en el frame de transición. La pantalla `ROOM_DETAIL` con `room == null` hace lo mismo hacia Habitaciones. El `onBackClick` desde una luz ahora también actualiza `selectedRoomId = light.roomId` antes de navegar. El `BackHandler` para `LIGHT_CONTROL` también deriva la habitación de `light.roomId`. `ROOM_DETAIL` válido usa `room.nameRes` directamente sin fallback.

- **D6 (Saver de LightStateHolder):** El `restore` parte del estado inicial por defecto (misma lógica que `rememberLightStateHolder`) y sobrepone solo las entradas que superan todas las validaciones: `parts.size >= 3`, `isOnFlag` solo "0"/"1", `brightness` con `toIntOrNull()` en rango 1–100, e `id` existente en `appLights`. Ninguna luz puede quedar sin estado.

**Verificaciones realizadas:**
- **(2026-10-18):** `BUILD SUCCESSFUL in 48s; 36 tareas: 16 ejecutadas, 20 up-to-date` — `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`.
- **Histórico:** durante la sesión inicial de la tarea el emulador `emulator-5554` estaba disponible (`device`) pero se desconectó antes de poder instalar, por lo que en ese momento no se tomaron capturas. Esa limitación ya no está vigente: ver instalación y pruebas manuales más abajo.
- **Instalado:** sí, en el emulador (instalado por Alejandro).
- **Probado manualmente por Alejandro (2026-10-18):**
  - Recorrido Inicio → Habitaciones → Sala → luz → +/− → apagar → volver: OK.
  - D5: Dormitorio → Techo → atrás vuelve a Dormitorio: OK.
  - Capturas del arco con la luz encendida (80%) y apagada: extremos redondeados completos y sin recortes en pantalla redonda: OK.
  - Volcado de accesibilidad: un solo nodo "80 %" para el indicador y descripciones "Bajar brillo"/"Subir brillo": OK.

**Observaciones:**
- El número del dial se ve "80 %" con espacio (Figma: "80%") y la descripción accesible no incluye la palabra "Brillo". Ambos se corrigen en LW-006.
- La hora del sistema se superpone al arco. Se revisa en LW-011.

**Afirmaciones previas sin demostración detectadas en avances.md:**
- LW-004: "Todos los componentes renderizan adecuadamente sus previews en el árbol composable" — formulado como hecho verificado, pero en ninguna tarea se registró apertura del panel de previews en Android Studio ni renderizado real.
- LW-001 a LW-004: el emulador figura como "pendiente" en todos los pasos, nunca como completado.

**Pendientes:**
- TalkBack real.
- Texto ampliado (font_scale 1.3).
- Tamaños de pantalla 192 dp y 240 dp.


---

## TAREA LW-006 — Textos visibles a recursos (español por defecto + inglés)

**Fecha:** 2026-10-18

**Implementado:**
- Auditoría: no hay literales visibles en `.kt`. Los literales restantes son IDs estables (`"living_plafon"`, `"living_room"`...), claves de navegación y el formato interno del saver. No se migró nada desde Kotlin.
- Nuevo `res/values-en/strings.xml` con las mismas claves y placeholders que `values/strings.xml`. Nombres de habitaciones y luces salen de `nameRes` en ambos idiomas.
- `lights_on_subtitle` pasó de `<string>` a `<plurals>` (`luz encendida` / `luces encendidas`; `light on` / `lights on`) y `HomeScreen.kt` usa `pluralStringResource` con `lightsOn`.
- `room_lights_counter` se mantiene como string con placeholders (`%1$d de %2$d encendidas`), sin concatenar fragmentos.
- Dial de brillo: el número grande ya usaba `light_control_brightness_pct` (`%1$d%%`, sin espacio). El Text y el `contentDescription` compartían la misma variable; ahora `brightnessText` (pct) es el texto visible y `brightnessDesc` (`light_control_brightness_a11y`: "Brillo, %1$d por ciento" / "Brightness, %1$d percent") es el `contentDescription`, con `semantics(mergeDescendants = true)` intacto.
- `light_control_title` sí se usa (`LightControlScreen.kt`, encabezado "luz · habitación"), por lo que se conserva en ambos archivos.
- `app_name` está en recursos; el Manifest no se editó.

**Claves finales:** 44 por idioma (43 `<string>` + 1 `<plurals>`).

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL in 42s` con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`.
- Preview renderizado: no.
- Instalado: no.
- Probado: no (lo prueba Alejandro).

**Verificaciones:** comparación por script de `values/` y `values-en/`: mismo conjunto de 44 claves y mismos placeholders por clave.

**Sin migrar / notas:**
- Símbolos `−`, `+`, `—` y `DEMO` / `LuminaWear`: iguales en ambos idiomas, se mantienen como recursos.
- Los porcentajes de listas, tarjetas y etiquetas ("Sala · 30 %") conservan el espacio, como en Figma.
- Sobre el "80 %" con espacio visto en el emulador: el código ya usaba el string sin espacio, así que el APK instalado probablemente era anterior. Falta confirmarlo en el emulador con esta compilación.

---

## TAREA LW-007 — Design System en código: tokens, tema claro/oscuro y tipografía

**Fecha:** 2026-10-18

**Implementado:**
- Nivel 1: `LuminaPalette` en `theme/Color.kt`, único archivo con `Color(0x...)` (17 colores de Figma "Lumina / Primitives").
- Nivel 2: `LuminaColors` + `LocalLuminaColors` + `LuminaTheme.colors` en `theme/Tokens.kt`, con `LightLuminaColors` y `DarkLuminaColors`. Mapeo al `ColorScheme` de Wear Material 3 en `Theme.kt` (primary=accent, onPrimary=onAccent, background=bg, surfaceContainer=surface, onSurface=text, onSurfaceVariant=muted, outline=border, error=danger).
- Escala: `LuminaSpacing`, `LuminaRadius`, `LuminaSizes.touch`, `LuminaBorder.width`.
- Nivel 3: `theme/ComponentTokens.kt` con ActionButton, StatusChip, RoomControl, SceneCard, LightToggle, NavigationItem, BrightnessDial y StepButton, solo con tokens semánticos.
- Tipografía: `LuminaTypography` en `theme/Type.kt` (display, title, body, button, label, time); familia en `LuminaFontFamily` (SansSerif hasta LW-007b). También alimenta la `Typography` de Material.
- Tema: `LuminaWearTheme(themeMode = ThemeMode.LIGHT)`, enum `ThemeMode { LIGHT, DARK }`. Estado `themeMode` en `WearApp` (rememberSaveable, arranca en LIGHT). Selector y persistencia: LW-008.
- Pantallas: Inicio, Habitaciones, detalle de habitación y control de luz usan solo tokens. Textos de acento ("DEMO", contador de Inicio, "POR LA HORA", estado "Encendida", arco) pasan de `primary` (lime) a `accentContent`. `StepButton` extraído como composable privado.
- Previews claro y oscuro de las 4 pantallas en `WearApp.kt`. Las previews anteriores quedan en tema claro (predeterminado).
- `docs/design-tokens.md` con tablas de los 3 niveles y mapa Atomic Design.

**Contraste (WCAG, calculado por script):**

| Par | Claro | Oscuro |
|---|---|---|
| text/bg | 16.74 | 19.05 |
| muted/bg | 6.52 | 7.05 |
| muted/surface | 5.94 | 6.39 |
| accentContent/bg | 6.66 | 17.82 |
| onAccent/accent | 17.82 | 17.82 |
| warning/bg | 6.68 | 12.22 |
| danger/bg | 6.57 | 6.84 |
| border/bg (informativo) | 3.76 | 1.74 |

**Decisiones propias:**
- Estados sin valor en Figma: disabled de ActionButton, NavigationItem y StepButton = surface + muted; pressed se aplica con `interactionSource` en "Apagar todo" y "Encender".
- Texto regular de 12 sp (subtítulos, estados, etiqueta del dial) usa el estilo `time`, el único Regular 12/16 de Figma. Los títulos de 14 sp en negrita usan `button`. Los símbolos −/+ pasan de 24 sp a `title` (20 sp). "Descanso" pasa de 20 sp Regular a `title` (Bold).
- Fuera de escala (como tokens de componente): alto mínimo de fila 60 dp (RoomControl), ancho mínimo de −/+ 56 dp (StepButton), ancho máximo del dial 180 dp (BrightnessDial).
- La pista del arco pasa de onSurfaceVariant al 30% a `border`.

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL in 40s` con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`.
- Preview renderizado: no.
- Instalado: no.
- Probado: no (lo prueba Alejandro).

**Pendientes:**
- Archivo Roboto Flex (LW-007b).
- Selector de tema y persistencia (LW-008).
- Revisar en emulador ambos temas, con atención al contraste de border en oscuro (1.74:1, decorativo).

**Retoque LW-007c (2026-10-18):** el reloj del sistema ahora sale de `LuminaTimeText` (`theme/Theme.kt`), con color `muted` y tamaño del estilo `time`, pasado a cada `AppScaffold` en `WearApp.kt`. Contraste: claro 6.52:1 (muted/bg), oscuro 7.05:1. En `values-en/strings.xml` los porcentajes pasan a "72%" sin espacio (el español no cambia); mismas 44 claves en ambos idiomas. Compilado (`BUILD SUCCESSFUL in 58s`); sin instalar ni probar. El reloj usa el `TextStyle` `time` completo (incluida Roboto Flex) mediante `CurvedTextStyle(TextStyle)`.

---

## TAREA LW-007b — Conectar la fuente Roboto Flex

**Fecha:** 2026-10-18

**Implementado:**
- El archivo estaba como `res/font/roboto_flex.ttf.ttf` (nombre inválido como recurso por el punto). Se renombró a `roboto_flex.ttf` sin modificar su contenido (1 684 624 bytes, ≈1,6 MB).
- `LuminaFontFamily` en `theme/Type.kt` pasó de `FontFamily.SansSerif` a Roboto Flex variable: tres `Font(R.font.roboto_flex, weight, variationSettings = FontVariation.Settings(FontVariation.weight(...)))` para Regular 400, Bold 700 y Black 900. Se usa `@OptIn(ExperimentalTextApi::class)`.
- Sin cambios de versiones: `FontVariation` está disponible con el BOM 2024.09.00 y `minSdk = 30` (las variaciones de fuente se aplican desde la API 26).
- Verificación por grep: `FontFamily`, `TextStyle(`, `fontSize`, `fontWeight`, `fontFamily` y `.sp` solo aparecen en `Type.kt`. Los 27 `Text(` de las 4 pantallas usan `style = LuminaTypography.*`.
- `docs/design-tokens.md`: nueva sección "Tipografía y licencia" (Roboto Flex, SIL OFL 1.1, archivo y ubicación del FontFamily). La licencia ya estaba en `docs/licenses/OFL.txt`.

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL in 40s` con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`.
- Preview renderizado: no.
- Instalado: no.
- Probado: no (lo prueba Alejandro).

**Límites / observaciones:**
- Solo se usan los ejes de peso (400, 700, 900). No se aplican otros ejes de Roboto Flex (ancho, óptico, etc.).
- Que el peso 900 se vea distinto del 700 en el emulador no está verificado.
- `FontVariation` se marca como API experimental de texto (`ExperimentalTextApi`); por eso el `@OptIn`.

---

## TAREA LW-008 — Pantalla Ajustes: tema e idioma con persistencia

**Fecha:** 2026-10-18

**Implementado:**
- `state/AppPreferences.kt`: `SharedPreferences` `luminawear_prefs`, claves `theme_mode` (light/dark) y `language` (es/en); ausente o inválido → LIGHT y ES. Escritura con `apply()`. Enum `AppLanguage`.
- `MainActivity`: lee las preferencias antes de `setContent`; `attachBaseContext` envuelve el contexto con la `Configuration` del idioma guardado (`createConfigurationContext`); al elegir otro idioma guarda y llama a `recreate()`.
- `WearApp`: recibe tema inicial, idioma y callbacks; `themeMode` sigue siendo un solo estado (rememberSaveable). Nuevo `ScreenType.SETTINGS`; el botón atrás vuelve a Inicio.
- `settings/SettingsScreen.kt`: encabezado DEMO + AJUSTES, opciones Claro/Oscuro, ayuda "Desliza para más ↓", sección IDIOMA, opciones Español/English, botón Inicio y nota. Las opciones tienen `selectable(role = RadioButton)`, 60 dp mínimo y botón de 48 dp.
- `NavigationItemTokens`: estado Selected (borde y detalle `accentContent`) y medidas de opción.
- Inicio: nuevo ítem "Ajustes" (`NavigationItem`) al final de la lista.
- Textos: 8 claves nuevas traducibles en `values/` y `values-en/`; `settings_language_es` y `settings_language_en` son `translatable="false"` y existen solo en `values/`. En inglés, `help_swipe_more` pasó a "Scroll for more ↓" (también afecta a Inicio).
- Previews de Ajustes en claro y oscuro. Docs: `design-tokens.md` (Selected y Preferencias).

**Verificaciones:**
- Claves: 52 traducibles en `values/` = 52 en `values-en/` (más 2 `translatable="false"` solo en `values/`). Grep de " %%" en `values-en`: vacío.
- `Color(0x` solo en `theme/Color.kt`; sin literales visibles ni estilos sueltos fuera de `theme/`.

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL in 51s` con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`.
- Preview renderizado: no.
- Instalado: no.
- Probado: no (lo prueba Alejandro).

**Sin verificar:** que `recreate()` conserve la pantalla y las luces; que `stringResource` y `pluralStringResource` tomen el idioma nuevo; la lectura de TalkBack ("Claro, Predeterminado, seleccionado"); la posición del ítem "Ajustes" en Inicio respecto a Figma (no se leyó el archivo).


---

## TAREA LW-009a — Escenas, sugerencia "Por la hora" y Deshacer

**Fecha:** 2026-10-18

**Implementado:**
- Estados por defecto unificados: `defaultLightStates()` en `LightStateHolder.kt` la usan `rememberLightStateHolder` y el Saver (antes la lista estaba duplicada). Mismo estado de antes (5/8; 2/3, 2/3, 1/2). `serializeLightStates` / `parseLightStates` comparten el formato del Saver con el snapshot de Deshacer.
- Modelo `Scene` (relax, read, focus, sleep) en `models/Scenes.kt`, `suggestionForHour(hour)` y `applyScene(...)` (función pura en `state/SceneLogic.kt`). 0% apaga conservando el brillo recordado; si no, enciende con el porcentaje; las demás luces no cambian.
- Pantallas nuevas (`scenes/SceneScreens.kt`): SCENES, SCENE_DETAIL, SCENE_ACTIVE, SUGGESTION. Componentes compartidos nuevos: `components/ActionButton.kt` y `components/NavigationItem.kt` (Ajustes ahora reutiliza este último).
- Deshacer: una sola capa; el snapshot es un `rememberSaveable` de texto (sobrevive a `recreate()`); se descarta con "Inicio" o con atrás desde SCENE_ACTIVE.
- Inicio: tarjeta "Por la hora" dinámica (hora de `LocalTime.now()`, se calcula al crear la pantalla), Habitaciones, Escenas, Ajustes. "Ahora no" oculta la tarjeta durante la sesión. Se quitaron los botones deshabilitados Favoritos y Voz (tareas posteriores); sus textos siguen en recursos.
- Accesibilidad: "ESCENA ACTIVA" con `liveRegion` educada; "Deshacer" con descripción propia; botones de 48 dp y NavigationItem de 60 dp.
- Textos: 23 claves nuevas en ambos idiomas y se eliminó `card_rest` (neto +22: de 52 a 74).

**Verificaciones:**
- Claves: 74 traducibles en `values/` = 74 en `values-en/` (más 2 `translatable="false"` solo en `values/`). Sin " %%" en `values-en`. `Color(0x` solo en `Color.kt`; sin literales visibles ni estilos sueltos fuera de `theme/`.
- Pruebas unitarias: **no se agregaron**. El proyecto no tiene JUnit para pruebas locales (no hay `testImplementation` ni `src/test`) y no se permiten dependencias nuevas. `suggestionForHour` y `applyScene` son puras y están listas para probarse.

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL in 1m 23s` con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`.
- Preview renderizado: no (no se agregaron previews de las pantallas nuevas).
- Instalado: no.
- Probado: no (lo prueba Alejandro).

**Sin verificar:** navegación y Deshacer en el emulador; que el snapshot sobreviva a `recreate()`; lectura de TalkBack de "Escena activa"; el diseño de Figma (no se leyó el archivo).
