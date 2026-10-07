package com.alejandro.luminawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val luminaColorPalette = ColorScheme(
    primary = LimeMain,
    onPrimary = DarkText,
    primaryContainer = LimeMain,
    onPrimaryContainer = DarkText,
    secondary = DarkSurfaceVariant,
    onSecondary = LightText,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = LightText,
    background = Black,
    onBackground = LightText,
    surfaceContainer = DarkSurface,
    onSurface = LightText,
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