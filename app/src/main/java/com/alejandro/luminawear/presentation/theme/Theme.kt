package com.alejandro.luminawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val luminaColorPalette = ColorScheme(
    primary = LimeMain,
    onPrimary = Black,
    primaryContainer = LimeMain,
    onPrimaryContainer = Black,
    secondary = SurfaceDark,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = TextPrimary,
    background = Black,
    onBackground = TextPrimary,
    surfaceContainer = SurfaceDark,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    error = ErrorColor,
    onError = Black
)

@Composable
fun LuminaWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = luminaColorPalette,
        content = content
    )
}