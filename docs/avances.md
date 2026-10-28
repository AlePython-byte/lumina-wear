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


---

## TAREA LW-009b — Favoritos y "Restaurar demo"

**Fecha:** 2026-10-19

**Implementado:**
- `state/Favorites.kt`: modelo `Favorite` (luz o escena), parseo/serialización de ids (descarta desconocidos y repetidos), defaults `living_plafon,relax`.
- `AppPreferences.favorites` (clave `favorites`): ausente → defaults; presente y vacía → sin favoritos. `MainActivity` la lee y persiste cada cambio.
- `favorites/FavoritesScreens.kt`: `FavoritesScreen` (lista / un favorito / vacío) y `FavoritesEditScreen`. Nuevos `ScreenType.FAVORITES` y `FAVORITES_EDIT` (rememberSaveable). Tocar "Plafón" abre `LightControlScreen`; "Descanso", `SCENE_DETAIL`; atrás/Volver regresan a Favoritos (`openedFromFavorites`, saveable).
- "Restaurar demo": favoritos por defecto, `defaultLightStates()`, limpia undo y escena seleccionada, y reactiva la tarjeta "Por la hora". No toca tema ni idioma.
- "Añadir plafón" (estado vacío) vuelve a agregar el plafón de Sala.
- Inicio: botón "Favoritos" entre Escenas y Ajustes.
- 12 textos nuevos en ES y EN con las mismas claves. 8 previews (4 pantallas × claro/oscuro).

**Decisiones (Figma no se leyó: el MCP pide autenticación OAuth):**
- Estado "un favorito": ítem + "Quitar <tipo>" + "Restaurar demo" + "Inicio" en la pantalla de lista (sin "Editar").
- Estado vacío: título, texto, "Añadir plafón" e "Inicio" debajo (ajuste LW-009b-fix según Figma: ambos `ActionButton`, 48 dp).
- Con 2+ favoritos "Restaurar demo" no aparece (solo en el estado de un favorito).
- Al quitar el último desde Editar se vuelve al estado vacío.
- `SceneHeader` y `CenteredText` pasaron de `private` a `internal` para reutilizarlos.
- Detalle de ítem: "Sala · brillo" / "Sala · escena" (en EN "Living room · brightness/scene").

**Verificaciones:** `Color(0x` solo en `Color.kt`; mismas claves ES/EN (salvo las 2 `translatable="false"`); sin " %%" en `values-en`.

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL in 30s` tras LW-009b-fix (antes 43s) con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`.
- Preview renderizado: no (solo se compilaron).
- Instalado: no (no hay emulador activo: `adb devices` vacío).
- Probado: no.

**Sin verificar:** posición exacta de botones frente a Figma; persistencia tras cerrar/rotar; flujo quitar/añadir/restaurar; contraste en claro/oscuro; TalkBack.


---

## TAREA LW-010a — Hub de luz + conexión de la Lámpara de Sala

**Fecha:** 2026-10-19

**Implementado (app):**
- `data/LightProvider.kt`: interfaz `LightProvider`, `DemoProvider` (éxito sin hacer nada) y `HubProvider` (`HttpURLConnection` en `Dispatchers.IO`, timeouts de 2 s, `org.json`, sin librerías). `LightProviders`: `living_lampara` → hub; las otras 7 → demo.
- `AppPreferences.hubUrl` (clave `hub_url`, sin UI; por defecto `http://10.0.2.2:8787`).
- `state/HubSync.kt`: `HubStatus` (IDLE, DEBOUNCING, CONNECTING, ERROR) y formato `"1:45"` para orden y rollback.
- `WearApp`: `LightStateHolder` sigue siendo la fuente de verdad. Encender/apagar la Lámpara envía de inmediato; los botones −/+ esperan 300 ms de inactividad. Al empezar una ráfaga se guarda el último estado confirmado; si falla, la luz vuelve a él. El efecto vive fuera de las pantallas y el estado es `rememberSaveable` (tras recrear en CONECTANDO se reenvía la orden).
- `lights/HubStatusScreens.kt`: CONECTANDO ("Buscando tu hogar", "Espera un momento…", Cancelar) y NO SE PUDO ("Sin cambios", "La luz no respondió.", Reintentar primario, Volver). Cabeceras con `liveRegion` educada. Atrás = Cancelar / Volver.
- Manifest: solo `INTERNET` y `networkSecurityConfig`. `res/xml/network_security_config.xml`: cleartext prohibido salvo `10.0.2.2`.
- 8 textos nuevos en ES y EN. 4 previews (conectando y error, claro y oscuro).

**Implementado (hub):** `hub/hub.py` (Python 3, estándar, `0.0.0.0:8787`), `/health`, GET/POST `/api/lights/living_lampara`, 404 para otros ids, 400 para JSON inválido o fuera de rango, página `/` con bombillo que consulta cada segundo. Modo `tuya` con `tinytuya` (import perezoso) y `hub/config.json` → **SIN PROBAR**. `config.example.json`, `README.md`, `hub/config.json` en `.gitignore`.

**Decisiones:**
- Figma no se leyó (MCP sin autenticar): textos de la tarea. "..." se escribió como "…" (evita el aviso de lint). EN: "Connecting", "Finding your home", "Couldn’t connect", "No changes".
- Las pantallas de estado del hub se muestran por encima de la pantalla actual (no son `ScreenType`), así un cambio pendiente al salir del control no se pierde. "Volver" lleva al control de la Lámpara.
- "Cancelar" restaura el estado anterior e ignora la respuesta, pero si el POST ya llegó al hub, el hub sí cambia (`HttpURLConnection` no se puede abortar a mitad). Con un hub local la ventana es de milisegundos.
- Con un hub local la respuesta llega muy rápido: CONECTANDO puede verse como un parpadeo. Se dejó según la tarea; revisar en el emulador.
- Escenas, "Apagar todo", Deshacer y "Restaurar demo" cambian la Lámpara solo en local (no llaman al hub), así que la app y el bombillo pueden quedar distintos hasta la próxima orden manual.
- Un reloj real necesitará la IP del PC y otra regla en `network_security_config`; queda fuera de esta tarea (no se suben IPs privadas).

