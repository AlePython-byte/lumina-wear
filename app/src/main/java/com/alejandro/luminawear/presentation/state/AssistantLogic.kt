package com.alejandro.luminawear.presentation.state

import com.alejandro.luminawear.presentation.models.appScenes

/** Acción del asistente. Las reglas locales y el servidor producen la misma forma. */
sealed interface AssistantAction {
    /** `brightness == null`: conserva el brillo actual de la luz. */
    data class SetLight(val id: String, val on: Boolean, val brightness: Int?) : AssistantAction
    data class ApplyScene(val id: String) : AssistantAction
    data object TurnOffAll : AssistantAction
    /** Solo local: el contrato del servidor no la incluye. Enciende todo conservando cada brillo. */
    data object TurnOnAll : AssistantAction
}

/** Luz tal como se describe al servidor (nombre y habitación en el idioma de la app). */
data class AssistantLight(val id: String, val name: String, val room: String, val on: Boolean, val brightness: Int)

/** Por qué el asistente no propone acciones. */
enum class AssistantError { NOT_UNDERSTOOD, NETWORK, SERVER, NOT_CONFIGURED }

const val ASSISTANT_MAX_ACTIONS = 5
const val ASSISTANT_MAX_TEXT = 200

/**
 * Valida acciones vengan de donde vengan (reglas locales o servidor): ids que existen en el
 * estado actual, brillo recortado a 0–100, solo las 4 escenas, como máximo 5. Lo inválido se descarta.
 */
fun validateAssistantActions(
    actions: List<AssistantAction>,
    states: Map<String, LightState>
): List<AssistantAction> {
    val sceneIds = appScenes.map { it.id }.toSet()
    return actions.mapNotNull { action ->
        when (action) {
            is AssistantAction.SetLight ->
                if (action.id in states) action.copy(brightness = action.brightness?.coerceIn(0, 100)) else null
            is AssistantAction.ApplyScene -> action.takeIf { it.id in sceneIds }
            AssistantAction.TurnOffAll, AssistantAction.TurnOnAll -> action
        }
    }.take(ASSISTANT_MAX_ACTIONS)
}

/**
 * Aplica acciones ya validadas sobre un mapa de estados (función pura). Una luz que se enciende
 * con 0 % queda apagada; apagar conserva el brillo recordado.
 */
fun applyAssistantActions(
    states: Map<String, LightState>,
    actions: List<AssistantAction>
): Map<String, LightState> {
    var result = states
    actions.forEach { action ->
        result = when (action) {
            is AssistantAction.SetLight -> {
                val current = result[action.id] ?: return@forEach
                val brightness = action.brightness
                val next = when {
                    !action.on || brightness == 0 -> current.copy(isOn = false)
                    brightness == null -> current.copy(isOn = true)
                    else -> LightState(isOn = true, brightness = brightness.coerceIn(1, 100))
                }
                result + (action.id to next)
            }
            is AssistantAction.ApplyScene -> {
                val scene = appScenes.firstOrNull { it.id == action.id } ?: return@forEach
                applyScene(result, scene)
            }
            AssistantAction.TurnOffAll -> result.mapValues { it.value.copy(isOn = false) }
            AssistantAction.TurnOnAll -> result.mapValues { it.value.copy(isOn = true) }
        }
    }
    return result
}

/** Formato de texto para rememberSaveable y "Reintentar": off_all;on_all;scene:read;light:id:1:50 ("-" = sin brillo). */
fun encodeAssistantActions(actions: List<AssistantAction>): String =
    actions.joinToString(";") { action ->
        when (action) {
            is AssistantAction.SetLight ->
                "light:${action.id}:${if (action.on) 1 else 0}:${action.brightness ?: "-"}"
            is AssistantAction.ApplyScene -> "scene:${action.id}"
            AssistantAction.TurnOffAll -> "off_all"
            AssistantAction.TurnOnAll -> "on_all"
        }
    }

fun decodeAssistantActions(saved: String): List<AssistantAction> =
    saved.split(";").mapNotNull { entry ->
        val parts = entry.split(":")
        when {
            entry == "off_all" -> AssistantAction.TurnOffAll
            entry == "on_all" -> AssistantAction.TurnOnAll
            parts.size == 2 && parts[0] == "scene" -> AssistantAction.ApplyScene(parts[1])
            parts.size == 4 && parts[0] == "light" && (parts[2] == "0" || parts[2] == "1") ->
                AssistantAction.SetLight(parts[1], parts[2] == "1", parts[3].toIntOrNull())
            else -> null
        }
    }
