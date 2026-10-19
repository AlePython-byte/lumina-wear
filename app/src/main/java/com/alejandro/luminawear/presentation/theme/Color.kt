package com.alejandro.luminawear.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Nivel 1 — Tokens globales (Figma: "Lumina / Primitives").
 * Único lugar del proyecto donde se permite declarar colores con hex.
 */
object LuminaPalette {
    // Oscuro
    val black = Color(0xFF000000)
    val surface = Color(0xFF101010)
    val white = Color(0xFFF4F4F0)
    val muted = Color(0xFF96968E)
    val border = Color(0xFF363636)
    val lime = Color(0xFFC8FF37)
    val pressed = Color(0xFFACD92F)
    val warning = Color(0xFFFFB84D)
    val danger = Color(0xFFFF5A52)

    // Claro
    val lightBackground = Color(0xFFFFFFFF)
    val lightSurface = Color(0xFFF3F5F0)
    val lightText = Color(0xFF172016)
    val lightMuted = Color(0xFF56614F)
    val lightBorder = Color(0xFF7C8775)
    val lightAccentContent = Color(0xFF456600)
    val lightWarning = Color(0xFF855000)
    val lightDanger = Color(0xFFB42318)
}