**Verificaciones:**
- Hub en modo simulado con `curl.exe`: `/health` 200 `simulated`; GET 200 (on, 45); POST válido 200 y GET posterior refleja el cambio; brillo 101, `on` no booleano, brillo booleano, JSON roto y POST sin cuerpo → 400; POST/GET a otro id y ruta desconocida → 404; `GET /` 200 con la página.
- Claves ES = EN (salvo las 2 `translatable="false"`). `Color(0x` solo en `Color.kt`. Sin IPs privadas en Git. Manifest: solo los 2 cambios autorizados.

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL in 1m 8s` con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`, sin warnings `w:` de Kotlin.
- Preview renderizado: no (solo compiladas).
- Instalado: no (no había emulador: `adb devices` vacío).
- Probado: solo el hub simulado con curl. La app no se probó: ni la conexión al hub desde el emulador, ni el error con el hub apagado, ni Reintentar, ni la página del bombillo en el navegador. Modo tuya sin probar.


---

## LW-010a-fix — Permiso de red local (Android 17)

**Fecha:** 2026-10-19

**Causa raíz:** el emulador es Android 17 (API 37) y la app tiene `targetSdk 37`. El firewall eBPF bloquea la red local (`LocalNetAccessKey{… remoteAddress=10.0.2.0/120}: false`) a los UID sin `PERMISSION_ACCESS_LOCAL_NETWORK`. La app lo tenía solo con INTERNET → `SocketTimeoutException: failed to connect to /10.0.2.2 (port 8787) … after 2000ms`. Desde `adb shell` (UID con el permiso) el mismo `nc` respondía 200; con `run-as` de la app, timeout. `adb reverse` a `127.0.0.1` tampoco servía.

**Cambios:**
- Manifest: `ACCESS_LOCAL_NETWORK` (autorizado por Alejandro). Total en el Manifest: INTERNET, `networkSecurityConfig` y `ACCESS_LOCAL_NETWORK`.
- `data/LocalNetworkPermission.kt`: `needsLocalNetworkPermission()` (solo API ≥ 37).
- `WearApp`: `rememberLauncherForActivityResult(RequestPermission)`. Nuevo `HubStatus.PERMISSION`: antes de enviar (toque, fin del debounce o Reintentar) se pide el permiso si falta. Negado → rollback y estado de error; se vuelve a pedir en la siguiente orden o en Reintentar. Log `HubPermission`.
- `HubProvider`: `Log.w` con la excepción y con códigos HTTP no 2xx.
- Sin textos nuevos (el diálogo es del sistema). `hub/README.md`: sección "Antes del demo".

**Importante para el demo:** conceder "Dispositivos cercanos" la primera vez. Tras dos negativas Android no vuelve a mostrar el diálogo: activarlo en Ajustes o con `adb shell pm grant com.alejandro.luminawear android.permission.ACCESS_LOCAL_NETWORK`.

**Estados:**
- Compilado: sí (`assembleDebug` OK).
- Instalado: sí, en `emulator-5554` (Wear_OS_Small_Round, Android 17).
- Probado en el emulador (con logcat y log del hub):
  - Permiso negado: diálogo → "Don’t allow" → `HubPermission: ACCESS_LOCAL_NETWORK concedido=false`, pantalla "NO SE PUDO", hub sin cambios, tras Volver la Lámpara sigue encendida al 45%.
  - Se vuelve a pedir y se concede: `concedido=true`, el hub pasa a `on:false`.
  - Encender sin diálogo: 1 POST, hub `on:true`. Tres toques rápidos de + → 1 solo POST, brillo 75.
  - Hub apagado: `W HubProvider: java.net.SocketTimeoutException: failed to connect to /10.0.2.2 (port 8787) …` y pantalla "NO SE PUDO". Hub encendido otra vez + Reintentar → POST 200, hub `on:false, brightness:75`.
  - Plafón: encender/apagar sin diálogo y 0 POST al hub.
  - Página http://localhost:8787 en Chrome: bombillo gris al apagar, amarillo "75 %" al encender.
- Sin probar: "Cancelar" durante Conectando, rotación/recreate con el diálogo abierto, modo tuya.



---

## LW-010a-2 — Escenas y acciones masivas sincronizadas con el hub; "Conectando" sin parpadeo

**Fecha:** 2026-10-19

**Implementado (app):**
- `WearApp.runHubAction()`: escenas (también desde Sugerencia), "Apagar todo", "Deshacer" y "Restaurar demo" se aplican en local y comparan la Lámpara (`living_lampara`) antes y después. Si cambió, se envía **una** orden con su estado final; si no, no se envía nada. Log `HubSync` en ambos casos.
- Si la orden falla (o se niega el permiso, o se pulsa Cancelar): se restauran todas las luces con un snapshot en el mismo formato del de Deshacer (`serializeLightStates`; en una escena es idéntico a `undoSnapshot`), se recupera el Deshacer previo y se vuelve a la pantalla de origen. Se muestra el error existente ("NO SE PUDO / Sin cambios").
- "Reintentar" repite la acción completa (se guarda como `turn_off_all`, `undo`, `restore_demo` o `scene:<id>` en `rememberSaveable`). "Volver" deja al usuario en la pantalla de origen (tras una orden desde el control de la luz sigue yendo al control de la Lámpara).
- El permiso `ACCESS_LOCAL_NETWORK` se pide con el mismo flujo (`startHubRequest`).
- CONECTANDO solo se muestra si el hub tarda más de 400 ms (`HUB_CONNECTING_REVEAL_MS`). Mientras no se ve, Atrás navega normal en vez de cancelar.
- `HubProvider`: `Log.i` con el código HTTP y los ms de cada orden confirmada.

**Implementado (hub):** escucha por defecto en `127.0.0.1` (el emulador llega por `10.0.2.2`). Nueva opción `--host 0.0.0.0` con aviso en consola. `hub/README.md`: sección de `--host`, lista "Antes del demo" y qué envía la app.

