package com.alejandro.luminawear.presentation.state

import com.alejandro.luminawear.presentation.models.Light
import com.alejandro.luminawear.presentation.models.Scene
import com.alejandro.luminawear.presentation.models.appLights

/**
 * Aplica una escena sobre un mapa de estados (función pura).
 * Para cada luz de la habitación objetivo (o de toda la casa): 0% → apagada conservando su
 * brillo recordado; si no, encendida con el porcentaje. Las demás luces no cambian.
 */
fun applyScene(
    states: Map<String, LightState>,
    scene: Scene,
    lights: List<Light> = appLights
): Map<String, LightState> {
    val result = states.toMutableMap()
    lights.filter { scene.roomId == null || it.roomId == scene.roomId }.forEach { light ->
        val current = result[light.id] ?: return@forEach
        result[light.id] = if (scene.percent == 0) {
            current.copy(isOn = false)
        } else {
            LightState(isOn = true, brightness = scene.percent)
        }
    }
    return result
}
