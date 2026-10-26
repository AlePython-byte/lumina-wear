package com.alejandro.luminawear.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.lazy.TransformationSpec
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.components.ActionButton
import com.alejandro.luminawear.presentation.components.NavigationItem
import com.alejandro.luminawear.presentation.models.appRooms
import com.alejandro.luminawear.presentation.scenes.CenteredText
import com.alejandro.luminawear.presentation.scenes.SceneHeader
import com.alejandro.luminawear.presentation.scenes.sceneRoomName
import com.alejandro.luminawear.presentation.state.Favorite
import com.alejandro.luminawear.presentation.theme.LuminaSpacing
import com.alejandro.luminawear.presentation.theme.LuminaTheme
import com.alejandro.luminawear.presentation.theme.LuminaTypography

@Composable
private fun favoriteName(favorite: Favorite): String = when (favorite) {
    is Favorite.LightFavorite -> stringResource(favorite.light.nameRes)
    is Favorite.SceneFavorite -> stringResource(favorite.scene.nameRes)
}

@Composable
private fun favoriteDetail(favorite: Favorite): String = when (favorite) {
    is Favorite.LightFavorite -> {
        val room = appRooms.firstOrNull { it.id == favorite.light.roomId }
        stringResource(R.string.fav_detail_light, stringResource(room?.nameRes ?: R.string.scene_room_all))
    }
    is Favorite.SceneFavorite -> stringResource(R.string.fav_detail_scene, sceneRoomName(favorite.scene))
}

@Composable
private fun favoriteRemoveLabel(favorite: Favorite): String = when (favorite) {
    is Favorite.LightFavorite ->
        stringResource(R.string.fav_remove_light, stringResource(favorite.light.nameRes).lowercase())
    is Favorite.SceneFavorite -> stringResource(R.string.fav_remove_scene)
}

/** Con el nombre de la escena: "Quitar escena" a secas no distingue una de otra. Las luces ya llevan su nombre. */
@Composable
private fun favoriteRemoveDescription(favorite: Favorite): String? = when (favorite) {
    is Favorite.LightFavorite -> null
    is Favorite.SceneFavorite -> stringResource(R.string.fav_remove_scene_desc, stringResource(favorite.scene.nameRes))
}

/**
 * Lista de favoritos. Con 2 o más: ítems + Editar + Inicio. Con uno: ítem + Quitar + Restaurar demo + Inicio.
 * Sin favoritos: estado vacío con "Añadir plafón" e "Inicio".
 */
@Composable
fun FavoritesScreen(
    favorites: List<Favorite>,
    onFavoriteClick: (Favorite) -> Unit,
    onEditClick: () -> Unit,
    onRemoveClick: (Favorite) -> Unit,
    onRestoreDemoClick: () -> Unit,
    onAddPlafonClick: () -> Unit,
    onHomeClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LuminaSpacing.xs)
    ) {
        if (favorites.isEmpty()) {
            item {
                SceneHeader(stringResource(R.string.access_favorites).uppercase(), this, transformationSpec)
            }
            item {
                CenteredText(
                    stringResource(R.string.fav_empty_title), this, transformationSpec,
                    LuminaTypography.title, LuminaTheme.colors.accentContent
                )
            }
            item {
                CenteredText(
                    stringResource(R.string.fav_empty_body), this, transformationSpec,
                    LuminaTypography.body, LuminaTheme.colors.muted
                )
            }
            item {
                ActionButton(stringResource(R.string.fav_add_plafon), this, transformationSpec, onAddPlafonClick)
            }
            item {
                ActionButton(stringResource(R.string.btn_home), this, transformationSpec, onHomeClick, secondary = true)
            }
        } else {
            item {
                SceneHeader(stringResource(R.string.access_favorites).uppercase(), this, transformationSpec)
            }
            favorites.forEach { favorite ->
                item(key = favorite.id) {
                    NavigationItem(
                        title = favoriteName(favorite),
                        detail = favoriteDetail(favorite),
                        scope = this,
                        transformationSpec = transformationSpec,
                        onClick = { onFavoriteClick(favorite) }
                    )
                }
            }
            if (favorites.size == 1) {
                val only = favorites.first()
                item {
                    ActionButton(
                        favoriteRemoveLabel(only), this, transformationSpec, { onRemoveClick(only) },
                        secondary = true, description = favoriteRemoveDescription(only)
                    )
                }
                item {
                    ActionButton(stringResource(R.string.btn_restore_demo), this, transformationSpec, onRestoreDemoClick, secondary = true)
                }
            } else {
                item {
                    ActionButton(stringResource(R.string.btn_edit), this, transformationSpec, onEditClick, secondary = true)
                }
            }
            item {
                ActionButton(stringResource(R.string.btn_home), this, transformationSpec, onHomeClick, secondary = true)
            }
        }
    }
}

@Composable
fun FavoritesEditScreen(
    favorites: List<Favorite>,
    onRemoveClick: (Favorite) -> Unit,
    onBackClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LuminaSpacing.xs)
    ) {
        item {
            SceneHeader(stringResource(R.string.fav_edit_title).uppercase(), this, transformationSpec)
        }
        item {
            CenteredText(
                stringResource(R.string.fav_saved_title), this, transformationSpec,
                LuminaTypography.body, LuminaTheme.colors.muted
            )
        }
        favorites.forEach { favorite ->
            item(key = favorite.id) {
                ActionButton(
                    favoriteRemoveLabel(favorite), this, transformationSpec, { onRemoveClick(favorite) },
                    secondary = true, description = favoriteRemoveDescription(favorite)
                )
            }
        }
        item {
            ActionButton(stringResource(R.string.btn_back), this, transformationSpec, onBackClick, secondary = true)
        }
    }
}