**Decisiones:**
- "Restaurar demo" fallido revierte las luces y el Deshacer, pero no los favoritos ni la sugerencia descartada (no dependen del hub).
- Si llega una acción masiva con una orden de brillo aún en espera, se conserva como rollback el último estado confirmado de la Lámpara (igual que en el control de la luz).
- El bombillo Tuya por Wi-Fi no necesita `--host 0.0.0.0`: el hub es quien se conecta al bombillo. Solo un reloj físico lo necesita.
- Escenas actuales: Relax (sala, 30 %) y Dormir (toda la casa, apagar) cambian la Lámpara; Leer (dormitorio) y Enfoque (estudio) no.

**Verificaciones:** sin textos nuevos (ES = EN sin cambios); `Color(0x` solo en `Color.kt`; Manifest, `res/` y Gradle sin cambios.

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL in 49s` con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`, sin warnings `w:` de Kotlin.
- Preview renderizado: no (no hay previews nuevas; las del hub siguen igual).
- Instalado: sí, en `emulator-5554` (Wear_OS_Small_Round, Android 17). Antes figuraba `unauthorized`; se resolvió reiniciando adb (`adb kill-server`). Permiso `ACCESS_LOCAL_NETWORK` ya concedido.
- Probado en el emulador. Hub simulado arrancado por Alejandro, escuchando en `127.0.0.1:8787` (`netstat`), el emulador lo alcanza por `10.0.2.2`. Evidencia: logcat (`HubSync`, `HubProvider`) y un monitor que consulta `GET /api/lights/living_lampara` cada 250 ms (la salida de consola del hub no fue accesible). El reloj del emulador va ~2 h 10 min adelantado respecto al PC.

| Prueba | logcat | Hub después |
|---|---|---|
| Escena Lectura (no toca la Lámpara) | `scene:read: la Lámpara no cambia (1:45), no se envía` | sin cambio (on, 45) |
| Deshacer de Lectura | `undo: la Lámpara no cambia (1:45), no se envía` | sin cambio |
| Escena Descanso | `scene:relax: Lámpara 1:45 → 1:30, se envía` · `HTTP 200 en 101 ms` | on, 30 |
| Deshacer | `undo: Lámpara 1:30 → 1:45, se envía` · `HTTP 200 en 14 ms` | on, 45 |
| Apagar todo | `turn_off_all: Lámpara 1:45 → 0:45, se envía` · `HTTP 200 en 12 ms` | off, 45 |
| Restaurar demo | `restore_demo: Lámpara 0:45 → 1:45, se envía` · `HTTP 200 en 10 ms` | on, 45 |
| Descanso con el hub apagado | `scene:relax … se envía` · `SocketTimeoutException … after 2000ms` | sin hub |
| Reintentar (hub apagado) | repite `scene:relax … se envía` · timeout | sin hub |
| Reintentar (hub encendido) | `scene:relax … se envía` · `HTTP 200 en 31 ms` | on, 30 |

- Cada acción que cambia la Lámpara produjo un solo POST y un solo cambio en el hub.
- Con respuestas de 10–101 ms no apareció CONECTANDO (se pasó directo a la pantalla siguiente). Con el hub apagado sí apareció ("Buscando tu hogar", Cancelar) durante la espera y, a los 2 s, "NO SE PUDO / Sin cambios".
- Rollback con el hub apagado: "Volver" dejó al usuario en el detalle de Descanso, y en Sala quedaron Plafón 72 % y Lámpara 45 % (la escena los habría dejado en 30 %).
- Tras el Reintentar exitoso, "Deshacer" volvió a enviar 45 % (`HTTP 200 en 13 ms`): la app y el hub quedaron en el estado de demo.
- Sin probar: "Cancelar" durante CONECTANDO en una acción masiva; recrear la Activity a mitad de una orden; `--host 0.0.0.0` (solo `--help`); el umbral exacto de 400 ms (las capturas cuestan ~0,3 s cada una, así que solo se confirma que no aparece con respuestas rápidas y sí con la espera de 2 s); modo tuya.
- Detalle visto: al volver a "NO SE PUDO" la lista conserva el desplazamiento de la vez anterior (el título puede quedar fuera de pantalla). Ya pasaba antes de esta tarea; no se cambió.



---

## LW-010c — Modo cozylife: el hub controla el bombillo real

**Fecha:** 2026-10-25

**Implementado (hub, commit `f818e4c`):**
- `hub/cozylife.py`: cliente propio del protocolo local de CozyLife (TCP 5555, JSON por línea), solo librería estándar. Lee la IP de `hub/config.json` → `cozylife` (ignorado por Git).
- `hub/hub.py --mode cozylife`: GET consulta el estado real del bombillo; POST envía la orden. Si el bombillo no responde, 502.
- `hub/tools/cozylife_probe.py`: prueba directa del bombillo sin el hub (`estado`, `--on`, `--off`, `--brightness N`). Si falla, indica la fase: conexión y consulta inicial, envío de la orden o lectura posterior del estado.
- Plazo por petición del cliente: de 2 s a 5 s (`TIMEOUT_S = 5.0`), porque el bombillo llega a tardar ~750 ms en el ping.

**Cómo se encontró el bombillo:** la primera IP candidata no respondió en el puerto 5555 (timeout). Alejandro identificó la correcta por la MAC (coincide con el ID del dispositivo en la app CozyLife). El `did` que reporta el bombillo termina en esa MAC. No se escaneó la red ni se contactó otra IP.

**Probado con el bombillo real (probe, un paso a la vez, Alejandro mirando el bombillo):**

| Paso | Bombillo reporta | Alejandro ve |
|---|---|---|
| estado | encendido, 100 % | encendido al máximo |
| `--off` | apagado (brillo guardado 100 %) | se apagó |
| `--on` | encendido, 100 % | encendió al máximo |
| `--brightness 20` (plazo de 2 s) | timeout tras la consulta inicial | — |
| `--brightness 20` (plazo de 5 s) | ya estaba en 20 % antes de enviar · confirmada · 20 % | al 20 % |
| `--brightness 100` | confirmada · 100 % | subió al máximo |

- El primer `--brightness 20` sí se aplicó: el timeout fue al esperar la confirmación o la lectura posterior. Este caso motivó LW-010d.

---

## LW-010d — Robustez del modo cozylife y prueba con la app

**Fecha:** 2026-10-25

