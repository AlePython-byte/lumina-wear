package com.alejandro.luminawear.presentation.lights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.lazy.TransformationSpec
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.components.ActionButton
import com.alejandro.luminawear.presentation.components.NavigationItem
import com.alejandro.luminawear.presentation.models.LightType
import com.alejandro.luminawear.presentation.models.Room
import com.alejandro.luminawear.presentation.scenes.SceneHeader
import com.alejandro.luminawear.presentation.theme.LuminaSpacing

/**
 * Añadir luz, paso 1 de 2: elegir habitación. `lightsInRoom` da cuántas luces tiene cada una;
 * `isFull` marca las que ya no admiten más (estado Disabled, no se pueden elegir).
 */
@Composable
fun AddLightRoomScreen(
    rooms: List<Room>,
    lightsInRoom: (String) -> Int,
    isFull: (String) -> Boolean,
    onRoomClick: (String) -> Unit,
    onCancelClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LuminaSpacing.xs)
    ) {
        item {
            SceneHeader(stringResource(R.string.add_light_room_header).uppercase(), this, transformationSpec, showDemoLabel = false)
        }
        items(rooms.size) { index ->
            val room = rooms[index]
            val count = lightsInRoom(room.id)
            val full = isFull(room.id)
            NavigationItem(
                title = stringResource(room.nameRes),
                detail = when {
                    full -> stringResource(R.string.add_light_room_full)
                    count == 0 -> stringResource(R.string.room_no_lights)
                    else -> pluralStringResource(R.plurals.add_light_room_count, count, count)
                },
                scope = this,
                transformationSpec = transformationSpec,
                onClick = { onRoomClick(room.id) },
                enabled = !full
            )
        }
        item {
            ActionButton(stringResource(R.string.btn_cancel), this, transformationSpec, onCancelClick, secondary = true)
        }
    }
}

/** Añadir luz, paso 2 de 2: elegir el tipo entre los que se pueden añadir ahora. */
@Composable
fun AddLightTypeScreen(
    roomNameRes: Int,
    types: List<LightType>,
    onTypeClick: (LightType) -> Unit,
    onBackClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LuminaSpacing.xs)
    ) {
        item {
            SceneHeader(
                stringResource(R.string.add_light_type_header, stringResource(roomNameRes)).uppercase(),
                this, transformationSpec, showDemoLabel = false
            )
        }
        items(types.size) { index ->
            val type = types[index]
            NavigationItem(
                title = if (type == LightType.HUB_LAMP) stringResource(R.string.add_light_type_hub) else stringResource(type.nameRes),
                detail = stringResource(addLightTypeDetailRes(type)),
                scope = this,
                transformationSpec = transformationSpec,
                onClick = { onTypeClick(type) }
            )
        }
        item {
            ActionButton(stringResource(R.string.add_light_btn_back), this, transformationSpec, onBackClick, secondary = true)
        }
    }
}

private fun addLightTypeDetailRes(type: LightType): Int = when (type) {
    LightType.PLAFON -> R.string.add_light_type_plafon_detail
    LightType.LAMPARA -> R.string.add_light_type_lampara_detail
    LightType.TIRA -> R.string.add_light_type_tira_detail
    else -> R.string.add_light_type_hub_detail
}
