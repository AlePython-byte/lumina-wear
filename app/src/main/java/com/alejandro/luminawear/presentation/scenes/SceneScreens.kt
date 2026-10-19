package com.alejandro.luminawear.presentation.scenes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.components.ActionButton
import com.alejandro.luminawear.presentation.components.NavigationItem
import com.alejandro.luminawear.presentation.models.Scene
import com.alejandro.luminawear.presentation.models.appRooms
import com.alejandro.luminawear.presentation.models.appScenes
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography

/** Nombre de la habitación objetivo de la escena ("Toda la casa" si no tiene). */
@Composable
fun sceneRoomName(scene: Scene): String {
    val room = appRooms.firstOrNull { it.id == scene.roomId }
    return stringResource(room?.nameRes ?: R.string.scene_room_all)
}

/** "Sala · 30 %" con espacio (listas, tarjetas y sugerencia). */
@Composable
fun sceneRoomPercentSpaced(scene: Scene): String =
    stringResource(R.string.card_room_percent, sceneRoomName(scene), scene.percent)

/** "Sala · 30%" pegado (pantalla de escena activa). */
@Composable
private fun sceneRoomPercentTight(scene: Scene): String =
    stringResource(R.string.scene_room_percent_tight, sceneRoomName(scene), scene.percent)

@Composable
private fun SceneHeader(
    title: String,
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    titleModifier: Modifier = Modifier,
    titleStyle: TextStyle = LuminaTypography.button
) {
    with(scope) {
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
                    style = titleStyle,
                    color = LuminaTheme.colors.text,
                    textAlign = TextAlign.Center,
                    modifier = titleModifier
                )
            }
        }
    }
}

@Composable
private fun CenteredText(
    text: String,
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    style: TextStyle,
    color: Color
) {
    Text(
        text = text,
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LuminaSpacing.xs)
            .transformedHeight(scope, transformationSpec)
    )
}

@Composable
fun ScenesScreen(
    onSceneClick: (String) -> Unit,
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
            SceneHeader(stringResource(R.string.access_scenes).uppercase(), this, transformationSpec)
        }
        appScenes.forEach { scene ->
            item {
                NavigationItem(
                    title = stringResource(scene.nameRes),
                    detail = if (scene.percent == 0) {
                        stringResource(R.string.scene_all_off)
                    } else {
                        sceneRoomPercentSpaced(scene)
                    },
                    scope = this,
                    transformationSpec = transformationSpec,
                    onClick = { onSceneClick(scene.id) }
                )
            }
        }
        item {
            ActionButton(stringResource(R.string.btn_home), this, transformationSpec, onHomeClick, secondary = true)
        }
    }
}

@Composable
fun SceneDetailScreen(
    scene: Scene,
    onActivateClick: () -> Unit,
    onScenesClick: () -> Unit,
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
            SceneHeader(stringResource(scene.nameRes).uppercase(), this, transformationSpec)
        }
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec)
            ) {
                Text(
                    text = stringResource(R.string.light_control_brightness_pct, scene.percent),
                    style = LuminaTypography.display,
                    color = LuminaTheme.colors.accentContent
                )
                Text(
                    text = sceneRoomName(scene),
                    style = LuminaTypography.body,
                    color = LuminaTheme.colors.muted
                )
            }
        }
        item {
            ActionButton(stringResource(R.string.scene_btn_activate), this, transformationSpec, onActivateClick)
        }
        item {
            CenteredText(
                stringResource(scene.descriptionRes), this, transformationSpec,
                LuminaTypography.body, LuminaTheme.colors.text
            )
        }
        item {
            ActionButton(stringResource(R.string.access_scenes), this, transformationSpec, onScenesClick, secondary = true)
        }
    }
}

@Composable
fun SceneActiveScreen(
    scene: Scene,
    onUndoClick: () -> Unit,
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
            SceneHeader(
                title = stringResource(R.string.scene_active_title).uppercase(),
                scope = this,
                transformationSpec = transformationSpec,
                // Región activa educada: TalkBack anuncia que la escena quedó activa.
                titleModifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                titleStyle = LuminaTypography.label
            )
        }
        item {
            CenteredText(
                stringResource(scene.nameRes), this, transformationSpec,
                LuminaTypography.title, LuminaTheme.colors.accentContent
            )
        }
        item {
            CenteredText(
                sceneRoomPercentTight(scene), this, transformationSpec,
                LuminaTypography.body, LuminaTheme.colors.muted
            )
        }
        item {
            ActionButton(
                text = stringResource(R.string.scene_btn_undo),
                scope = this,
                transformationSpec = transformationSpec,
                onClick = onUndoClick,
                secondary = true,
                description = stringResource(R.string.scene_btn_undo_desc)
            )
        }
        item {
            ActionButton(stringResource(R.string.btn_home), this, transformationSpec, onHomeClick, secondary = true)
        }
    }
}

@Composable
fun SuggestionScreen(
    scene: Scene,
    onActivateClick: () -> Unit,
    onViewSceneClick: () -> Unit,
    onDismissClick: () -> Unit,
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
            SceneHeader(stringResource(R.string.suggestion_title).uppercase(), this, transformationSpec)
        }
        item {
            CenteredText(
                stringResource(scene.nameRes), this, transformationSpec,
                LuminaTypography.title, LuminaTheme.colors.accentContent
            )
        }
        item {
            CenteredText(
                sceneRoomPercentSpaced(scene), this, transformationSpec,
                LuminaTypography.body, LuminaTheme.colors.muted
            )
        }
        item {
            ActionButton(stringResource(R.string.scene_btn_activate), this, transformationSpec, onActivateClick)
        }
        item {
            CenteredText(
                stringResource(scene.suggestionRes), this, transformationSpec,
                LuminaTypography.body, LuminaTheme.colors.text
            )
        }
        item {
            ActionButton(stringResource(R.string.suggestion_btn_view), this, transformationSpec, onViewSceneClick, secondary = true)
        }
        item {
            ActionButton(stringResource(R.string.suggestion_btn_dismiss), this, transformationSpec, onDismissClick, secondary = true)
        }
        item {
            CenteredText(
                stringResource(R.string.suggestion_note), this, transformationSpec,
                LuminaTypography.label, LuminaTheme.colors.muted
            )
        }
    }
}