**Implementado (hub):**
- **Órdenes que llegan sin confirmación.** La Wi-Fi del bombillo tiene cortes de varios segundos y a veces aplica la orden sin que llegue la confirmación. Un POST tiene un tope total de 8 s (`ORDER_DEADLINE_S`):
  1. Envío (espera de confirmación 3,5 s). Si confirma, lee el estado para verificar; si esa lectura falla, responde 200 con el estado pedido y aviso en consola.
  2. Sin confirmación: **no reenvía enseguida**. Lee el estado hasta 2 veces (2 s cada una). Si ya está lo pedido → 200.
  3. Si responde con otra cosa → reenvía **una sola vez** (valores absolutos, idempotente) y vuelve a verificar.
  4. Si no responde, o sigue distinto → 502.
  - Consola: `orden confirmada`, `orden verificada sin confirmación`, `orden reenviada (confirmada | verificada sin confirmación)`, `orden fallida (motivo)`.
- **Keepalive.** Un hilo de fondo consulta el estado cada 8 s (`KEEPALIVE_S`, lectura de 2,5 s como máximo) sin enviar órdenes, y guarda el último estado conocido con su hora. Tras 2 fallos seguidos marca "no responde"; cuando el bombillo vuelve, se recupera solo.
- `GET /api/status` (nuevo): último estado conocido, sin contactar al bombillo. La página lo consulta cada segundo y muestra "Última respuesta hace N s" y "El bombillo no responde". `GET /api/lights/living_lampara` sigue consultando al bombillo directamente (502 si falla).
- En consola solo se registran los cambios de estado, la pérdida o recuperación de la conexión, el resultado de cada orden y las peticiones de la app (no las de la página).
- Ctrl+C cierra el servidor, detiene el keepalive y cierra la conexión con el bombillo.
- `hub/cozylife.py`: cada petición acepta su propio plazo, y la espera mientras termina otra petición (por ejemplo un keepalive) cuenta dentro de ese plazo. Así una orden no puede pasar de 8 s.

**Implementado (app):** `HubProvider.TIMEOUT_MS` sube de 2 000 a 10 000 ms (más que los 8 s del hub). Sin cambios en "Conectando" (`HUB_CONNECTING_REVEAL_MS = 400`) ni en "Cancelar". Sin textos nuevos, sin cambios en Manifest, `res/` ni Gradle.

**Decisiones:**
- Primero se subió la app a 6 s. Durante las pruebas apareció una orden aplicada sin confirmación (502 con el bombillo ya en 20 %). Por eso se agregó la verificación con reenvío único, el hub pasó a 8 s y la app a 10 s.
- La página ya no consulta al bombillo cada segundo: lee el estado que guarda el keepalive.

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL in 28s` con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`, sin warnings `w:` de Kotlin.
- Instalado: sí, en `emulator-5554` (`adb install -r` → `Success`; `ACCESS_LOCAL_NETWORK` ya concedido).
- Probado: ver abajo.

**Pruebas sin red (bombillo falso, script fuera del repo):** 7 casos del flujo de orden, todos con el resultado esperado: confirmada; confirmada con verificación fallida; sin confirmación pero aplicada; no aplicada + reenvío confirmado; no aplicada + reenvío aplicado sin confirmar; sin respuesta → 502 (1 envío); sigue distinto tras reenviar → 502 (2 envíos). Con esperas reales, el peor caso tarda 8,0 s.

**a) Hub + `curl.exe` con el bombillo real (Alejandro confirma cada paso):**

| Paso | Respuesta | Consola del hub | Bombillo |
|---|---|---|---|
| GET | 200 · on, 100 · 0,25 s | — | encendido al máximo |
| POST encendido 100 | 200 · 0,40 s | — (sin cambio) | sigue al máximo |
| POST apagado | 200 · 0,36 s | `bombillo: apagada` | se apagó |
| POST brillo 20 (código anterior) | 502 · 5,2 s | luego el keepalive: `encendida 20 %` | al 20 % (aplicada sin confirmación) |
| POST brillo 100 (código nuevo) | 200 · 3,03 s | `orden confirmada: encendida 100 %` | subió al máximo |
| POST brillo 0 | 200 · `brightness: 1` · 1,21 s | `orden confirmada: encendida 1 %` | encendido muy tenue |

**b) Bombillo sin corriente:**
- GET → 502 en 5,25 s. POST → 502 en 8,22 s (`sin confirmación y el bombillo no responde`, 1 solo envío). Consola: `se perdió la conexión` y `orden fallida`.
- `/api/status` → `responding: false`. Alejandro confirma que la página decía "El bombillo no responde".
- Al volver la corriente: ping en segundos, y `bombillo: conexión recuperada` ~1 min después, sin reiniciar el hub. El bombillo volvió con su último estado (1 %, tenue), y GET → 200.

**c) App en el emulador → bombillo real.** Alejandro usó "Apagar todo", encendió la Lámpara y ajustó el brillo varias veces; el bombillo siguió cada cambio. Logcat: 11 órdenes `HubProvider … HTTP 200`, de 138 a 3 160 ms. Consola del hub: las 11 `orden confirmada` (ninguna necesitó verificación ni reenvío).

**Cierre:** `hub.py --mode cozylife` en su propio grupo de procesos; CTRL_BREAK convertido en `KeyboardInterrupt` (el mismo camino que Ctrl+C) → `Cerrando el hub…`, código 0 en 0,56 s, con el keepalive activo.

**Incidencias durante las pruebas:**
- Al conectar el PC por cable, quedó en otra red (luego en el punto de acceso del celular) y el bombillo dejó de ser alcanzable. Al volver a la Wi-Fi de la casa, el bombillo respondía al ping pero rechazaba el puerto 5555. Se resolvió cortándole la corriente. Quedó en el README ("Si el bombillo no responde").
- Con el PC en la red correcta, el keepalive registró 3 pérdidas de ~10 s en 6 min, recuperadas solas: la Wi-Fi del bombillo es inestable.

**Sin probar:**
- Ctrl+C real en una terminal (se probó con CTRL_BREAK convertido en `KeyboardInterrupt`).
- "Cancelar" durante "Conectando" con el bombillo real.
- Los casos "verificada sin confirmación" y "reenviada" con el bombillo real: no ocurrieron con el código nuevo; solo están probados con el bombillo falso.
- Que "Conectando" se viera en las órdenes de 1,5–3 s (no se registró en pantalla).
- Modo tuya.

