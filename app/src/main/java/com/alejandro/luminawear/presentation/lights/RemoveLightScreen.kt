package com.alejandro.luminawear.presentation.lights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.lazy.TransformationSpec
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.components.ActionButton
import com.alejandro.luminawear.presentation.models.Light
import com.alejandro.luminawear.presentation.models.lightName
import com.alejandro.luminawear.presentation.scenes.CenteredText
import com.alejandro.luminawear.presentation.scenes.SceneHeader
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography

/** Confirmación de "Quitar luz": Conservar es la acción principal; quitar es la secundaria. */
@Composable
fun RemoveLightScreen(
    light: Light,
    roomNameRes: Int,
    onKeepClick: () -> Unit,
    onRemoveClick: () -> Unit,
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
            SceneHeader(stringResource(R.string.remove_light_header).uppercase(), this, transformationSpec, showDemoLabel = false)
        }
        item {
            CenteredText(
                stringResource(R.string.remove_light_title, lightName(light)), this, transformationSpec,
                LuminaTypography.title, LuminaTheme.colors.accentContent
            )
        }
        item {
            CenteredText(
                stringResource(R.string.remove_light_body, stringResource(roomNameRes)), this, transformationSpec,
                LuminaTypography.body, LuminaTheme.colors.muted
            )
        }
        item {
            ActionButton(stringResource(R.string.remove_light_btn_keep), this, transformationSpec, onKeepClick)
        }
        item {
            ActionButton(stringResource(R.string.remove_light_btn_remove), this, transformationSpec, onRemoveClick, secondary = true)
        }
    }
}
