package com.alejandro.luminawear.presentation.state

import com.alejandro.luminawear.presentation.models.Light
import com.alejandro.luminawear.presentation.models.Scene
import com.alejandro.luminawear.presentation.models.sceneById

/** Favoritos por defecto: plafón de Sala y escena Descanso. */
const val DEFAULT_FAVORITES = "living_plafon,relax"

/** Id del favorito que reaparece con "Añadir plafón" (solo si esa luz existe). */
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

/** Resuelve contra las luces que existen: el id de una luz quitada no da favorito. */
fun favoriteById(id: String, lights: List<Light>): Favorite? {
    lights.firstOrNull { it.id == id }?.let { return Favorite.LightFavorite(it) }
    sceneById(id)?.let { return Favorite.SceneFavorite(it) }
    return null
}

/** Lista de ids separada por comas; descarta ids desconocidos (o de luces que ya no existen) y repetidos. */
fun parseFavoriteIds(saved: String, lights: List<Light>): List<String> =
    saved.split(",").filter { it.isNotEmpty() && favoriteById(it, lights) != null }.distinct()

fun serializeFavoriteIds(ids: List<String>): String = ids.joinToString(",")

fun resolveFavorites(saved: String, lights: List<Light>): List<Favorite> =
    parseFavoriteIds(saved, lights).mapNotNull { favoriteById(it, lights) }