**Observación:** al final del ajuste de brillo, la app envió dos órdenes seguidas con el mismo valor (65 %), y la segunda salió antes de que respondiera la primera. Ambas dieron 200 y no causaron problema, pero sobra una. No se investigó en esta tarea.

---

## LW-010b — Asistente de luces (texto y frases rápidas)

**Fecha:** 2026-10-25

**Qué hice:**
- Agregué el acceso **Asistente** en Inicio, entre Favoritos y Ajustes. No había un acceso previo: el botón "Voz" se había quitado en LW-009a. La voz queda fuera de esta tarea.
- **Pantalla del asistente:** 5 frases rápidas (Apagar todo, Modo lectura, Modo descanso, Encender la lámpara, Lámpara al 50 %), cada una con su detalle ("Dormitorio · 70 %"), y la opción **Escribir**. Las frases pasan por las mismas reglas que el texto escrito.
- **Escribir:** campo de texto con tope de 200 caracteres, contador, Enviar y Volver. Usé `BasicTextField`, que ya venía con las dependencias. La tecla Enter del teclado también envía.
- **Reglas locales** (`AssistantRules.kt`, funciones puras sin Android). Entienden:
  - apagar/encender todo;
  - las 4 escenas por nombre o sinónimo (descanso/relax, lectura/leer, concentración/enfoque, buenas noches/dormir);
  - encender/apagar una o varias luces por nombre, en español o inglés;
  - "al N %", "N por ciento", "to N" para el brillo.
  - No distinguen acentos ni mayúsculas. Si no reconocen la frase devuelven `null`.
- **Servidor:** si las reglas no entienden y hay URL y token, consulto el servidor con `HttpURLConnection` en `Dispatchers.IO`, con un tope total de 8 s. Solo envío `text`, `lang` y `lights` (id, nombre, habitación, encendida, brillo). Cancelar corta la conexión. No sigo redirecciones.
- **Validación siempre, venga de donde venga:** ids que existen en el estado actual, brillo recortado a 0–100, solo las 4 escenas, como máximo 5 acciones. Lo inválido se descarta; si no queda nada, muestro "No entendí".
- **Confirmación:** resumen legible ("Encender Lámpara (Sala) al 50 %", "Escena: Lectura") y la respuesta del asistente, con Aplicar y Cancelar.
- **Al aplicar** uso la misma ruta que las escenas (`runHubAction`). Si cambia la Lámpara se envía una sola orden al hub; si falla, todas las luces vuelven a como estaban y aparece "NO SE PUDO" con Reintentar. La pantalla **Listo** ofrece Deshacer con el mismo snapshot de una capa.
- **Estados:** cargando (con Cancelar), no entendí, sin conexión, error del servidor y sin configurar. Reutilizan la plantilla de Conectando / No se pudo.
- Todos los textos están en `strings.xml` en español e inglés. Porcentajes: "30 %" en español y "30%" en inglés.
- Configuración: activé `buildConfig` y dos campos, `ASSISTANT_URL` y `ASSISTANT_TOKEN`, que se leen de `local.properties` y quedan vacíos si faltan. No hay URL ni token en el código.
- Documenté el servidor en `worker/README.md`: qué hace, contrato, variables sin valores, despliegue y límites de seguridad.

**Decisiones:**
- "Encender todo" es una acción solo local (`TurnOnAll`): el contrato del servidor no la tiene y con 8 luces superaría el tope de 5 acciones.
- Si la frase nombra una habitación ("apaga las luces del dormitorio"), las reglas no la interpretan como "toda la casa": dejo que decida el servidor.
- Si el servidor manda `set_light` sin brillo, la luz conserva su brillo actual. Encender con 0 % la deja apagada.
- Cuando el servidor no entiende y explica por qué, muestro su respuesta en vez del texto genérico.

**Seguridad (honestamente):** el token va dentro del APK y se puede extraer. Lo mitigan los topes de entrada y salida del servidor, el límite del plan gratuito y que la app valida y confirma todo antes de actuar. Lo detallo en `worker/README.md`.

**Estados:**
- Implementado: sí.
- Compilado: sí, `BUILD SUCCESSFUL` con `.\gradlew.bat :app:assembleDebug --no-daemon --stacktrace`, sin warnings de Kotlin.
- Preview renderizado: no. Agregué previews (frases, escribir, confirmar, listo, cargando, sin conexión, sin configurar; claro, oscuro y escalas de fuente), pero no los rendericé.
- Instalado: sí, en el emulador Wear OS (`:app:installDebug`).
- Probado en el emulador:

| Caso | Resultado |
|---|---|
| Inicio → Asistente | Aparece la lista con las 5 frases y Escribir |
| "Modo lectura" → Aplicar | Confirmación "Escena: Lectura" → Listo. La Lámpara no cambia, así que no se envía nada al hub |
| Deshacer desde Listo | Vuelve a la lista; Dormitorio regresa a 2 de 3 encendidas |
| "Lámpara al 50 %" → Aplicar con el hub apagado | Una orden al hub; a los 10 s, "NO SE PUDO". Volver regresa a la confirmación y la Lámpara sigue al 45 % |
| Cancelar en la confirmación | Vuelve a la lista y no se envía nada |
| App en inglés: "Reading mode" | La reconocen las reglas: "Scene: Reading". Porcentajes sin espacio ("Bedroom · 70%") |
| Fuente a 1,3 | Lista y confirmación sin texto cortado (las filas crecen) |
| Campo de texto | Se ve el contador centrado y el borde de foco |

**Sin probar:**
- **Consulta al servidor:** la compilación quedó con URL y token vacíos (`local.properties` no tiene `assistant.url` ni `assistant.token`). Tampoco probé "sin conexión", "error del servidor" ni Cancelar durante la carga.
- **Enviar texto escrito:** en el emulador, el teclado no aceptó los toques simulados en su botón de enviar, así que el texto no llegó a la app. Falta probarlo a mano. Por lo mismo, la pantalla "Sin configurar" (solo se llega con una frase que las reglas no entienden) no se vio en el emulador.
- Reintentar tras "NO SE PUDO" en una acción del asistente.
- Las reglas no tienen pruebas automáticas: el proyecto no tiene dependencias de pruebas unitarias y no agregué ninguna.

