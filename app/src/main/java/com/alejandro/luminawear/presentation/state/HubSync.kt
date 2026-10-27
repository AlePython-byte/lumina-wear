package com.alejandro.luminawear.presentation.state

/**
 * Ciclo de una orden al hub: IDLE → (DEBOUNCING) → (PERMISSION) → CONNECTING → IDLE o ERROR.
 * PERMISSION: esperando la respuesta del diálogo de red local; si se niega, pasa a ERROR.
 */
enum class HubStatus { IDLE, DEBOUNCING, PERMISSION, CONNECTING, ERROR }

/** Espera de inactividad antes de enviar cambios de brillo. */
const val HUB_DEBOUNCE_MS = 300L

/** Formato "1:45" (encendida, brillo) para guardar órdenes y estados de rollback en rememberSaveable. */
fun encodeLightState(state: LightState?): String =
    if (state == null) "" else "${if (state.isOn) 1 else 0}:${state.brightness}"

fun decodeLightState(saved: String): LightState? {
    val parts = saved.split(":")
    if (parts.size != 2 || (parts[0] != "0" && parts[0] != "1")) return null
    val brightness = parts[1].toIntOrNull()?.takeIf { it in 1..100 } ?: return null
    return LightState(isOn = parts[0] == "1", brightness = brightness)
}

/** Tiempo que debe tardar el hub antes de mostrar CONECTANDO (evita el parpadeo). */
const val HUB_CONNECTING_REVEAL_MS = 400L

/**
 * Acciones masivas que pueden cambiar la Lámpara. Se guardan como texto para que "Reintentar"
 * repita la acción completa ("" = la orden vino del control de la luz).
 */
const val HUB_ACTION_TURN_OFF_ALL = "turn_off_all"
const val HUB_ACTION_UNDO = "undo"
const val HUB_ACTION_SCENE_PREFIX = "scene:"
/** Acciones del asistente ya validadas, en el formato de encodeAssistantActions. */
const val HUB_ACTION_ASSISTANT_PREFIX = "assistant:"
const val HUB_ACTION_ASSISTANT_UNDO = "assistant_undo"
