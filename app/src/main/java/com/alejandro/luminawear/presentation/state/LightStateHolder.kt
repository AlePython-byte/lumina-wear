package com.alejandro.luminawear.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.alejandro.luminawear.presentation.models.Light
import com.alejandro.luminawear.presentation.models.LightType
import com.alejandro.luminawear.presentation.models.Scene

data class LightState(
    val isOn: Boolean,
    val brightness: Int
)

/** Brillo que recuerda una luz apagada sin otro valor (luces nuevas y apagadas de la demo). */
const val DEFAULT_REMEMBERED_BRIGHTNESS = 50

/** Estado inicial de la casa demo según Figma: las encendidas con su brillo. */
private val demoInitiallyOn = mapOf(
    "living_plafon" to 72, "living_lampara" to 45, "bed_techo" to 60, "bed_mesa" to 30, "study_escritorio" to 80
)

/** Estado inicial de una luz: el de la demo si es una de sus encendidas; si no, apagada con 50 %. */
fun initialLightState(id: String): LightState {
    val brightness = demoInitiallyOn[id]
    return if (brightness != null) LightState(isOn = true, brightness = brightness)
    else LightState(isOn = false, brightness = DEFAULT_REMEMBERED_BRIGHTNESS)
}

/** Estado inicial de cada luz de la lista (Restaurar demo, cargar la casa demo). */
fun defaultLightStates(lights: List<Light>): Map<String, LightState> =
    lights.associate { it.id to initialLightState(it.id) }

/** Formato de texto: id1:1:72,id2:0:50 (usado por el Saver y por el snapshot de Deshacer). */
fun serializeLightStates(states: Map<String, LightState>): String =
    states.entries.joinToString(",") { (id, state) ->
        val onFlag = if (state.isOn) "1" else "0"
        "$id:$onFlag:${state.brightness}"
    }

/**
 * Solo las entradas válidas del texto, sin rellenar las que faltan: al restaurar un snapshot, una luz
 * añadida después no se toca y una que ya no existe se ignora.
 */
fun parseLightStates(saved: String): Map<String, LightState> {
    val result = mutableMapOf<String, LightState>()
    if (saved.isNotEmpty()) {
        saved.split(",").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size < 3 || parts[0].isEmpty()) return@forEach
            if (parts[1] != "0" && parts[1] != "1") return@forEach
            val br = parts[2].toIntOrNull() ?: return@forEach
            if (br !in 1..100) return@forEach
            result[parts[0]] = LightState(isOn = parts[1] == "1", brightness = br)
        }
    }
    return result
}

/**
 * Única fuente de verdad: qué luces existen (configuración persistente) y el estado de cada una
 * (no se persiste entre aperturas). Cada cambio de la lista se entrega a `onConfigChange` para guardarlo.
 */
@Stable
class LightStateHolder(
    initialConfig: LightsConfig,
    initialStates: Map<String, LightState>,
    private val onConfigChange: (LightsConfig) -> Unit = {}
) {
    var config: LightsConfig by mutableStateOf(initialConfig)
        private set

    private val _states = mutableStateMapOf<String, LightState>().apply {
        initialConfig.lights.forEach { put(it.id, initialStates[it.id] ?: initialLightState(it.id)) }
    }

    val lights: List<Light> get() = config.lights

    val states: Map<String, LightState> get() = _states

    /** false si la luz del hub ya existe: esa opción no se puede volver a añadir. */
    val hubLightAvailable: Boolean get() = config.hubLightAvailable

    fun addableTypes(): List<LightType> = config.addableTypes()

    fun lightById(id: String): Light? = config.lights.firstOrNull { it.id == id }

    fun lightsInRoom(roomId: String): List<Light> = config.lights.filter { it.roomId == roomId }

    /** true si la escena no tiene ninguna luz sobre la que actuar (su habitación, o la casa, está vacía). */
    fun isSceneWithoutLights(scene: Scene): Boolean = sceneLights(scene, config.lights).isEmpty()

    /** Añade una luz apagada con 50 % recordado; el resto conserva su estado. Devuelve la luz o null. */
    fun addLight(roomId: String, type: LightType): Light? {
        val (next, light) = config.withLight(roomId, type) ?: return null
        _states[light.id] = LightState(isOn = false, brightness = DEFAULT_REMEMBERED_BRIGHTNESS)
        updateConfig(next)
        return light
    }

    /** Quita una luz; el resto conserva su estado. Los favoritos se limpian en quien los guarda. */
    fun removeLight(id: String) {
        if (lightById(id) == null) return
        _states.remove(id)
        updateConfig(config.withoutLight(id))
    }

    /** Carga la casa demo con su estado inicial. El contador de ids se conserva. */
    fun loadDemo() {
        _states.clear()
        _states.putAll(defaultLightStates(DEMO_LIGHTS_CONFIG.lights))
        updateConfig(DEMO_LIGHTS_CONFIG.copy(nextId = config.nextId))
    }

    /** Cero luces. El contador de ids se conserva para no reutilizarlos. */
    fun clear() {
        _states.clear()
        updateConfig(config.copy(lights = emptyList()))
    }

    private fun updateConfig(next: LightsConfig) {
        config = next
        onConfigChange(next)
    }

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
        _states.putAll(applyScene(toMap(), scene, config.lights))
    }

    /** Restaura las luces del snapshot que todavía existen (Deshacer); las demás no cambian. */
    fun restore(snapshot: Map<String, LightState>) {
        snapshot.forEach { (id, state) -> if (_states.containsKey(id)) _states[id] = state }
    }

    fun toMap(): Map<String, LightState> = _states.toMap()

    companion object {
        /** Solo guarda el estado: la lista de luces se vuelve a leer de las preferencias. */
        fun Saver(config: LightsConfig, onConfigChange: (LightsConfig) -> Unit): Saver<LightStateHolder, String> = Saver(
            save = { holder -> serializeLightStates(holder.states) },
            restore = { savedString -> LightStateHolder(config, parseLightStates(savedString), onConfigChange) }
        )
    }
}

@Composable
fun rememberLightStateHolder(
    initialConfig: LightsConfig,
    onConfigChange: (LightsConfig) -> Unit
): LightStateHolder {
    return rememberSaveable(saver = LightStateHolder.Saver(initialConfig, onConfigChange)) {
        LightStateHolder(initialConfig, emptyMap(), onConfigChange)
    }
}
