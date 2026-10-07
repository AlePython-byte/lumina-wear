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
        // Range 1 to 100
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
                if (savedString.isEmpty()) {
                    LightStateHolder(emptyMap())
                } else {
                    val map = savedString.split(",").associate { entry ->
                        val parts = entry.split(":")
                        parts[0] to LightState(parts[1] == "1", parts[2].toInt())
                    }
                    LightStateHolder(map)
                }
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
