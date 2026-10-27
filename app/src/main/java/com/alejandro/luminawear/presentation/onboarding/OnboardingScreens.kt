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
            SceneHeader(stringResource(R.string.app_name).uppercase(), this, transformationSpec)
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
