package com.alejandro.luminawear.presentation.models

import com.alejandro.luminawear.R

/** Escena de iluminación. `roomId == null` significa toda la casa. */
data class Scene(
    val id: String,
    val nameRes: Int,
    val roomId: String?,
    val percent: Int,
    val descriptionRes: Int,
    val suggestionRes: Int
)

val appScenes = listOf(
    Scene("relax", R.string.scene_relax_name, "living_room", 30, R.string.scene_relax_desc, R.string.suggestion_text_relax),
    Scene("read", R.string.scene_read_name, "bedroom", 70, R.string.scene_read_desc, R.string.suggestion_text_read),
    Scene("focus", R.string.scene_focus_name, "study", 80, R.string.scene_focus_desc, R.string.suggestion_text_focus),
    Scene("sleep", R.string.scene_sleep_name, null, 0, R.string.scene_sleep_desc, R.string.suggestion_text_sleep)
)

fun sceneById(id: String): Scene? = appScenes.firstOrNull { it.id == id }

/** Escena sugerida para una hora (0..23): 05–11 focus, 12–17 read, 18–22 relax, 23–04 sleep. */
fun suggestionForHour(hour: Int): Scene = when (hour) {
    in 5..11 -> sceneById("focus")
    in 12..17 -> sceneById("read")
    in 18..22 -> sceneById("relax")
    else -> sceneById("sleep")
}!!
