package com.alejandro.luminawear.presentation.lights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.lazy.TransformationSpec
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.components.ActionButton
import com.alejandro.luminawear.presentation.scenes.CenteredText
import com.alejandro.luminawear.presentation.scenes.SceneHeader
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography

/** Estado CONECTANDO (Figma 12:632): la orden al hub está en curso. */
@Composable
fun HubConnectingScreen(
    onCancelClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    HubStatusScreen(
        header = stringResource(R.string.hub_connecting_header),
        title = stringResource(R.string.hub_connecting_title),
        body = stringResource(R.string.hub_connecting_body),
        contentPadding = contentPadding,
        listState = listState,
        transformationSpec = transformationSpec
    ) {
        item {
            ActionButton(stringResource(R.string.btn_cancel), this, transformationSpec, onCancelClick, secondary = true)
        }
    }
}

/** Estado NO SE PUDO (Figma 12:640): la luz volvió a su último estado confirmado. */
@Composable
fun HubErrorScreen(
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    HubStatusScreen(
        header = stringResource(R.string.hub_error_header),
        title = stringResource(R.string.hub_error_title),
        body = stringResource(R.string.hub_error_body),
        contentPadding = contentPadding,
        listState = listState,
        transformationSpec = transformationSpec
    ) {
        item {
            ActionButton(stringResource(R.string.btn_retry), this, transformationSpec, onRetryClick)
        }
        item {
            ActionButton(stringResource(R.string.btn_back), this, transformationSpec, onBackClick, secondary = true)
        }
    }
}

@Composable
private fun HubStatusScreen(
    header: String,
    title: String,
    body: String,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec,
    actions: TransformingLazyColumnScope.() -> Unit
) {
    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LuminaSpacing.xs)
    ) {
        item {
            SceneHeader(
                title = header.uppercase(),
                scope = this,
                transformationSpec = transformationSpec,
                // Región activa educada: TalkBack anuncia "Conectando" / "No se pudo".
                titleModifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                titleStyle = LuminaTypography.label
            )
        }
        item {
            CenteredText(title, this, transformationSpec, LuminaTypography.title, LuminaTheme.colors.text)
        }
        item {
            CenteredText(body, this, transformationSpec, LuminaTypography.body, LuminaTheme.colors.muted)
        }
        actions()
    }
}
