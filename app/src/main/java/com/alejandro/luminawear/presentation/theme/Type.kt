package com.alejandro.luminawear.presentation.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.alejandro.luminawear.R

/**
 * Familia tipográfica de la app: Roboto Flex (fuente variable, SIL OFL 1.1).
 * Es el único lugar donde se define el FontFamily.
 */
@OptIn(ExperimentalTextApi::class)
val LuminaFontFamily: FontFamily = FontFamily(
    Font(
        R.font.roboto_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.roboto_flex,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        R.font.roboto_flex,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(FontVariation.weight(900))
    )
)

/** Estilos de texto de Figma (tamaño/interlineado en sp). */
object LuminaTypography {
    val display = TextStyle(fontFamily = LuminaFontFamily, fontWeight = FontWeight.Black, fontSize = 48.sp, lineHeight = 48.sp)
    val title = TextStyle(fontFamily = LuminaFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 24.sp)
    val body = TextStyle(fontFamily = LuminaFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 18.sp)
    val button = TextStyle(fontFamily = LuminaFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp)
    val label = TextStyle(fontFamily = LuminaFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)
    val time = TextStyle(fontFamily = LuminaFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)
}