---

## LW-010b-2 — Diagnóstico del asistente: certificado rechazado y servidor lento

**Fecha:** 2026-10-25

**Qué pasaba:** al consultar al servidor del asistente desde el emulador, la consulta fallaba y la app mostraba "Sin conexión".

**Causa 1 — reloj del emulador:** el emulador tenía la hora atrasada y rechazaba el certificado del servidor por considerarlo todavía no válido. No era un error de la app ni del servidor. Lo resolví con un **arranque en frío** del emulador, que le devolvió la hora correcta.

**Causa 2 — servidor lento:** algunas consultas al modelo tardaban demasiado. Lo corregí en el servidor (`worker/worker.js`):
- puse un **tiempo máximo de 6 s** a la consulta al modelo; si se agota, el servidor responde un error controlado (504) en vez de quedarse esperando;
- cambié a un **modelo más rápido**; el nombre del modelo no está en el código, se configura como variable del servicio;
- la app conserva su tope total de 8 s, mayor que el del servidor para que este responda primero.

También hice que el servidor nombre las escenas según el idioma de la app.

**Estados:**
- Implementado: sí (commit `feat(worker): agrega tiempo máximo a la consulta y nombra las escenas según el idioma`).
- Compilado: no aplica al cambio del servidor, que es JavaScript y no pasa por Gradle.
- Instalado: no aplica a la app, que no cambió en esta entrada.
- Probado: el arranque en frío quitó el rechazo del certificado. Después, en LW-011b, una frase escrita ("hola") llegó al servidor desde el emulador y volvió su respuesta ("No entendí" con el texto del servidor), así que la consulta funciona de punta a punta.
- Sin probar: no repetí aquí la medición de tiempos del modelo (los números están en `docs/documentacion.md`, tal como los registré entonces); tampoco el caso de los 6 s agotados contra el servicio real, ni el asistente en un reloj físico.

---

## LW-011a — Pulido, parte 1: hora, pantallas de estado, orden duplicada y Aplicar

**Fecha:** 2026-10-25 y 2026-10-26

**Qué hice:**
- **Hora pegada al encabezado.** Las pantallas de un solo paso recordaban el desplazamiento de la visita anterior y el título quedaba pegado a la hora o fuera de vista. Ahora **Confirmar, Listo, Consultando y los errores del asistente empiezan arriba**, y también **Conectando, No se pudo y Sin conexión**, cada una con su propia clave de estado.
- **Orden duplicada de brillo.** Al subir de más el brillo de la Lámpara y corregir hasta un valor que el hub ya había confirmado, la app volvía a enviar la orden. Ahora recuerda el último estado confirmado y no reenvía lo que el hub ya tiene. Ese valor no se guarda: si la app se recrea, la orden se vuelve a enviar, porque otro cliente pudo cambiar el hub.
- **Aplicar cortado.** En la confirmación del asistente, con varias acciones o textos largos, Aplicar quedaba cortado por abajo. Ahora, si no cabe completo, la lista baja lo justo para que se vea entero (un máximo de ajustes, sin bucles) y, si ya cabe, no se mueve.

**Estados:**
- Implementado: sí, en cuatro commits (`fix(asistente)` ×2, `fix(estados)`, `fix(luz)`).
- Compilado: sí. La compilación de LW-011b incluye estos cambios y terminó en `BUILD SUCCESSFUL`.
- Instalado: sí, en el emulador Wear OS (`:app:installDebug`, LW-011b).
- Probado en el emulador, al repasar las pantallas en LW-011b: Confirmar y Listo empiezan arriba; Conectando y No se pudo empiezan arriba; con "Apagar todas las luces" a fuente 1,3, Aplicar y Cancelar quedan completos y alcanzables.
- Sin probar: la orden duplicada de brillo (no repetí la secuencia de subir y corregir con el hub); Aplicar con varias acciones y respuestas largas del servidor; el comportamiento en un reloj físico.

---

## LW-011b — Pulido, parte 2: fuente 1,3, tema oscuro, accesibilidad y auditoría

**Fecha:** 2026-10-26

Trabajé cuatro puntos, uno por commit. Salvo el tercero y el contraste, no hubo nada que corregir.

**1. Fuente a 1,3.** Recorrí todas las pantallas en español: Inicio, Habitaciones, luces, Escenas, Favoritos (lista, Editar y vacío), Ajustes, Asistente y sus estados, Conectando y No se pudo. No encontré texto cortado ni botones inalcanzables, así que no cambié código. Observación: en Confirmar, la primera línea del título queda detrás de la hora porque la pantalla baja a propósito para dejar Aplicar completo (LW-011a).

**2. Tema oscuro.** Recorrí las mismas pantallas. Los textos cumplen el mínimo de 4,5:1 (oscuro: 6,19 a 17,8; claro: 5,9 a 16,7). No hubo errores objetivos de legibilidad ni colores fuera de los tokens. Lo único bajo 3:1 eran los bordes de controles en oscuro (1,57:1), que traté en el punto de contraste de abajo.

**3. Accesibilidad.** Revisé el árbol de la interfaz de cada pantalla: descripciones, tamaño táctil y orden de lectura. Los objetivos táctiles ya cumplían 48 dp (botones 48 dp, elementos de lista 60 a 82 dp, − y + de 56×48 dp) y el orden de lectura coincide con el visual. Corregí cuatro cosas:
- **− y +:** el lector decía "Bajar brillo" y además el símbolo "−". Ahora el símbolo es decorativo y solo se anuncia la descripción.
- **Botones con descripción propia** (Deshacer de escena y de asistente): leían la descripción y luego el texto visible. Ahora solo la descripción.
- **"5 de 8 luces":** Inicio leía "5 / 8" y "luces encendidas" por separado; ahora se lee "5 de 8 luces encendidas" (cadena nueva en español e inglés).
- **Quitar escena con nombre:** el botón decía "Quitar escena" sin decir cuál; ahora la descripción es "Quitar escena Descanso".

