# LuminaWear — Design tokens

Fuente de verdad: Figma `vZFxwvxXECtL9x7WdRtmoa`, colecciones **Lumina / Primitives** y **Lumina / Tema** (modos *Claro · Predeterminado* y *Oscuro · Secundario*). Código en `app/src/main/java/com/alejandro/luminawear/presentation/theme/`.

## Nivel 1 — Globales (`Color.kt` → `LuminaPalette`)

| Figma (Primitives) | Kotlin | Valor |
|---|---|---|
| black / surface / white | `black` / `surface` / `white` | #000000 / #101010 / #F4F4F0 |
| muted / border | `muted` / `border` | #96968E / #363636 |
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
| accent | `accent` | lime | lime | primary |
| onAccent | `onAccent` | black | black | onPrimary |
| pressed | `pressed` | pressed | pressed | primaryDim |
| warning | `warning` | lightWarning | warning | — |
| danger | `danger` | lightDanger | danger | error |
| accentContent | `accentContent` | lightAccentContent | lime | — |

Regla: en claro `accent` solo es relleno; texto, arcos e iconos de acento usan `accentContent`.
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
| NavigationItem | `NavigationItemTokens` | surface · text · disabled muted · border |
| BrightnessDial | `BrightnessDialTokens` | arco accentContent · pista border · valor text/muted · etiqueta muted |
| StepButton | `StepButtonTokens` | surface · text · disabled muted · border |

## Atomic Design (archivos reales)

| Nivel | Elementos | Archivo |
|---|---|---|
| Fundamentos | paleta, semánticos, escala, tipografía, tema | `theme/Color.kt`, `Tokens.kt`, `Type.kt`, `ComponentTokens.kt`, `Theme.kt` |
| Átomos | `StepButton`, `SecondaryAccessButton` (NavigationItem), textos con `LuminaTypography` | `lights/LightControlScreen.kt`, `home/HomeScreen.kt` |
| Moléculas | RoomControl (tarjeta de habitación / luz con StatusChip), SceneCard, encabezado DEMO + título | `rooms/RoomsScreen.kt`, `rooms/RoomDetailScreen.kt`, `home/HomeScreen.kt` |
| Organismos | BrightnessDial (arco + valor), fila de StepButtons, listas `TransformingLazyColumn` | `lights/LightControlScreen.kt`, todas las pantallas |
| Plantillas | `AppScaffold` + `ScreenScaffold` + navegación | `WearApp.kt` |
| Páginas | `HomeScreen`, `RoomsScreen`, `RoomDetailScreen`, `LightControlScreen` | `home/`, `rooms/`, `lights/` |


## Tipografía y licencia

- Fuente: **Roboto Flex** (fuente variable), licencia **SIL Open Font License 1.1** (texto en `docs/licenses/OFL.txt`).
- Archivo: `app/src/main/res/font/roboto_flex.ttf`.
- El `FontFamily` se define en un solo lugar: `LuminaFontFamily` en `theme/Type.kt` (pesos 400, 700 y 900 vía `FontVariation.weight`). Todo texto usa `LuminaTypography`.