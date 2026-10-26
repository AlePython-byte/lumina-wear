package com.alejandro.luminawear.presentation.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnVisibleItemInfo
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.alejandro.luminawear.R
import com.alejandro.luminawear.data.HUB_LIGHT_ID
import com.alejandro.luminawear.presentation.components.ActionButton
import com.alejandro.luminawear.presentation.components.NavigationItem
import com.alejandro.luminawear.presentation.lights.HubStatusScreen
import com.alejandro.luminawear.presentation.models.appLights
import com.alejandro.luminawear.presentation.models.appRooms
import com.alejandro.luminawear.presentation.models.sceneById
import com.alejandro.luminawear.presentation.scenes.CenteredText
import com.alejandro.luminawear.presentation.scenes.SceneHeader
import com.alejandro.luminawear.presentation.scenes.sceneRoomPercentSpaced
import com.alejandro.luminawear.presentation.state.ASSISTANT_MAX_TEXT
import com.alejandro.luminawear.presentation.state.AssistantAction
import com.alejandro.luminawear.presentation.state.AssistantError
import com.alejandro.luminawear.presentation.theme.LuminaBorder
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography
import com.alejandro.luminawear.presentation.theme.TextInputTokens

/** Frases rápidas y acceso a "Escribir". Cada frase pasa por las mismas reglas que el texto escrito. */
@Composable
fun AssistantScreen(
    onPhraseClick: (String) -> Unit,
    onWriteClick: () -> Unit,
    onHomeClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    val lamp = appLights.first { it.id == HUB_LIGHT_ID }
    val lampRoom = stringResource(appRooms.first { it.id == lamp.roomId }.nameRes)
    val phrases = listOf(
        stringResource(R.string.assistant_phrase_off_all) to stringResource(R.string.assistant_detail_all_lights),
        stringResource(R.string.assistant_phrase_read) to sceneRoomPercentSpaced(sceneById("read")!!),
        stringResource(R.string.assistant_phrase_relax) to sceneRoomPercentSpaced(sceneById("relax")!!),
        stringResource(R.string.assistant_phrase_lamp_on) to stringResource(R.string.light_control_title, stringResource(lamp.nameRes), lampRoom),
        stringResource(R.string.assistant_phrase_lamp_50) to stringResource(R.string.card_room_percent, lampRoom, 50)
    )
    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LuminaSpacing.xs)
    ) {
        item {
            SceneHeader(stringResource(R.string.access_assistant).uppercase(), this, transformationSpec)
        }
        item {
            CenteredText(
                stringResource(R.string.assistant_quick_header), this, transformationSpec,
                LuminaTypography.label, LuminaTheme.colors.muted
            )
        }
        phrases.forEach { (phrase, detail) ->
            item {
                NavigationItem(
                    title = phrase,
                    detail = detail,
                    scope = this,
                    transformationSpec = transformationSpec,
                    onClick = { onPhraseClick(phrase) }
                )
            }
        }
        item {
            NavigationItem(
                title = stringResource(R.string.assistant_write),
                detail = stringResource(R.string.assistant_write_detail, ASSISTANT_MAX_TEXT),
                scope = this,
                transformationSpec = transformationSpec,
                onClick = onWriteClick
            )
        }
        item {
            ActionButton(stringResource(R.string.btn_home), this, transformationSpec, onHomeClick, secondary = true)
        }
    }
}

/** Campo de texto (máx. ASSISTANT_MAX_TEXT) con contador, Enviar y Volver. */
@Composable
fun AssistantInputScreen(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
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
            SceneHeader(stringResource(R.string.assistant_write).uppercase(), this, transformationSpec)
        }
        item {
            val interactionSource = remember { MutableInteractionSource() }
            val focused by interactionSource.collectIsFocusedAsState()
            val shape = RoundedCornerShape(TextInputTokens.radius)
            val description = stringResource(R.string.assistant_input_desc, ASSISTANT_MAX_TEXT)
            BasicTextField(
                value = text,
                onValueChange = { value ->
                    // Enter del teclado: se envía en vez de guardar un salto de línea invisible.
                    if ('\n' in value) {
                        onTextChange(value.replace('\n', ' ').trim().take(ASSISTANT_MAX_TEXT))
                        onSendClick()
                    } else {
                        onTextChange(value.take(ASSISTANT_MAX_TEXT))
                    }
                },
                textStyle = LuminaTypography.body.copy(color = TextInputTokens.content),
                cursorBrush = SolidColor(TextInputTokens.cursor),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec)
                    .heightIn(min = TextInputTokens.minHeight)
                    .clip(shape)
                    .background(TextInputTokens.container)
                    .border(
                        LuminaBorder.width,
                        if (focused) TextInputTokens.borderFocused else TextInputTokens.border,
                        shape
                    )
                    .semantics { contentDescription = description },
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.padding(TextInputTokens.padding)
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.assistant_input_hint),
                                style = LuminaTypography.body,
                                color = TextInputTokens.placeholder
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        item {
            Text(
                text = stringResource(R.string.assistant_input_counter, text.length, ASSISTANT_MAX_TEXT),
                style = LuminaTypography.label,
                color = TextInputTokens.counter,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec)
            )
        }
        item {
            ActionButton(stringResource(R.string.assistant_btn_send), this, transformationSpec, onSendClick)
        }
        item {
            ActionButton(stringResource(R.string.btn_back), this, transformationSpec, onBackClick, secondary = true)
        }
    }
}

