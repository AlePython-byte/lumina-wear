package com.alejandro.luminawear.presentation.lights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.models.Light
import com.alejandro.luminawear.presentation.models.lightName
import com.alejandro.luminawear.presentation.theme.ActionButtonTokens
import com.alejandro.luminawear.presentation.theme.BrightnessDialTokens
import com.alejandro.luminawear.presentation.theme.LightToggleTokens
import com.alejandro.luminawear.presentation.theme.LuminaBorder
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography
import com.alejandro.luminawear.presentation.theme.StepButtonTokens

@Composable
fun LightControlScreen(
    light: Light,
    roomNameRes: Int,
    isOn: Boolean,
    brightness: Int,
    onBrightnessChange: (Int) -> Unit,
    onTogglePower: () -> Unit,
    onBackClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    val roomName = stringResource(roomNameRes)
    val title = stringResource(R.string.light_control_title, lightName(light), roomName).uppercase()

    val decreaseDesc = stringResource(R.string.light_control_decrease)
    val increaseDesc = stringResource(R.string.light_control_increase)
    val offDesc = stringResource(R.string.light_control_off_accessible)
    val brightnessText = stringResource(R.string.light_control_brightness_pct, brightness)
    val brightnessDesc = stringResource(R.string.light_control_brightness_a11y, brightness)

    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ListHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.header_demo),
                        style = LuminaTypography.label,
                        color = LuminaTheme.colors.accentContent,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = title,
                        style = LuminaTypography.button,
                        color = LuminaTheme.colors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            val arcColor = BrightnessDialTokens.arc
            val trackColor = BrightnessDialTokens.track

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LuminaSpacing.sm)
                    .transformedHeight(this, transformationSpec),
                contentAlignment = Alignment.Center
            ) {
                // Semicircular Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = BrightnessDialTokens.maxWidth)
                        .aspectRatio(2f)
                ) {
                    val strokeWidth = BrightnessDialTokens.strokeWidth.toPx()
                    val style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    val inset = strokeWidth / 2f
                    val arcSize = Size(size.width - strokeWidth, size.width - strokeWidth)
                    val arcOffset = Offset(inset, inset)

                    // Background track
                    drawArc(
                        color = trackColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = arcOffset,
                        size = arcSize,
                        style = style
                    )

                    // Foreground track
                    if (isOn) {
                        val sweep = (brightness / 100f) * 180f
                        drawArc(
                            color = arcColor,
                            startAngle = 180f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = style
                        )
                    }
                }

                // Central text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = BrightnessDialTokens.valueTopPadding).semantics(mergeDescendants = true) {
                        contentDescription = if (isOn) brightnessDesc else offDesc
                    }
                ) {
                    Text(
                        text = if (isOn) brightnessText else stringResource(R.string.light_control_off_state),
                        style = LuminaTypography.display,
                        color = if (isOn) BrightnessDialTokens.value else BrightnessDialTokens.valueOff
                    )
                    Text(
                        text = if (isOn) stringResource(R.string.light_control_brightness) else stringResource(R.string.light_state_off),
                        style = LuminaTypography.time,
                        color = BrightnessDialTokens.label
                    )
                }
            }
        }

        if (isOn) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = LuminaSpacing.xs)
                        .transformedHeight(this, transformationSpec),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val decreaseEnabled = brightness > 1
                    val increaseEnabled = brightness < 100

                    StepButton(
                        symbol = stringResource(R.string.btn_decrease),
                        description = decreaseDesc,
                        enabled = decreaseEnabled,
                        onClick = { if (decreaseEnabled) onBrightnessChange(brightness - 10) }
                    )

                    Spacer(modifier = Modifier.width(LuminaSpacing.sm))

                    StepButton(
                        symbol = stringResource(R.string.btn_increase),
                        description = increaseDesc,
                        enabled = increaseEnabled,
                        onClick = { if (increaseEnabled) onBrightnessChange(brightness + 10) }
                    )
                }
            }
        }

        item {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val containerColor = when {
                isOn -> LightToggleTokens.containerWhenOn
                isPressed -> LightToggleTokens.containerPressed
                else -> LightToggleTokens.containerWhenOff
            }
            val borderColor = if (isOn) LightToggleTokens.borderWhenOn else LightToggleTokens.borderWhenOff
            Button(
                onClick = onTogglePower,
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = if (isOn) LightToggleTokens.contentWhenOn else LightToggleTokens.contentWhenOff
                ),
                shape = RoundedCornerShape(LightToggleTokens.radius),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LuminaSpacing.xs)
                    .heightIn(min = LightToggleTokens.minHeight)
                    .border(
                        width = LuminaBorder.width,
                        color = borderColor,
                        shape = RoundedCornerShape(LightToggleTokens.radius)
                    )
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Text(
                    text = if (isOn) stringResource(R.string.btn_turn_off) else stringResource(R.string.btn_turn_on),
                    style = LuminaTypography.button
                )
            }
        }

        item {
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActionButtonTokens.containerSecondary,
                    contentColor = ActionButtonTokens.contentSecondary
                ),
                shape = RoundedCornerShape(ActionButtonTokens.radius),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LuminaSpacing.sm)
                    .heightIn(min = ActionButtonTokens.minHeight)
                    .border(LuminaBorder.width, ActionButtonTokens.borderSecondary, RoundedCornerShape(ActionButtonTokens.radius))
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Text(
                    text = stringResource(R.string.btn_back_to_room, roomName),
                    style = LuminaTypography.button
                )
            }
        }
    }
}

@Composable
private fun StepButton(
    symbol: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = StepButtonTokens.container,
            contentColor = StepButtonTokens.content,
            disabledContainerColor = StepButtonTokens.containerDisabled,
            disabledContentColor = StepButtonTokens.contentDisabled
        ),
        shape = RoundedCornerShape(StepButtonTokens.radius),
        modifier = Modifier
            .widthIn(min = StepButtonTokens.minWidth)
            .heightIn(min = StepButtonTokens.minHeight)
            .border(LuminaBorder.width, StepButtonTokens.border, RoundedCornerShape(StepButtonTokens.radius))
            .semantics { contentDescription = description }
    ) {
        // El símbolo es decorativo: el botón se anuncia solo con su descripción.
        Text(text = symbol, style = LuminaTypography.title, modifier = Modifier.clearAndSetSemantics {})
    }
}
