package com.alejandro.luminawear.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.alejandro.luminawear.presentation.theme.LuminaBorder
import com.alejandro.luminawear.presentation.theme.LuminaTypography
import com.alejandro.luminawear.presentation.theme.NavigationItemTokens

/**
 * NavigationItem de 60 dp con título y detalle.
 * `selected == null`: ítem que navega (rol Button). Con valor: opción exclusiva (rol RadioButton).
 */
@Composable
fun NavigationItem(
    title: String,
    detail: String,
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    onClick: () -> Unit,
    selected: Boolean? = null
) {
    val isSelected = selected == true
    val shape = RoundedCornerShape(NavigationItemTokens.radius)
    val interaction = if (selected == null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    }
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(scope, transformationSpec)
            .heightIn(min = NavigationItemTokens.optionMinHeight)
            .clip(shape)
            .background(if (isSelected) NavigationItemTokens.containerSelected else NavigationItemTokens.container)
            .border(
                LuminaBorder.width,
                if (isSelected) NavigationItemTokens.borderSelected else NavigationItemTokens.border,
                shape
            )
            .then(interaction)
            .padding(horizontal = NavigationItemTokens.optionPaddingHorizontal)
    ) {
        Text(
            text = title,
            style = LuminaTypography.button,
            color = NavigationItemTokens.content
        )
        Text(
            text = detail,
            style = LuminaTypography.label,
            color = if (isSelected) NavigationItemTokens.detailSelected else NavigationItemTokens.detail
        )
    }
}