/** Esperando al servidor (mismo estilo que Conectando). */
@Composable
fun AssistantLoadingScreen(
    onCancelClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    HubStatusScreen(
        header = stringResource(R.string.assistant_loading_header),
        title = stringResource(R.string.assistant_loading_title),
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

/** Sin entender, sin red, error del servidor o sin configuración (mismo estilo que No se pudo). */
@Composable
fun AssistantErrorScreen(
    error: AssistantError,
    reply: String,
    onRetryClick: () -> Unit,
    onPhrasesClick: () -> Unit,
    onBackClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    val header = when (error) {
        AssistantError.NOT_UNDERSTOOD -> R.string.assistant_not_understood_header
        AssistantError.NETWORK -> R.string.assistant_network_header
        AssistantError.SERVER -> R.string.assistant_server_header
        AssistantError.NOT_CONFIGURED -> R.string.assistant_config_header
    }
    val title = if (error == AssistantError.NOT_CONFIGURED) R.string.assistant_config_title else R.string.hub_error_title
    val defaultBody = when (error) {
        AssistantError.NOT_UNDERSTOOD -> R.string.assistant_not_understood_body
        AssistantError.NETWORK -> R.string.assistant_network_body
        AssistantError.SERVER -> R.string.assistant_server_body
        AssistantError.NOT_CONFIGURED -> R.string.assistant_config_body
    }
    // Si el asistente explicó por qué no entendió, se muestra su respuesta.
    val body = if (error == AssistantError.NOT_UNDERSTOOD && reply.isNotBlank()) reply else stringResource(defaultBody)
    val canRetry = error == AssistantError.NETWORK || error == AssistantError.SERVER
    HubStatusScreen(
        header = stringResource(header),
        title = stringResource(title),
        body = body,
        contentPadding = contentPadding,
        listState = listState,
        transformationSpec = transformationSpec
    ) {
        if (canRetry) {
            item { ActionButton(stringResource(R.string.btn_retry), this, transformationSpec, onRetryClick) }
        }
        item {
            ActionButton(
                stringResource(R.string.assistant_btn_phrases), this, transformationSpec, onPhrasesClick,
                secondary = canRetry
            )
        }
        if (error == AssistantError.NOT_UNDERSTOOD) {
            item { ActionButton(stringResource(R.string.btn_back), this, transformationSpec, onBackClick, secondary = true) }
        }
    }
}

/** Parte baja del área visible (fracción del alto) a partir de la cual el borde redondo recorta un botón. */
private const val APPLY_MAX_BOTTOM_FRACTION = 0.86f

/**
 * Hasta dónde puede llegar por arriba el elemento del título al salir de la vista (fracción del alto).
 * El elemento tiene espacio vacío debajo del texto: con este valor el texto ya no se ve bajo la hora,
 * pero la lista no baja de más y deja el resumen respirar bajo ella.
 */
private const val HEADER_HIDDEN_FRACTION = 0.05f

/** Medidas que se hacen como máximo al colocar Aplicar (cada una baja la lista y vuelve a medir). */
private const val APPLY_MAX_ADJUSTMENTS = 4

/** Borde inferior de un elemento, en píxeles, antes de que la transformación lo encoja junto al borde. */
private fun TransformingLazyColumnVisibleItemInfo.bottomPx(viewportHeight: Float): Float {
    val fraction = scrollProgress.bottomOffsetFraction
    return if (fraction.isNaN()) (offset + measuredHeight).toFloat() else fraction * viewportHeight
}

/**
 * Empieza arriba y, si Aplicar no queda completo dentro del área visible (muchas acciones, nombres
 * largos o una respuesta larga), baja la lista para que quepa. Al bajar, el texto del título sale de
 * la vista: no se deja a medias detrás de la hora. Si Aplicar ya cabe, no mueve nada.
 */
private suspend fun TransformingLazyColumnState.startWithApplyVisible(applyIndex: Int) {
    scrollToItem(0)
    var moved = false
    repeat(APPLY_MAX_ADJUSTMENTS) {
        // Se espera a que la lista mida con la posición actual antes de consultar dónde cae cada cosa.
        withFrameNanos { }
        withFrameNanos { }
        val viewportHeight = layoutInfo.viewportSize.height.toFloat()
        if (viewportHeight <= 0f) return
        val items = layoutInfo.visibleItems
        val applyBottom = items.firstOrNull { it.index == applyIndex }?.bottomPx(viewportHeight)
        val overflow = applyBottom?.minus(viewportHeight * APPLY_MAX_BOTTOM_FRACTION)
        // Cuánto falta para que el título (primer elemento) salga entero por arriba; 0 si ya salió.
        val headerHide = (items.firstOrNull { it.index == 0 }?.bottomPx(viewportHeight)
            ?.minus(viewportHeight * HEADER_HIDDEN_FRACTION) ?: 0f).coerceAtLeast(0f)
        val delta = when {
            overflow == null -> viewportHeight / 2f // aún no se ve (muy abajo): se baja y se vuelve a medir
            overflow > 0f -> maxOf(overflow, headerHide)
            moved && headerHide > 0f -> headerHide // ya se bajó: que el título no quede asomando
            else -> return // cabe sin mover nada
        }
        scrollBy(delta)
        moved = true
    }
}

/** Resumen de lo que se va a hacer, con Aplicar y Cancelar. */
@Composable
fun AssistantConfirmScreen(
    reply: String,
    actions: List<AssistantAction>,
    onApplyClick: () -> Unit,
    onCancelClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    // Orden de los elementos: encabezado, respuesta (si hay), una línea por acción, Aplicar, Cancelar.
    val applyIndex = 1 + (if (reply.isNotBlank()) 1 else 0) + actions.size
    LaunchedEffect(applyIndex) { listState.startWithApplyVisible(applyIndex) }
    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LuminaSpacing.xs)
    ) {
        item {
            SceneHeader(
                title = stringResource(R.string.assistant_confirm_header).uppercase(),
                scope = this,
                transformationSpec = transformationSpec,
                titleModifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                titleStyle = LuminaTypography.label
            )
        }
        if (reply.isNotBlank()) {
            item { CenteredText(reply, this, transformationSpec, LuminaTypography.body, LuminaTheme.colors.accentContent) }
        }
        actions.forEach { action ->
            item { CenteredText(assistantActionText(action), this, transformationSpec, LuminaTypography.button, LuminaTheme.colors.text) }
        }
        item { ActionButton(stringResource(R.string.assistant_btn_apply), this, transformationSpec, onApplyClick) }
        item { ActionButton(stringResource(R.string.btn_cancel), this, transformationSpec, onCancelClick, secondary = true) }
    }
}

