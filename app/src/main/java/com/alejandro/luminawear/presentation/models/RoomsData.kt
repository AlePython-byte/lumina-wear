package com.alejandro.luminawear.presentation.models

import com.alejandro.luminawear.R

data class Room(val id: String, val nameRes: Int)
data class Light(val id: String, val roomId: String, val nameRes: Int, val defaultBrightness: Int)

val appRooms = listOf(
    Room("living_room", R.string.room_living),
    Room("bedroom", R.string.room_bedroom),
    Room("study", R.string.room_study)
)

val appLights = listOf(
    Light("living_plafon", "living_room", R.string.light_plafon, 72),
    Light("living_lampara", "living_room", R.string.light_lampara, 45),
    Light("living_tira", "living_room", R.string.light_tira, 0),
    Light("bed_techo", "bedroom", R.string.light_techo, 60),
    Light("bed_mesa", "bedroom", R.string.light_mesa, 30),
    Light("bed_armario", "bedroom", R.string.light_armario, 0),
    Light("study_escritorio", "study", R.string.light_escritorio, 80),
    Light("study_ambiente", "study", R.string.light_ambiente, 0)
)
