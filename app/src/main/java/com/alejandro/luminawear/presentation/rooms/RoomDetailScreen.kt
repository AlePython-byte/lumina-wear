package com.alejandro.luminawear.presentation.rooms

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.models.Light
import com.alejandro.luminawear.presentation.state.LightStateHolder
import com.alejandro.luminawear.presentation.theme.ActionButtonTokens
import com.alejandro.luminawear.presentation.theme.LuminaBorder
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography
import com.alejandro.luminawear.presentation.theme.RoomControlTokens
import com.alejandro.luminawear.presentation.theme.StatusChipTokens

@Composable
fun RoomDetailScreen(
    roomNameRes: Int,
    lights: List<Light>,
    lightStateHolder: LightStateHolder,
    onLightClick: (String) -> Unit,
    onBackClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
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
                        text = stringResource(roomNameRes).uppercase(),
                        style = LuminaTypography.button,
                        color = LuminaTheme.colors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        items(lights.size) { index ->
            val light = lights[index]
            val state = lightStateHolder.states[light.id]
            val isOn = state?.isOn == true
            val brightness = state?.brightness ?: light.defaultBrightness

            Card(
                onClick = { onLightClick(light.id) },
                colors = CardDefaults.cardColors(
                    containerColor = RoomControlTokens.container
                ),
                shape = RoundedCornerShape(RoomControlTokens.radius),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LuminaSpacing.xs)
                    .heightIn(min = RoomControlTokens.minHeight)
                    .border(LuminaBorder.width, RoomControlTokens.border, RoundedCornerShape(RoomControlTokens.radius))
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Column(modifier = Modifier.padding(RoomControlTokens.padding)) {
                    Text(
                        text = stringResource(light.nameRes),
                        style = LuminaTypography.button,
                        color = RoomControlTokens.title
                    )
                    Spacer(modifier = Modifier.height(LuminaSpacing.xs))
                    Text(
                        text = if (isOn) stringResource(R.string.light_state_on_percent, brightness)
                               else stringResource(R.string.light_state_off),
                        style = LuminaTypography.time,
                        color = if (isOn) StatusChipTokens.contentActive else StatusChipTokens.contentInactive
                    )
                }
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
                    text = stringResource(R.string.access_rooms),
                    style = LuminaTypography.button
                )
            }
        }
    }
}
