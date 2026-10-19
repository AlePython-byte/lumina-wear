package com.alejandro.luminawear.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Nivel 2 — Tokens semánticos (Figma: "Lumina / Tema").
 * Regla: en tema claro `accent` (lime) solo se usa como relleno. Para texto,
 * arcos o iconos de acento se usa `accentContent`.
 */
@Immutable
data class LuminaColors(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val accent: Color,
    val onAccent: Color,
    val pressed: Color,
    val warning: Color,
    val danger: Color,
    val accentContent: Color
)

/** Modo Claro · Predeterminado */
val LightLuminaColors = LuminaColors(
    bg = LuminaPalette.lightBackground,
    surface = LuminaPalette.lightSurface,
    text = LuminaPalette.lightText,
    muted = LuminaPalette.lightMuted,
    border = LuminaPalette.lightBorder,
    accent = LuminaPalette.lime,
    onAccent = LuminaPalette.black,
    pressed = LuminaPalette.pressed,
    warning = LuminaPalette.lightWarning,
    danger = LuminaPalette.lightDanger,
    accentContent = LuminaPalette.lightAccentContent
)

/** Modo Oscuro · Secundario */
val DarkLuminaColors = LuminaColors(
    bg = LuminaPalette.black,
    surface = LuminaPalette.surface,
    text = LuminaPalette.white,
    muted = LuminaPalette.muted,
    border = LuminaPalette.border,
    accent = LuminaPalette.lime,
    onAccent = LuminaPalette.black,
    pressed = LuminaPalette.pressed,
    warning = LuminaPalette.warning,
    danger = LuminaPalette.danger,
    accentContent = LuminaPalette.lime
)

val LocalLuminaColors = staticCompositionLocalOf { LightLuminaColors }

object LuminaSpacing {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp
}

object LuminaRadius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 18.dp
    val full = 999.dp
}

object LuminaSizes {
    val touch = 48.dp
}

object LuminaBorder {
    val width = 1.dp
}
