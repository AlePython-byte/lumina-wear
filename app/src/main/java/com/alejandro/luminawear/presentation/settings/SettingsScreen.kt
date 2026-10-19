package com.alejandro.luminawear.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.state.AppLanguage
import com.alejandro.luminawear.presentation.theme.ActionButtonTokens
import com.alejandro.luminawear.presentation.theme.LuminaBorder
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography
import com.alejandro.luminawear.presentation.theme.NavigationItemTokens
import com.alejandro.luminawear.presentation.theme.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    language: AppLanguage,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onHomeClick: () -> Unit,
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
                        text = stringResource(R.string.settings_title).uppercase(),
                        style = LuminaTypography.label,
                        color = LuminaTheme.colors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        item {
            SettingsOption(
                title = stringResource(R.string.settings_theme_light),
                detail = stringResource(R.string.settings_default),
                selected = themeMode == ThemeMode.LIGHT,
                scope = this,
                transformationSpec = transformationSpec,
                onClick = { onThemeModeChange(ThemeMode.LIGHT) }
            )
        }
        item {
            SettingsOption(
                title = stringResource(R.string.settings_theme_dark),
                detail = stringResource(R.string.settings_optional),
                selected = themeMode == ThemeMode.DARK,
                scope = this,
                transformationSpec = transformationSpec,
                onClick = { onThemeModeChange(ThemeMode.DARK) }
            )
        }
        item {
            Text(
                text = stringResource(R.string.help_swipe_more),
                style = LuminaTypography.label,
                color = LuminaTheme.colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LuminaSpacing.sm)
                    .transformedHeight(this, transformationSpec)
            )
        }
        item {
            Text(
                text = stringResource(R.string.settings_language_title).uppercase(),
                style = LuminaTypography.label,
                color = LuminaTheme.colors.text,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec)
            )
        }
        item {
            SettingsOption(
                title = stringResource(R.string.settings_language_es),
                detail = stringResource(R.string.settings_default),
                selected = language == AppLanguage.ES,
                scope = this,
                transformationSpec = transformationSpec,
                onClick = { if (language != AppLanguage.ES) onLanguageChange(AppLanguage.ES) }
            )
        }
        item {
            SettingsOption(
                title = stringResource(R.string.settings_language_en),
                detail = stringResource(R.string.settings_optional),
                selected = language == AppLanguage.EN,
                scope = this,
                transformationSpec = transformationSpec,
                onClick = { if (language != AppLanguage.EN) onLanguageChange(AppLanguage.EN) }
            )
        }
        item {
            Button(
                onClick = onHomeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActionButtonTokens.containerSecondary,
                    contentColor = ActionButtonTokens.contentSecondary
                ),
                shape = RoundedCornerShape(ActionButtonTokens.radius),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = LuminaSpacing.sm)
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
        item {
            Text(
                text = stringResource(R.string.settings_note),
                style = LuminaTypography.label,
                color = LuminaTheme.colors.muted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LuminaSpacing.sm)
                    .transformedHeight(this, transformationSpec)
            )
        }
    }
}

/** NavigationItem de 60 dp con estado Selected y semántica de RadioButton. */
@Composable
private fun SettingsOption(
    title: String,
    detail: String,
    selected: Boolean,
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(NavigationItemTokens.radius)
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(scope, transformationSpec)
            .heightIn(min = NavigationItemTokens.optionMinHeight)
            .clip(shape)
            .background(if (selected) NavigationItemTokens.containerSelected else NavigationItemTokens.container)
            .border(
                LuminaBorder.width,
                if (selected) NavigationItemTokens.borderSelected else NavigationItemTokens.border,
                shape
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
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
            color = if (selected) NavigationItemTokens.detailSelected else NavigationItemTokens.detail
        )
    }
}
