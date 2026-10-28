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
import com.alejandro.luminawear.presentation.components.ActionButton
import com.alejandro.luminawear.presentation.models.Room
import com.alejandro.luminawear.presentation.theme.ActionButtonTokens
import com.alejandro.luminawear.presentation.theme.LuminaBorder
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography
import com.alejandro.luminawear.presentation.theme.RoomControlTokens

@Composable
fun RoomsScreen(
    rooms: List<Room>,
    getLightsCount: (String) -> Pair<Int, Int>,
    onRoomClick: (String) -> Unit,
    onAddLightClick: () -> Unit = {},
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
                        text = stringResource(R.string.header_rooms),
                        style = LuminaTypography.button,
                        color = LuminaTheme.colors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        items(rooms.size) { index ->
            val room = rooms[index]
            val (onCount, totalCount) = getLightsCount(room.id)
            Card(
                onClick = { onRoomClick(room.id) },
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
                        text = stringResource(room.nameRes),
                        style = LuminaTypography.button,
                        color = RoomControlTokens.title
                    )
                    Spacer(modifier = Modifier.height(LuminaSpacing.xs))
                    Text(
                        text = if (totalCount == 0) stringResource(R.string.room_no_lights)
                               else stringResource(R.string.room_lights_counter, onCount, totalCount),
                        style = LuminaTypography.time,
                        color = RoomControlTokens.subtitle
                    )
                }
            }
        }

        item {
            ActionButton(stringResource(R.string.add_light_btn_add), this, transformationSpec, onAddLightClick, secondary = true)
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
                    text = stringResource(R.string.btn_home),
                    style = LuminaTypography.button
                )
            }
        }
    }
}