**4. Auditoría del sistema de diseño.** Busqué `Color(0x…)`, `dp`, `sp`, formas y textos literales. Sin hallazgos que corregir: todos los colores están en `Color.kt`, no hay `dp` ni `sp` literales en pantallas, las formas usan tokens de radio y no hay texto visible fuera de `strings.xml`. Dejé como decisiones pendientes: la cadena `access_voice` sin uso, dos alturas de 60 dp repetidas, unas 35 apariciones de colores semánticos directos en pantallas y los textos de ejemplo de dos previews.

**Contraste del borde de controles en oscuro.** Creé el token `controlBorder` solo para el borde del campo de texto y la pista del arco de brillo. Elegí `#606060`, el gris más cercano al `#363636` anterior que llega a 3:1: **3,03:1** sobre la superficie (`#101010`) y **3,34:1** sobre negro. El tema claro reutiliza su borde actual (3,76:1) y el borde de las tarjetas no cambió. Está en `Color.kt`, `Tokens.kt` y `docs/design-tokens.md`.

**Estados:**
- Implementado: sí. Accesibilidad (`fix(a11y)`) y contraste (`fix(tema)`) con código; fuente 1,3, tema oscuro y auditoría sin cambios de código.
- Compilado: sí, `BUILD SUCCESSFUL` con `.\gradlew.bat :app:assembleDebug :app:installDebug --no-daemon --stacktrace` en las dos entregas con código.
- Instalado: sí, en el emulador Wear OS.
- Probado en el emulador, con capturas: fuente 1,3 y tema oscuro en las pantallas listadas; árbol de accesibilidad antes y después (descripciones de − y +, "5 de 8 luces encendidas", "Quitar escena Descanso"); borde del campo de texto medido en `#606060` en oscuro y `#7C8775` en claro, y pista del arco en `#606060`. Al terminar dejé el emulador en fuente 1,0 y tema claro.
- **Sin probar:**
  - **TalkBack real:** solo revisé el árbol de accesibilidad; lo que oye una persona lo deduzco, no lo escuché.
  - **Fuente 1,3 con la app en inglés:** solo comprobé que ninguna cadena en inglés es mucho más larga que la española.
  - **Reloj físico:** todo fue en el emulador.
  - Los estados pulsado y enfocado en oscuro, y si los títulos se anuncian como encabezado.
  - Las cadenas nuevas de accesibilidad en pantalla con la app en inglés.

---

## LW-012 — Documentación verificada contra el código

**Fecha:** 2026-10-26

**Qué hice:** comparé cada afirmación de `docs/documentacion.md` con el código y los recursos, y edité solo lo que estaba mal (11 correcciones):
- el diagrama de navegación no incluía Escena activa, Sugerencia, la apertura de luces y escenas desde Favoritos ni Escribir;
- "máximo tres niveles" no valía para el asistente con Escribir; ahora dice "tres pasos" hasta cualquier luz o escena y aclara que el asistente es un flujo aparte;
- el texto visible de Inicio es "5 / 8 luces encendidas", no "5 de 8";
- las listas muestran habitación y porcentaje junto al nombre de la escena;
- Favoritos permite quitar y restaurar, no editar en general;
- la paleta tiene 18 colores (con `controlBorder`), no 17;
- faltaban tres roles semánticos y cuatro tokens de componente;
- la clasificación atómica contradecía a `design-tokens.md` (el dial de brillo es organismo y las pantallas son plantillas y páginas);
- español e inglés tienen las mismas claves salvo los dos nombres de idioma;
- el hub solo reenvía una orden si el bombillo sigue distinto tras leer el estado.

Confirmé sin cambios: 8 luces en 3 habitaciones, las 4 escenas, los archivos citados, los tres enlaces relativos, `minSdk 30`, y el flujo de conexión y del asistente (puertos, plazos de 6 y 8 s, validación en servidor y app, tope de 5 acciones).

**No pude comprobar en el código:** los nombres de los modelos y los tiempos medidos (el modelo se configura como variable del servicio), el límite del plan gratuito, las pruebas con el bombillo real y con el modelo del cuadro "Hecho y probado", la respuesta al ejemplo «pon la sala tranquila» y la afirmación "una tarea por pantalla".

**Estados:**
- Implementado: sí, solo se editó `docs/documentacion.md`.
- Compilado e instalado: no aplica, no hubo cambios de código.
- Probado: verificación por lectura del código y de los recursos, no en ejecución.
- Sin probar: lo listado arriba como no comprobable, además de TalkBack real, fuente 1,3 en inglés y reloj físico, que el documento ya marca como pendientes.

---

## LW-013a — Hub con reloj físico: dirección por intent y red de depuración

**Fecha:** 2026-10-26

**Qué hice:**
- **Dirección del hub por intent (solo depuración).** El extra `hub_url` de `MainActivity` fija la dirección del hub sin recompilar (`--es hub_url http://IP:8787`) y `reset` vuelve a la de fábrica. `HubUrl.kt` solo acepta `http://`, una IP privada literal (10.x, 172.16–31.x o 192.168.x), un puerto y ninguna ruta; si no cumple, la ignora y registra solo el motivo (etiqueta `HubUrl`), nunca la dirección.
- **Configuración de red de depuración.** `app/src/debug/res/xml/network_security_config.xml` permite HTTP solo a `10.0.2.2` (emulador), `192.168.80.18` (PC en casa) y `172.20.10.2` a `172.20.10.14` (hotspot del iPhone). La configuración de `main` sigue permitiendo solo `10.0.2.2`.
- **Guía en `hub/README.md`**, sección «Reloj físico»: hub con `--host 0.0.0.0`, regla de firewall solo para redes privadas, cómo averiguar la IP del PC, prueba con `curl.exe`, el comando de `hub_url` y qué hacer si la IP no está en la lista.

**Estados:**
- Implementado: sí.
- Probado en emulador por Alejandro: el bombillo real desde la app, con el PC y el bombillo conectados al hotspot del iPhone (hub escuchando en el PC, bombillo en 172.20.10.2). Existe una grabación de esa prueba; no está en el repositorio.
- **Sin probar:** reloj físico. La guía lo dice: «falta probarlo con el reloj físico».

