package com.alejandro.luminawear.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.alejandro.luminawear.presentation.models.Scene
import com.alejandro.luminawear.presentation.models.appLights

data class LightState(
    val isOn: Boolean,
    val brightness: Int
)

private val initiallyOnLightIds = setOf(
    "living_plafon", "living_lampara", "bed_techo", "bed_mesa", "study_escritorio"
)

/** Estado inicial según Figma. Las luces apagadas recuerdan 50%. Única fuente de verdad. */
fun defaultLightStates(): Map<String, LightState> = appLights.associate { light ->
    val isOn = light.id in initiallyOnLightIds
    light.id to LightState(isOn = isOn, brightness = if (isOn) light.defaultBrightness else 50)
}

/** Formato de texto: id1:1:72,id2:0:50 (usado por el Saver y por el snapshot de Deshacer). */
fun serializeLightStates(states: Map<String, LightState>): String =
    states.entries.joinToString(",") { (id, state) ->
        val onFlag = if (state.isOn) "1" else "0"
        "$id:$onFlag:${state.brightness}"
    }

/** Parte del estado por defecto y sobrepone solo las entradas válidas. */
fun parseLightStates(saved: String): Map<String, LightState> {
    val validIds = appLights.map { it.id }.toSet()
    val result = defaultLightStates().toMutableMap()
    if (saved.isNotEmpty()) {
        saved.split(",").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size < 3) return@forEach
            val id = parts[0]
            if (id !in validIds) return@forEach
            if (parts[1] != "0" && parts[1] != "1") return@forEach
            val br = parts[2].toIntOrNull() ?: return@forEach
            if (br !in 1..100) return@forEach
            result[id] = LightState(isOn = parts[1] == "1", brightness = br)
        }
    }
    return result
}

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

    fun applyScene(scene: Scene) {
        _states.putAll(applyScene(toMap(), scene))
    }

    /** Restaura exactamente las luces del snapshot (Deshacer). */
    fun restore(snapshot: Map<String, LightState>) {
        snapshot.forEach { (id, state) -> if (_states.containsKey(id)) _states[id] = state }
    }

    fun toMap(): Map<String, LightState> = _states.toMap()

    companion object {
        fun Saver(): Saver<LightStateHolder, String> = Saver(
            save = { holder -> serializeLightStates(holder.states) },
            restore = { savedString -> LightStateHolder(parseLightStates(savedString)) }
        )
    }
}

@Composable
fun rememberLightStateHolder(): LightStateHolder {
    return rememberSaveable(saver = LightStateHolder.Saver()) {
        LightStateHolder(defaultLightStates())
    }
}
