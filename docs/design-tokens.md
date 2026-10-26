# LuminaWear — Design tokens

Fuente de verdad: Figma `vZFxwvxXECtL9x7WdRtmoa`, colecciones **Lumina / Primitives** y **Lumina / Tema** (modos *Claro · Predeterminado* y *Oscuro · Secundario*). Código en `app/src/main/java/com/alejandro/luminawear/presentation/theme/`.

## Nivel 1 — Globales (`Color.kt` → `LuminaPalette`)

| Figma (Primitives) | Kotlin | Valor |
|---|---|---|
| black / surface / white | `black` / `surface` / `white` | #000000 / #101010 / #F4F4F0 |
| muted / border | `muted` / `border` | #96968E / #363636 |
| controlBorder (solo oscuro) | `controlBorder` | #606060 |
| lime / pressed | `lime` / `pressed` | #C8FF37 / #ACD92F |
| warning / danger | `warning` / `danger` | #FFB84D / #FF5A52 |
| light background / surface / text | `lightBackground` / `lightSurface` / `lightText` | #FFFFFF / #F3F5F0 / #172016 |
| light muted / border | `lightMuted` / `lightBorder` | #56614F / #7C8775 |
| light accent content / warning / danger | `lightAccentContent` / `lightWarning` / `lightDanger` | #456600 / #855000 / #B42318 |

## Nivel 2 — Semánticos (`Tokens.kt` → `LuminaColors`, acceso `LuminaTheme.colors`)

| Figma (Tema) | Kotlin | Claro | Oscuro | Material 3 |
|---|---|---|---|---|
| bg | `bg` | lightBackground | black | background |
| surface | `surface` | lightSurface | surface | surfaceContainer |
| text | `text` | lightText | white | onSurface, onBackground |
| muted | `muted` | lightMuted | muted | onSurfaceVariant |
| border | `border` | lightBorder | border | outline |
| controlBorder | `controlBorder` | lightBorder | controlBorder | — |
| accent | `accent` | lime | lime | primary |
| onAccent | `onAccent` | black | black | onPrimary |
| pressed | `pressed` | pressed | pressed | primaryDim |
| warning | `warning` | lightWarning | warning | — |
| danger | `danger` | lightDanger | danger | error |
| accentContent | `accentContent` | lightAccentContent | lime | — |

Regla: en claro `accent` solo es relleno; texto, arcos e iconos de acento usan `accentContent`.

`controlBorder`: borde de los controles que el usuario debe distinguir del fondo (borde del campo de texto y pista del arco de brillo). En oscuro vale #606060, el gris más cercano al `border` (#363636) que llega a 3:1 en ambos fondos: 3,03:1 sobre `surface` (#101010) y 3,34:1 sobre `bg` (#000000). En claro reutiliza `lightBorder` (3,76:1 sobre blanco), sin cambios. El `border` de tarjetas y botones no cambia (en oscuro 1,57:1 sobre `surface`, decorativo).
Escala: `LuminaSpacing` 0·4·8·12·16·20·24·32·48 dp · `LuminaRadius` 4·8·12·18·999 · `LuminaSizes.touch` 48 dp · `LuminaBorder.width` 1 dp.
Tipografía (`Type.kt`, familia en `LuminaFontFamily`): display Black 48/48 · title Bold 20/24 · body Regular 14/18 · button Bold 14/18 · label Bold 12/16 · time Regular 12/16.

## Nivel 3 — Componente (`ComponentTokens.kt`)

| Figma | Kotlin | Tokens semánticos usados |
|---|---|---|
| ActionButton | `ActionButtonTokens` | default accent/onAccent · pressed pressed · disabled surface/muted · secondary surface/text/border |
| StatusChip | `StatusChipTokens` | activo accentContent · inactivo muted |
| RoomControl | `RoomControlTokens` | surface · border · text · muted |
| SceneCard | `SceneCardTokens` | surface · accentContent · text · muted |
| LightToggle | `LightToggleTokens` | luz apagada accent/onAccent (pressed) · luz encendida surface/text/border |
| NavigationItem | `NavigationItemTokens` | surface · text · disabled muted · border · **Selected**: surface + borde accentContent + detalle accentContent (opciones de Ajustes, 60 dp) |
| BrightnessDial | `BrightnessDialTokens` | arco accentContent · pista controlBorder · valor text/muted · etiqueta muted |
| TextInput | `TextInputTokens` | surface · text · placeholder/contador muted · borde controlBorder · foco accentContent |
| StepButton | `StepButtonTokens` | surface · text · disabled muted · border |