---

## LW-014a — Capa de datos de luces

**Fecha:** 2026-10-26

**Qué hice:** dejé de tener las 8 luces fijas en el código; ahora la lista de luces es configuración.
- **Modelo.** `Light` (id, habitación, tipo y número de orden) y `LightType`. El nombre visible no se guarda: se deriva del tipo y del número en el idioma actual («Plafón», «Plafón 2»). Solo Plafón, Lámpara, Tira LED y la luz del hub se pueden añadir; los otros cinco tipos existen únicamente en la casa demo.
- **Guardado.** `LightsConfig` se guarda como JSON en las preferencias (`lights_config`) con un contador `next_id` que solo crece, para no reutilizar ids. Un JSON ilegible, o entradas con habitación, tipo o id inválidos, no rompen la app: se descartan o se vuelve a la casa demo.
- **Estado.** `LightStateHolder` permite añadir, quitar, vaciar y recargar la demo conservando el estado del resto. La luz del hub solo puede existir una vez y usa siempre `living_lampara`.
- **Lo que depende de la lista.** Los favoritos ignoran las luces quitadas, las escenas se calculan con las luces que existen, Inicio cuenta con la lista real y el asistente busca las luces por tipo y número con la lista actual. También evité un cierre cuando no existe la luz del hub y hice que el asistente responda «Sin luces» sin consultar al servidor.

**Estados:**
- Implementado: sí (commits `45a927f`, `d2f39f2`, `a39d04d`, `791004d`, `9179155`, `676bec9`).
- Probado: sin registro propio; se ejerció a través de LW-014b y LW-014c. No hay pruebas automáticas en el repositorio.
- Sin probar: el comportamiento con un JSON de configuración dañado.

---

## LW-014b — Primer uso: bienvenida, punto de partida, estados vacíos y Volver a empezar

**Fecha:** 2026-10-26

**Qué hice:**
- **Bienvenida y punto de partida.** Si el primer uso no está completo, la app arranca en la bienvenida; «Empezar» lleva a elegir «Casa demo» (8 luces de ejemplo) o «Empezar desde cero». Las dos pantallas no muestran la etiqueta DEMO y empiezan arriba.
- **Estados vacíos.** Inicio sin luces («Aún no hay luces.»), habitaciones sin luces («Sin luces») y escenas cuya habitación o casa no tiene luces («Sin luces en Sala.»), que no aplican nada.
- **Volver a empezar** en Ajustes, con confirmación: borra luces y favoritos y vuelve a la bienvenida. El botón principal es «Cancelar».
- **Favoritos.** Quité «Restaurar demo» (ya no tiene sentido con luces configurables) y generalicé el texto de «Buenas noches». En `hub/README.md` expliqué cómo sincronizar la Lámpara sin Restaurar demo.

**Estados:**
- Implementado: sí (commits `6fbaaec` a `e75f64f`).
- Probado en emulador por Alejandro: el flujo de primer uso, los estados vacíos y Volver a empezar.
- Sin probar: reloj físico. Sin evidencia en el repositorio de pruebas por pantalla: no hay pruebas automáticas.

---

## LW-014c — Añadir y quitar luces

**Fecha:** 2026-10-27

**Qué hice:**
- **Añadir luz en 2 pasos:** habitación y tipo (Plafón, Lámpara, Tira LED o Lámpara con hub), y una pantalla «Luz añadida» con «Controlar» y «Añadir otra». Las luces nuevas nacen apagadas con el brillo recordado en 50 %. El límite es de 6 por habitación: el paso de habitación muestra «Completa» y el detalle de la habitación oculta «Añadir luz».
- **Puntos de entrada:** Inicio sin luces, Habitaciones, el detalle de una habitación (salta el paso 1) y el detalle de una escena sin luces (salta el paso 1 si la escena es de una habitación).
- **Quitar luz** desde el control de la luz, con confirmación: el botón principal es «Conservar». Al quitarla sale de favoritos y deja de contar en las escenas.
- **Corrección:** en inglés, el nombre de la habitación en «No lights in …» va en minúscula.

**Estados:**
- Implementado: sí (commits `9f65844` a `b4e1437`).
- Probado en emulador por Alejandro: añadir y quitar luces, el límite de 6 **solo en Estudio**, y la Lámpara con hub tras quitarla y volver a añadirla (con el hub simulado).
- **Sin probar:** el límite de 6 en Sala y Dormitorio; reloj físico.

---

## LW-014d — Textos de primer uso y envío del teclado

**Fecha:** 2026-10-28

**Qué hice:**
- **Inicio vacío.** El texto ahora dice «Añade la primera para controlarla.» (en: «Add the first one to control it.»).
- **«Luz añadida».** El titular es «Luz añadida.» («Light added.») y el cuerpo «<nombre> · <habitación> · apagada.», por ejemplo «Plafón 2 · Sala · apagada.». Así la concordancia no depende del género del nombre. Quité la cadena «<nombre> añadido.».
- **Envío del teclado en el asistente.** `AssistantInputScreen` ahora recibe el texto al enviar: el Enter usa el texto recién saneado y la flecha del teclado y el botón «Enviar» leen el texto vigente con `rememberUpdatedState`, no el de una composición anterior. Sospechaba que `onSend` enviaba el borrador anterior; no lo reproduje antes del cambio, así que la causa es una deducción del código.

**Estados:**
- Implementado y compilado: sí, `installDebug` sin errores (commits `ddff05f` y `8b0a504`).
- Instalado: sí, en el emulador Wear OS pequeño y redondo (384×384).
- Probado en el emulador: Inicio vacío con el texto nuevo; «Luz añadida.» con «Plafón · Sala · apagada.» y «Lámpara · Sala · apagada.»; el envío con Enter de dos frases distintas seguidas («enciende todo» y luego «apaga todo»), que propuso «Encender todas las luces» y después «Apagar todas las luces».
- **Sin probar:** la flecha de envío del teclado de Wear. Con `adb shell input text` el texto quedó en el editor propio del teclado (el contador de la app seguía en 0/200) y ni `keyevent 66` ni tocar la flecha lo enviaron. Tampoco probé el botón «Enviar». Falta confirmarlo en un reloj real.
