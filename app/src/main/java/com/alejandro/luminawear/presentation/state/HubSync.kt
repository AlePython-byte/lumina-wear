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