/** Cambios aplicados, con Deshacer (snapshot de una capa) e Inicio. */
@Composable
fun AssistantDoneScreen(
    actions: List<AssistantAction>,
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
                title = stringResource(R.string.assistant_done_header).uppercase(),
                scope = this,
                transformationSpec = transformationSpec,
                titleModifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                titleStyle = LuminaTypography.label
            )
        }
        item {
            CenteredText(
                stringResource(R.string.assistant_done_title), this, transformationSpec,
                LuminaTypography.title, LuminaTheme.colors.accentContent
            )
        }
        actions.forEach { action ->
            item { CenteredText(assistantActionText(action), this, transformationSpec, LuminaTypography.body, LuminaTheme.colors.muted) }
        }
        item {
            ActionButton(
                text = stringResource(R.string.scene_btn_undo),
                scope = this,
                transformationSpec = transformationSpec,
                onClick = onUndoClick,
                secondary = true,
                description = stringResource(R.string.assistant_btn_undo_desc)
            )
        }
        item { ActionButton(stringResource(R.string.btn_home), this, transformationSpec, onHomeClick, secondary = true) }
    }
}

/** Texto legible de una acción ya validada ("Encender Lámpara (Sala) al 50 %"). */
@Composable
fun assistantActionText(action: AssistantAction): String = when (action) {
    AssistantAction.TurnOffAll -> stringResource(R.string.assistant_action_off_all)
    AssistantAction.TurnOnAll -> stringResource(R.string.assistant_action_on_all)
    is AssistantAction.ApplyScene -> {
        val scene = sceneById(action.id)
        stringResource(R.string.assistant_action_scene, if (scene != null) stringResource(scene.nameRes) else action.id)
    }
    is AssistantAction.SetLight -> {
        val light = appLights.firstOrNull { it.id == action.id }
        val name = if (light != null) stringResource(light.nameRes) else action.id
        val room = appRooms.firstOrNull { it.id == light?.roomId }?.let { stringResource(it.nameRes) }.orEmpty()
        val brightness = action.brightness
        when {
            !action.on || brightness == 0 -> stringResource(R.string.assistant_action_light_off, name, room)
            brightness == null -> stringResource(R.string.assistant_action_light_on, name, room)
            else -> stringResource(R.string.assistant_action_light_on_percent, name, room, brightness)
        }
    }
}
