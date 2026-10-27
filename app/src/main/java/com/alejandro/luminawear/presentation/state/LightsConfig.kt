package com.alejandro.luminawear.presentation.state

import com.alejandro.luminawear.data.HUB_LIGHT_ID
import com.alejandro.luminawear.presentation.models.Light
import com.alejandro.luminawear.presentation.models.LightType
import com.alejandro.luminawear.presentation.models.appRooms
import com.alejandro.luminawear.presentation.models.demoLights
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Luces configuradas (id, habitación, tipo y número de orden) y el siguiente número para ids nuevos.
 * `nextId` solo crece: el id de una luz quitada no se vuelve a usar.
 */
data class LightsConfig(val lights: List<Light>, val nextId: Int)

private const val NEW_LIGHT_ID_PREFIX = "light_"

/** Casa demo: las 8 luces de siempre. Es lo que se usa si no hay nada guardado. */
val DEMO_LIGHTS_CONFIG = LightsConfig(demoLights, nextId = 1)

/** true si la luz del hub todavía no existe y se puede añadir. */
val LightsConfig.hubLightAvailable: Boolean
    get() = lights.none { it.type == LightType.HUB_LAMP }

/** Tipos que se pueden añadir ahora: los añadibles, y la luz del hub solo si no existe. */
fun LightsConfig.addableTypes(): List<LightType> =
    LightType.entries.filter { it.addable && (it != LightType.HUB_LAMP || hubLightAvailable) }

/**
 * Añade una luz al final. Recibe el número más bajo libre entre las luces con el mismo nombre base
 * en esa habitación. La luz del hub siempre usa HUB_LIGHT_ID. Devuelve null si no se puede añadir.
 */
fun LightsConfig.withLight(roomId: String, type: LightType): Pair<LightsConfig, Light>? {
    if (type !in addableTypes() || appRooms.none { it.id == roomId }) return null
    val taken = lights.filter { it.roomId == roomId && it.nameRes == type.nameRes }.map { it.number }.toSet()
    val number = generateSequence(1) { it + 1 }.first { it !in taken }
    var next = nextId
    val id = if (type == LightType.HUB_LAMP) {
        HUB_LIGHT_ID
    } else {
        var candidate = NEW_LIGHT_ID_PREFIX + next++
        while (lights.any { it.id == candidate }) candidate = NEW_LIGHT_ID_PREFIX + next++
        candidate
    }
    val light = Light(id, roomId, type, number)
    return LightsConfig(lights + light, next) to light
}

fun LightsConfig.withoutLight(id: String): LightsConfig = copy(lights = lights.filterNot { it.id == id })

/** JSON: {"next_id": 3, "lights": [{"id", "room", "type", "order"}]}. */
fun encodeLightsConfig(config: LightsConfig): String {
    val lights = JSONArray()
    config.lights.forEach { light ->
        lights.put(
            JSONObject()
                .put("id", light.id)
                .put("room", light.roomId)
                .put("type", light.type.key)
                .put("order", light.number)
        )
    }
    return JSONObject().put("next_id", config.nextId).put("lights", lights).toString()
}

/**
 * Sin nada guardado o con un JSON ilegible: casa demo. Descarta entradas con habitación o tipo
 * desconocidos, ids repetidos o con separadores, y una luz del hub con otro id.
 */
fun decodeLightsConfig(saved: String?): LightsConfig {
    if (saved == null) return DEMO_LIGHTS_CONFIG
    return try {
        val root = JSONObject(saved)
        val array = root.getJSONArray("lights")
        val roomIds = appRooms.map { it.id }.toSet()
        val lights = mutableListOf<Light>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id")
            val roomId = item.optString("room")
            val type = LightType.fromKey(item.optString("type")) ?: continue
            val number = item.optInt("order", 0)
            if (id.isEmpty() || id.any { it == ',' || it == ':' || it == ';' }) continue
            if (roomId !in roomIds || number < 1 || lights.any { it.id == id }) continue
            if ((type == LightType.HUB_LAMP) != (id == HUB_LIGHT_ID)) continue
            lights += Light(id, roomId, type, number)
        }
        // Por si el contador guardado quedó atrás: nunca por debajo de un id ya usado.
        val minNext = lights.mapNotNull { light ->
            if (light.id.startsWith(NEW_LIGHT_ID_PREFIX)) light.id.removePrefix(NEW_LIGHT_ID_PREFIX).toIntOrNull() else null
        }.maxOrNull()?.plus(1) ?: 1
        LightsConfig(lights, maxOf(root.optInt("next_id", 1), minNext))
    } catch (e: JSONException) {
        DEMO_LIGHTS_CONFIG
    }
}
