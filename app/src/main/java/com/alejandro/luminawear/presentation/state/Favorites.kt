package com.alejandro.luminawear.presentation.state

import com.alejandro.luminawear.presentation.models.Light
import com.alejandro.luminawear.presentation.models.Scene
import com.alejandro.luminawear.presentation.models.appLights
import com.alejandro.luminawear.presentation.models.sceneById

/** Favoritos por defecto: plafón de Sala y escena Descanso. */
const val DEFAULT_FAVORITES = "living_plafon,relax"

/** Id del favorito que reaparece con "Añadir plafón". */
const val FAVORITE_PLAFON_ID = "living_plafon"

/** Un favorito resuelto: una luz o una escena. */
sealed interface Favorite {
    val id: String

    data class LightFavorite(val light: Light) : Favorite {
        override val id: String get() = light.id
    }

    data class SceneFavorite(val scene: Scene) : Favorite {
        override val id: String get() = scene.id
    }
}

fun favoriteById(id: String): Favorite? {
    appLights.firstOrNull { it.id == id }?.let { return Favorite.LightFavorite(it) }
    sceneById(id)?.let { return Favorite.SceneFavorite(it) }
    return null
}

/** Lista de ids separada por comas; descarta ids desconocidos y repetidos. */
fun parseFavoriteIds(saved: String): List<String> =
    saved.split(",").filter { it.isNotEmpty() && favoriteById(it) != null }.distinct()

fun serializeFavoriteIds(ids: List<String>): String = ids.joinToString(",")

fun resolveFavorites(saved: String): List<Favorite> = parseFavoriteIds(saved).mapNotNull(::favoriteById)
