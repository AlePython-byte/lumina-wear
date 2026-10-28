package com.alejandro.luminawear.presentation.home

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
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
import com.alejandro.luminawear.presentation.models.Scene
import com.alejandro.luminawear.presentation.scenes.CenteredText
import com.alejandro.luminawear.presentation.scenes.sceneRoomPercentSpaced
import com.alejandro.luminawear.presentation.theme.ActionButtonTokens
import com.alejandro.luminawear.presentation.theme.LuminaBorder
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography
import com.alejandro.luminawear.presentation.theme.NavigationItemTokens
import com.alejandro.luminawear.presentation.theme.SceneCardTokens

/**
 * Inicio. Con luces: contador, Apagar todo, sugerencia y accesos. Sin luces: estado vacío con Ajustes
 * y los accesos que siguen teniendo sentido (Habitaciones, Favoritos, Asistente).
 */
@Composable
fun HomeScreen(
    lightsOn: Int,
    totalLights: Int,
    onTurnOffAllClick: () -> Unit,
    onRoomsClick: () -> Unit,
    onScenesClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onAssistantClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onAddLightClick: () -> Unit = {},
    suggestion: Scene? = null,
    onSuggestionClick: () -> Unit = {},
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isEmpty = totalLights == 0
        item {
            ListHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Sin luces no hay casa demo que señalar.
                    if (!isEmpty) {
                        Text(
                            text = stringResource(R.string.header_demo),
                            style = LuminaTypography.label,
                            color = LuminaTheme.colors.accentContent,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = stringResource(R.string.header_my_home),
                        style = LuminaTypography.button,
                        color = LuminaTheme.colors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        if (isEmpty) {
            item {
                CenteredText(
                    stringResource(R.string.home_empty_title), this, transformationSpec,
                    LuminaTypography.title, LuminaTheme.colors.accentContent
                )
            }
            item {
                CenteredText(
                    stringResource(R.string.home_empty_body), this, transformationSpec,
                    LuminaTypography.body, LuminaTheme.colors.muted
                )
            }
            item {
                ActionButton(stringResource(R.string.add_light_btn_add), this, transformationSpec, onAddLightClick)
            }
            item {
                ActionButton(stringResource(R.string.access_settings), this, transformationSpec, onSettingsClick, secondary = true)
            }
            item { SecondaryAccessButton(stringResource(R.string.access_rooms), this, transformationSpec, true, onRoomsClick) }
            item { SecondaryAccessButton(stringResource(R.string.access_favorites), this, transformationSpec, true, onFavoritesClick) }
            item { SecondaryAccessButton(stringResource(R.string.access_assistant), this, transformationSpec, true, onAssistantClick) }
            return@TransformingLazyColumn
        }
        item {
            val subtitle = pluralStringResource(R.plurals.lights_on_subtitle, lightsOn)
            val counterDesc = stringResource(R.string.lights_counter_a11y, lightsOn, totalLights, subtitle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec)
                    .padding(vertical = LuminaSpacing.sm)
                    .semantics(mergeDescendants = true) { contentDescription = counterDesc },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.lights_counter, lightsOn, totalLights),
                    style = LuminaTypography.display,
                    color = LuminaTheme.colors.accentContent
                )
                Text(
                    text = subtitle,
                    style = LuminaTypography.body,
                    color = LuminaTheme.colors.muted
                )
            }
        }
        item {
            val allOff = lightsOn == 0
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            Button(
                onClick = onTurnOffAllClick,
                enabled = !allOff,
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPressed) ActionButtonTokens.containerPressed else ActionButtonTokens.containerDefault,
                    contentColor = ActionButtonTokens.contentDefault,
                    disabledContainerColor = ActionButtonTokens.containerDisabled,
                    disabledContentColor = ActionButtonTokens.contentDisabled
                ),
                shape = RoundedCornerShape(ActionButtonTokens.radius),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LuminaSpacing.xs)
                    .heightIn(min = ActionButtonTokens.minHeight)
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Text(
                    text = if (allOff) stringResource(R.string.btn_all_off) else stringResource(R.string.btn_turn_off_all),
                    style = LuminaTypography.button
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.help_swipe_more),
                style = LuminaTypography.time,
                color = LuminaTheme.colors.muted,
                modifier = Modifier
                    .padding(top = LuminaSpacing.lg, bottom = LuminaSpacing.sm)
                    .transformedHeight(this, transformationSpec)
            )
        }
        if (suggestion != null) {
            item {
                Card(
                    onClick = onSuggestionClick,
                    colors = CardDefaults.cardColors(
                        containerColor = SceneCardTokens.container
                    ),
                    shape = RoundedCornerShape(SceneCardTokens.radius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = LuminaSpacing.xs)
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Column(modifier = Modifier.padding(SceneCardTokens.padding)) {
                        Text(
                            text = stringResource(R.string.card_by_time),
                            style = LuminaTypography.label,
                            color = SceneCardTokens.eyebrow
                        )
                        Text(
                            text = stringResource(suggestion.nameRes),
                            style = LuminaTypography.title,
                            color = SceneCardTokens.title
                        )
                        Text(
                            text = sceneRoomPercentSpaced(suggestion),
                            style = LuminaTypography.body,
                            color = SceneCardTokens.subtitle
                        )
                    }
                }
            }
        }
        item { SecondaryAccessButton(stringResource(R.string.access_rooms), this, transformationSpec, true, onRoomsClick) }
        item { SecondaryAccessButton(stringResource(R.string.access_scenes), this, transformationSpec, true, onScenesClick) }
        item { SecondaryAccessButton(stringResource(R.string.access_favorites), this, transformationSpec, true, onFavoritesClick) }
        item { SecondaryAccessButton(stringResource(R.string.access_assistant), this, transformationSpec, true, onAssistantClick) }
        item { SecondaryAccessButton(stringResource(R.string.access_settings), this, transformationSpec, true, onSettingsClick) }
    }
}

@Composable
fun SecondaryAccessButton(
    text: String,
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    isEnabled: Boolean = false,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = NavigationItemTokens.container,
            contentColor = NavigationItemTokens.content,
            disabledContainerColor = NavigationItemTokens.containerDisabled,
            disabledContentColor = NavigationItemTokens.contentDisabled
        ),
        shape = RoundedCornerShape(NavigationItemTokens.radius),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LuminaSpacing.xs)
            .heightIn(min = NavigationItemTokens.minHeight)
            .border(LuminaBorder.width, NavigationItemTokens.border, RoundedCornerShape(NavigationItemTokens.radius))
            .transformedHeight(scope, transformationSpec),
        transformation = with(scope) { SurfaceTransformation(transformationSpec) }
    ) {
        Text(text = text, style = LuminaTypography.body)
    }
}