## Atomic Design (archivos reales)

| Nivel | Elementos | Archivo |
|---|---|---|
| Fundamentos | paleta, semánticos, escala, tipografía, tema | `theme/Color.kt`, `Tokens.kt`, `Type.kt`, `ComponentTokens.kt`, `Theme.kt` |
| Átomos | `StepButton`, `SecondaryAccessButton` (NavigationItem), `ActionButton` (primario/secundario, 48 dp), textos con `LuminaTypography` | `lights/LightControlScreen.kt`, `home/HomeScreen.kt`, `components/ActionButton.kt` |
| Moléculas | RoomControl (tarjeta de habitación / luz con StatusChip), SceneCard (sugerencia "Por la hora" dinámica), `NavigationItem` de 60 dp (clic o selección: Ajustes y lista de Escenas), encabezado DEMO + título | `rooms/RoomsScreen.kt`, `rooms/RoomDetailScreen.kt`, `home/HomeScreen.kt`, `components/NavigationItem.kt`, `scenes/SceneScreens.kt` |
| Organismos | BrightnessDial (arco + valor), fila de StepButtons, listas `TransformingLazyColumn` | `lights/LightControlScreen.kt`, todas las pantallas |
| Plantillas | `AppScaffold` + `ScreenScaffold` + navegación | `WearApp.kt` |
| Páginas | `HomeScreen`, `RoomsScreen`, `RoomDetailScreen`, `LightControlScreen`, `SettingsScreen`, `ScenesScreen`, `SceneDetailScreen`, `SceneActiveScreen`, `SuggestionScreen` | `home/`, `rooms/`, `lights/`, `settings/`, `scenes/SceneScreens.kt` |

Lógica sin UI de escenas: `models/Scenes.kt` (modelo, `suggestionForHour`) y `state/SceneLogic.kt` (`applyScene`, función pura). Estados por defecto: `defaultLightStates()` en `state/LightStateHolder.kt`.


## Tipografía y licencia

- Fuente: **Roboto Flex** (fuente variable), licencia **SIL Open Font License 1.1** (texto en `docs/licenses/OFL.txt`).
- Archivo: `app/src/main/res/font/roboto_flex.ttf`.
- El `FontFamily` se define en un solo lugar: `LuminaFontFamily` en `theme/Type.kt` (pesos 400, 700 y 900 vía `FontVariation.weight`). Todo texto usa `LuminaTypography`.

## Preferencias

- Dónde: `SharedPreferences` `luminawear_prefs` (clase `AppPreferences` en `state/AppPreferences.kt`), sin dependencias.
- Qué: `theme_mode` (`light` / `dark`), `language` (`es` / `en`), `favorites` (ids separados por comas; por defecto `living_plafon,relax`; clave presente y vacía = sin favoritos) y `hub_url` (solo lectura, sin UI; ausente → `http://10.0.2.2:8787`). Se escriben con `apply()`.
- Por defecto: valor ausente o inválido → tema **LIGHT** e idioma **ES** (aunque el sistema esté en inglés).
- Lectura: `MainActivity` las lee antes de `setContent`; el idioma se aplica en `attachBaseContext` y cambiarlo llama a `recreate()`.

## Favoritos

Sin tokens nuevos. `favorites/FavoritesScreens.kt` reutiliza `NavigationItem`, `ActionButton` y `SceneHeader`/`CenteredText` (ahora `internal` en `scenes/SceneScreens.kt`). Lógica en `state/Favorites.kt`.

## Hub (estados Conectando / No se pudo)

Sin tokens nuevos. `lights/HubStatusScreens.kt` reutiliza `SceneHeader`, `CenteredText` y `ActionButton` ("Reintentar" primario = `accent`, lima; "Volver" y "Cancelar" secundarios).

