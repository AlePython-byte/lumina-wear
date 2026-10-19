package com.alejandro.luminawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Nivel 3 — Tokens de componente. Solo referencian tokens semánticos
 * (LuminaTheme.colors) o la escala de spacing/radius/sizes. Nada de hex.
 */

object ActionButtonTokens {
    val containerDefault: Color @Composable get() = LuminaTheme.colors.accent
    val contentDefault: Color @Composable get() = LuminaTheme.colors.onAccent
    val containerPressed: Color @Composable get() = LuminaTheme.colors.pressed
    val contentPressed: Color @Composable get() = LuminaTheme.colors.onAccent
    val containerDisabled: Color @Composable get() = LuminaTheme.colors.surface
    val contentDisabled: Color @Composable get() = LuminaTheme.colors.muted
    val containerSecondary: Color @Composable get() = LuminaTheme.colors.surface
    val contentSecondary: Color @Composable get() = LuminaTheme.colors.text
    val borderSecondary: Color @Composable get() = LuminaTheme.colors.border
    val radius: Dp = LuminaRadius.sm
    val minHeight: Dp = LuminaSizes.touch
}

object StatusChipTokens {
    val contentActive: Color @Composable get() = LuminaTheme.colors.accentContent
    val contentInactive: Color @Composable get() = LuminaTheme.colors.muted
}

object RoomControlTokens {
    val container: Color @Composable get() = LuminaTheme.colors.surface
    val border: Color @Composable get() = LuminaTheme.colors.border
    val title: Color @Composable get() = LuminaTheme.colors.text
    val subtitle: Color @Composable get() = LuminaTheme.colors.muted
    val radius: Dp = LuminaRadius.sm
    val padding: Dp = LuminaSpacing.md
    val minHeight: Dp = 60.dp // fuera de escala: alto mínimo de fila con dos líneas
}

object SceneCardTokens {
    val container: Color @Composable get() = LuminaTheme.colors.surface
    val eyebrow: Color @Composable get() = LuminaTheme.colors.accentContent
    val title: Color @Composable get() = LuminaTheme.colors.text
    val subtitle: Color @Composable get() = LuminaTheme.colors.muted
    val radius: Dp = LuminaRadius.md
    val padding: Dp = LuminaSpacing.md
}

/** Botón Encender/Apagar: con la luz apagada invita a encender (relleno lime). */
object LightToggleTokens {
    val containerWhenOff: Color @Composable get() = LuminaTheme.colors.accent
    val contentWhenOff: Color @Composable get() = LuminaTheme.colors.onAccent
    val borderWhenOff: Color @Composable get() = LuminaTheme.colors.accent
    val containerPressed: Color @Composable get() = LuminaTheme.colors.pressed
    val containerWhenOn: Color @Composable get() = LuminaTheme.colors.surface
    val contentWhenOn: Color @Composable get() = LuminaTheme.colors.text
    val borderWhenOn: Color @Composable get() = LuminaTheme.colors.border
    val radius: Dp = LuminaRadius.sm
    val minHeight: Dp = LuminaSizes.touch
}

object NavigationItemTokens {
    val container: Color @Composable get() = LuminaTheme.colors.surface
    val content: Color @Composable get() = LuminaTheme.colors.text
    val containerDisabled: Color @Composable get() = LuminaTheme.colors.surface
    val contentDisabled: Color @Composable get() = LuminaTheme.colors.muted
    val border: Color @Composable get() = LuminaTheme.colors.border
    val detail: Color @Composable get() = LuminaTheme.colors.muted
    // Estado Selected (opciones de Ajustes)
    val containerSelected: Color @Composable get() = LuminaTheme.colors.surface
    val borderSelected: Color @Composable get() = LuminaTheme.colors.accentContent
    val detailSelected: Color @Composable get() = LuminaTheme.colors.accentContent
    val radius: Dp = LuminaRadius.sm
    val minHeight: Dp = LuminaSizes.touch
    val optionMinHeight: Dp = 60.dp // fuera de escala: alto de opción en Figma
    val optionPaddingHorizontal: Dp = LuminaSpacing.md
}

object BrightnessDialTokens {
    val arc: Color @Composable get() = LuminaTheme.colors.accentContent
    val track: Color @Composable get() = LuminaTheme.colors.border
    val value: Color @Composable get() = LuminaTheme.colors.text
    val valueOff: Color @Composable get() = LuminaTheme.colors.muted
    val label: Color @Composable get() = LuminaTheme.colors.muted
    val strokeWidth: Dp = LuminaSpacing.md
    val maxWidth: Dp = 180.dp // fuera de escala: ancho de referencia del dial en Figma
    val valueTopPadding: Dp = LuminaSpacing.lg
}

object StepButtonTokens {
    val container: Color @Composable get() = LuminaTheme.colors.surface
    val content: Color @Composable get() = LuminaTheme.colors.text
    val containerDisabled: Color @Composable get() = LuminaTheme.colors.surface
    val contentDisabled: Color @Composable get() = LuminaTheme.colors.muted
    val border: Color @Composable get() = LuminaTheme.colors.border
    val radius: Dp = LuminaRadius.sm
    val minWidth: Dp = 56.dp // fuera de escala: ancho mínimo de −/+ en Figma
    val minHeight: Dp = LuminaSizes.touch
}
