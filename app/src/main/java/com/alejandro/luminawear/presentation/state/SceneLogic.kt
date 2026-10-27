package com.alejandro.luminawear.presentation.state

import com.alejandro.luminawear.presentation.models.Light
import com.alejandro.luminawear.presentation.models.Scene

/** Luces existentes sobre las que actúa la escena: las de su habitación o, sin habitación, todas. */
fun sceneLights(scene: Scene, lights: List<Light>): List<Light> =
    lights.filter { scene.roomId == null || it.roomId == scene.roomId }

/**
 * Aplica una escena sobre un mapa de estados (función pura).
 * Para cada luz existente de la habitación objetivo (o de toda la casa): 0% → apagada conservando su
 * brillo recordado; si no, encendida con el porcentaje. Las demás luces no cambian.
 */
fun applyScene(
    states: Map<String, LightState>,
    scene: Scene,
    lights: List<Light>
): Map<String, LightState> {
    val result = states.toMutableMap()
    sceneLights(scene, lights).forEach { light ->
        val current = result[light.id] ?: return@forEach
        result[light.id] = if (scene.percent == 0) {
            current.copy(isOn = false)
        } else {
            LightState(isOn = true, brightness = scene.percent)
        }
    }
    return result
}
