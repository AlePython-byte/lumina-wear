package com.alejandro.luminawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.timeTextCurvedText
import androidx.wear.compose.material3.Typography

enum class ThemeMode { LIGHT, DARK }

object LuminaTheme {
    val colors: LuminaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLuminaColors.current
}

/** Traduce los tokens semánticos al ColorScheme de Wear Material 3. */
private fun LuminaColors.toColorScheme() = ColorScheme(
    primary = accent,
    primaryDim = pressed,
    onPrimary = onAccent,
    primaryContainer = accent,
    onPrimaryContainer = onAccent,
    secondary = surface,
    onSecondary = text,
    secondaryContainer = surface,
    onSecondaryContainer = text,
    background = bg,
    onBackground = text,
    surfaceContainerLow = surface,
    surfaceContainer = surface,
    surfaceContainerHigh = surface,
    onSurface = text,
    onSurfaceVariant = muted,
    outline = border,
    outlineVariant = border,
    error = danger,
    onError = bg
)

private val luminaMaterialTypography = Typography(
    displayLarge = LuminaTypography.display,
    titleMedium = LuminaTypography.title,
    bodyMedium = LuminaTypography.body,
    labelMedium = LuminaTypography.button,
    labelSmall = LuminaTypography.label
)

/** Reloj del sistema con el estilo `time` y el color `muted` del tema activo. */
@Composable
fun LuminaTimeText() {
    val style = CurvedTextStyle(LuminaTypography.time.copy(color = LuminaTheme.colors.muted))
    TimeText { time ->
        timeTextCurvedText(time, style)
    }
}

@Composable
fun LuminaWearTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val colors = when (themeMode) {
        ThemeMode.LIGHT -> LightLuminaColors
        ThemeMode.DARK -> DarkLuminaColors
    }
    CompositionLocalProvider(LocalLuminaColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = luminaMaterialTypography,
            content = content
        )
    }
}
