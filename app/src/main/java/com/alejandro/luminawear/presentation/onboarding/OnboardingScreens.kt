package com.alejandro.luminawear.presentation.onboarding

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
import com.alejandro.luminawear.presentation.components.NavigationItem
import com.alejandro.luminawear.presentation.scenes.CenteredText
import com.alejandro.luminawear.presentation.scenes.SceneHeader
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography

/** Primera pantalla de una instalación nueva: presenta la app y lleva al punto de partida. */
@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
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
            SceneHeader(stringResource(R.string.app_name).uppercase(), this, transformationSpec, showDemoLabel = false)
        }
        item {
            CenteredText(
                stringResource(R.string.welcome_title), this, transformationSpec,
                LuminaTypography.title, LuminaTheme.colors.accentContent
            )
        }
        item {
            CenteredText(
                stringResource(R.string.welcome_body), this, transformationSpec,
                LuminaTypography.body, LuminaTheme.colors.muted
            )
        }
        item {
            ActionButton(stringResource(R.string.welcome_btn_start), this, transformationSpec, onStartClick)
        }
    }
}

/** Confirmación de "Volver a empezar": Cancelar es la acción principal; borrar es la secundaria. */
@Composable
fun StartOverScreen(
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
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
            SceneHeader(stringResource(R.string.start_over_header).uppercase(), this, transformationSpec, showDemoLabel = false)
        }
        item {
            CenteredText(
                stringResource(R.string.start_over_title), this, transformationSpec,
                LuminaTypography.title, LuminaTheme.colors.accentContent
            )
        }
        item {
            CenteredText(
                stringResource(R.string.start_over_body), this, transformationSpec,
                LuminaTypography.body, LuminaTheme.colors.muted
            )
        }
        item {
            ActionButton(stringResource(R.string.btn_cancel), this, transformationSpec, onCancelClick)
        }
        item {
            ActionButton(stringResource(R.string.start_over_btn_confirm), this, transformationSpec, onConfirmClick, secondary = true)
        }
    }
}

/** Punto de partida: la casa demo con sus 8 luces o una casa vacía. */
@Composable
fun StartPointScreen(
    onDemoClick: () -> Unit,
    onEmptyClick: () -> Unit,
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
            SceneHeader(stringResource(R.string.start_point_header).uppercase(), this, transformationSpec, showDemoLabel = false)
        }
        item {
            NavigationItem(
                title = stringResource(R.string.start_point_demo),
                detail = stringResource(R.string.start_point_demo_detail),
                scope = this,
                transformationSpec = transformationSpec,
                onClick = onDemoClick
            )
        }
        item {
            NavigationItem(
                title = stringResource(R.string.start_point_empty),
                detail = stringResource(R.string.start_point_empty_detail),
                scope = this,
                transformationSpec = transformationSpec,
                onClick = onEmptyClick
            )
        }
    }
}
