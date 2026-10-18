package com.alejandro.luminawear.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.alejandro.luminawear.presentation.models.appLights

data class LightState(
    val isOn: Boolean,
    val brightness: Int
)

@Stable
class LightStateHolder(
    initialStates: Map<String, LightState>
) {
    private val _states = mutableStateMapOf<String, LightState>().apply {
        putAll(initialStates)
    }

    val states: Map<String, LightState> get() = _states

    fun setLightOn(id: String, isOn: Boolean) {
        val current = _states[id] ?: return
        _states[id] = current.copy(isOn = isOn)
    }

    fun setBrightness(id: String, brightness: Int) {
        val current = _states[id] ?: return
        if (!current.isOn) return
        val clamped = brightness.coerceIn(1, 100)
        _states[id] = current.copy(brightness = clamped)
    }

    fun turnOffAll() {
        _states.keys.forEach { id ->
            val current = _states[id] ?: return@forEach
            _states[id] = current.copy(isOn = false)
        }
    }

    fun toMap(): Map<String, LightState> = _states.toMap()

    companion object {
        fun Saver(): Saver<LightStateHolder, String> = Saver(
            save = { holder ->
                // Serialize format: id1:1:72,id2:0:50
                holder.states.entries.joinToString(",") { (id, state) ->
                    val onFlag = if (state.isOn) "1" else "0"
                    "$id:$onFlag:${state.brightness}"
                }
            },
            restore = { savedString ->
                val validIds = appLights.map { it.id }.toSet()
                val initialOnIds = setOf("living_plafon", "living_lampara", "bed_techo", "bed_mesa", "study_escritorio")
                val base = appLights.associate { light ->
                    val isOn = light.id in initialOnIds
                    light.id to LightState(isOn = isOn, brightness = if (isOn) light.defaultBrightness else 50)
                }.toMutableMap()
                if (savedString.isNotEmpty()) {
                    savedString.split(",").forEach { entry ->
                        val parts = entry.split(":")
                        if (parts.size < 3) return@forEach
                        val id = parts[0]
                        if (id !in validIds) return@forEach
                        if (parts[1] != "0" && parts[1] != "1") return@forEach
                        val br = parts[2].toIntOrNull() ?: return@forEach
                        if (br !in 1..100) return@forEach
                        base[id] = LightState(isOn = parts[1] == "1", brightness = br)
                    }
                }
                LightStateHolder(base)
            }
        )
    }
}

@Composable
fun rememberLightStateHolder(): LightStateHolder {
    // Initial state based on Figma
    val initialOn = setOf("living_plafon", "living_lampara", "bed_techo", "bed_mesa", "study_escritorio")
    val defaultStateMap = appLights.associate { light ->
        val isOn = initialOn.contains(light.id)
        val defaultBr = if (isOn) light.defaultBrightness else 50 // 50% for off lights
        light.id to LightState(isOn = isOn, brightness = defaultBr)
    }

    return rememberSaveable(saver = LightStateHolder.Saver()) {
        LightStateHolder(defaultStateMap)
    }
}
