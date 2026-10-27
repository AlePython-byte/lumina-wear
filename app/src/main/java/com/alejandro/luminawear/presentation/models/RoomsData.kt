package com.alejandro.luminawear.presentation.models

import com.alejandro.luminawear.R

data class Room(val id: String, val nameRes: Int)

/**
 * Tipo de luz. `key` se guarda en la configuración; `nameRes` es el nombre base visible. Las luces
 * con el mismo nombre base en una habitación comparten numeración ("Lámpara", "Lámpara 2").
 * Solo los tipos `addable` se pueden añadir; los demás existen únicamente en la casa demo.
 */
enum class LightType(val key: String, val nameRes: Int, val addable: Boolean) {
    PLAFON("plafon", R.string.light_plafon, true),
    LAMPARA("lampara", R.string.light_lampara, true),
    TIRA("tira", R.string.light_tira, true),
    /** Luz real conectada al hub. Puede existir una sola vez. */
    HUB_LAMP("hub_lamp", R.string.light_lampara, true),
    TECHO("techo", R.string.light_techo, false),
    MESA("mesa", R.string.light_mesa, false),
    ARMARIO("armario", R.string.light_armario, false),
    ESCRITORIO("escritorio", R.string.light_escritorio, false),
    AMBIENTE("ambiente", R.string.light_ambiente, false);

    companion object {
        fun fromKey(key: String): LightType? = entries.firstOrNull { it.key == key }
    }
}

/** Luz tal como la usan las pantallas. `number > 1` añade el número al nombre ("Plafón 2"). */
data class Light(val id: String, val roomId: String, val type: LightType, val number: Int) {
    val nameRes: Int get() = type.nameRes
}

val appRooms = listOf(
    Room("living_room", R.string.room_living),
    Room("bedroom", R.string.room_bedroom),
    Room("study", R.string.room_study)
)

/** Las 8 luces de la casa demo, con los ids de siempre. */
val demoLights = listOf(
    Light("living_plafon", "living_room", LightType.PLAFON, 1),
    Light("living_lampara", "living_room", LightType.HUB_LAMP, 1),
    Light("living_tira", "living_room", LightType.TIRA, 1),
    Light("bed_techo", "bedroom", LightType.TECHO, 1),
    Light("bed_mesa", "bedroom", LightType.MESA, 1),
    Light("bed_armario", "bedroom", LightType.ARMARIO, 1),
    Light("study_escritorio", "study", LightType.ESCRITORIO, 1),
    Light("study_ambiente", "study", LightType.AMBIENTE, 1)
)
