package com.alejandro.luminawear.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.alejandro.luminawear.presentation.theme.ActionButtonTokens
import com.alejandro.luminawear.presentation.theme.LuminaBorder
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTypography

/** ActionButton de 48 dp: primario (relleno accent) o secundario (surface con borde). */
@Composable
fun ActionButton(
    text: String,
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    onClick: () -> Unit,
    secondary: Boolean = false,
    description: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(ActionButtonTokens.radius)
    val container = when {
        secondary -> ActionButtonTokens.containerSecondary
        isPressed -> ActionButtonTokens.containerPressed
        else -> ActionButtonTokens.containerDefault
    }
    val content = if (secondary) ActionButtonTokens.contentSecondary else ActionButtonTokens.contentDefault
    val borderModifier = if (secondary) {
        Modifier.border(LuminaBorder.width, ActionButtonTokens.borderSecondary, shape)
    } else {
        Modifier
    }
    val semanticsModifier = if (description != null) {
        Modifier.semantics { contentDescription = description }
    } else {
        Modifier
    }
    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LuminaSpacing.xs)
            .heightIn(min = ActionButtonTokens.minHeight)
            .then(borderModifier)
            .then(semanticsModifier)
            .transformedHeight(scope, transformationSpec),
        transformation = with(scope) { SurfaceTransformation(transformationSpec) }
    ) {
        Text(text = text, style = LuminaTypography.button)
    }
}
