package com.alejandro.luminawear.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.alejandro.luminawear.R
import com.alejandro.luminawear.data.DEFAULT_HUB_URL
import com.alejandro.luminawear.data.HUB_LIGHT_ID
import com.alejandro.luminawear.data.LOCAL_NETWORK_PERMISSION
import com.alejandro.luminawear.data.LightProviders
import com.alejandro.luminawear.data.needsLocalNetworkPermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import com.alejandro.luminawear.presentation.favorites.FavoritesEditScreen
import com.alejandro.luminawear.presentation.favorites.FavoritesScreen
import com.alejandro.luminawear.presentation.home.HomeScreen
import com.alejandro.luminawear.presentation.lights.HubConnectingScreen
import com.alejandro.luminawear.presentation.lights.HubErrorScreen
import com.alejandro.luminawear.presentation.lights.LightControlScreen
import com.alejandro.luminawear.presentation.models.appLights
import com.alejandro.luminawear.presentation.models.Scene
import com.alejandro.luminawear.presentation.models.appRooms
import com.alejandro.luminawear.presentation.models.sceneById
import com.alejandro.luminawear.presentation.models.suggestionForHour
import com.alejandro.luminawear.presentation.rooms.RoomDetailScreen
import com.alejandro.luminawear.presentation.rooms.RoomsScreen
import com.alejandro.luminawear.presentation.scenes.SceneActiveScreen
import com.alejandro.luminawear.presentation.scenes.SceneDetailScreen
import com.alejandro.luminawear.presentation.scenes.ScenesScreen
import com.alejandro.luminawear.presentation.scenes.SuggestionScreen
import com.alejandro.luminawear.presentation.settings.SettingsScreen
import com.alejandro.luminawear.presentation.state.parseLightStates
import com.alejandro.luminawear.presentation.state.serializeLightStates
import com.alejandro.luminawear.presentation.state.AppLanguage
import com.alejandro.luminawear.presentation.state.DEFAULT_FAVORITES
import com.alejandro.luminawear.presentation.state.FAVORITE_PLAFON_ID
import com.alejandro.luminawear.presentation.state.HUB_ACTION_RESTORE_DEMO
import com.alejandro.luminawear.presentation.state.HUB_ACTION_SCENE_PREFIX
import com.alejandro.luminawear.presentation.state.HUB_ACTION_TURN_OFF_ALL
import com.alejandro.luminawear.presentation.state.HUB_ACTION_UNDO
import com.alejandro.luminawear.presentation.state.HUB_CONNECTING_REVEAL_MS
import com.alejandro.luminawear.presentation.state.HUB_DEBOUNCE_MS
import com.alejandro.luminawear.presentation.state.HubStatus
import com.alejandro.luminawear.presentation.state.decodeLightState
import com.alejandro.luminawear.presentation.state.encodeLightState
import com.alejandro.luminawear.presentation.state.Favorite
import com.alejandro.luminawear.presentation.state.defaultLightStates
import com.alejandro.luminawear.presentation.state.parseFavoriteIds
import com.alejandro.luminawear.presentation.state.resolveFavorites
import com.alejandro.luminawear.presentation.state.serializeFavoriteIds
import com.alejandro.luminawear.presentation.state.rememberLightStateHolder
import com.alejandro.luminawear.presentation.state.LightState
import com.alejandro.luminawear.presentation.state.LightStateHolder
import com.alejandro.luminawear.presentation.theme.LuminaTimeText
import com.alejandro.luminawear.presentation.theme.LuminaWearTheme
import com.alejandro.luminawear.presentation.theme.ThemeMode

private const val HUB_SYNC_TAG = "HubSync"

enum class ScreenType { HOME, ROOMS, ROOM_DETAIL, LIGHT_CONTROL, SETTINGS, SCENES, SCENE_DETAIL, SCENE_ACTIVE, SUGGESTION, FAVORITES, FAVORITES_EDIT }

@Composable
fun WearApp(
    initialThemeMode: ThemeMode = ThemeMode.LIGHT,
    language: AppLanguage = AppLanguage.ES,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit = {},
    initialFavorites: String = DEFAULT_FAVORITES,
    onFavoritesChange: (String) -> Unit = {},
    hubUrl: String = DEFAULT_HUB_URL
) {
    val lightStateHolder = rememberLightStateHolder()

    val totalLights = appLights.size
    val totalLightsOn = lightStateHolder.states.count { it.value.isOn }

    var currentScreen by rememberSaveable { mutableStateOf(ScreenType.HOME.name) }
    var selectedRoomId by rememberSaveable { mutableStateOf("") }
    var selectedLightId by rememberSaveable { mutableStateOf("") }
    // Único dueño del tema; MainActivity entrega el valor guardado y persiste los cambios.
    var themeMode by rememberSaveable { mutableStateOf(initialThemeMode) }
    var selectedSceneId by rememberSaveable { mutableStateOf("") }
    // Snapshot de las luces justo antes de activar una escena ("" = sin Deshacer). Una sola capa.
    var undoSnapshot by rememberSaveable { mutableStateOf("") }
    var suggestionDismissed by rememberSaveable { mutableStateOf(false) }
    val suggestion = remember { suggestionForHour(LocalTime.now().hour) }
    // Ids de favoritos separados por comas; MainActivity entrega el valor guardado y persiste los cambios.
    var favoritesRaw by rememberSaveable { mutableStateOf(serializeFavoriteIds(parseFavoriteIds(initialFavorites))) }
    // true cuando el control de luz o el detalle de escena se abrieron desde Favoritos (atrás vuelve allí).
    var openedFromFavorites by rememberSaveable { mutableStateOf(false) }
    val favorites = resolveFavorites(favoritesRaw)

    // Hub: solo la Lámpara de Sala pide confirmación remota; las demás luces cambian directo.
    val providers = remember(hubUrl) { LightProviders(hubUrl) }
    var hubStatus by rememberSaveable { mutableStateOf(HubStatus.IDLE) }
    // Orden pendiente y último estado confirmado de la Lámpara, en formato "1:45".
    var hubTarget by rememberSaveable { mutableStateOf("") }
    var hubRollback by rememberSaveable { mutableStateOf("") }
    // Acción masiva en curso (HUB_ACTION_*; "" = control de la luz) y lo necesario para deshacerla:
    // snapshot de todas las luces (mismo formato que Deshacer), Deshacer previo y pantalla de origen.
    var hubAction by rememberSaveable { mutableStateOf("") }
    var hubActionSnapshot by rememberSaveable { mutableStateOf("") }
    var hubActionUndo by rememberSaveable { mutableStateOf("") }
    var hubActionOrigin by rememberSaveable { mutableStateOf("") }
    // CONECTANDO solo se muestra si el hub tarda más de HUB_CONNECTING_REVEAL_MS.
    var hubConnectingVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun updateFavorites(ids: List<String>) {
        favoritesRaw = serializeFavoriteIds(ids)
        onFavoritesChange(favoritesRaw)
    }

    fun removeFavorite(favorite: Favorite) {
        val remaining = parseFavoriteIds(favoritesRaw) - favorite.id
        updateFavorites(remaining)
        if (remaining.isEmpty()) currentScreen = ScreenType.FAVORITES.name
    }

    fun rollbackHubLight() {
        decodeLightState(hubRollback)?.let { lightStateHolder.restore(mapOf(HUB_LIGHT_ID to it)) }
    }

    /** Orden no confirmada: deshace la acción masiva (si la hubo) y deja la Lámpara en su último estado confirmado. */
    fun rollbackHubOrder() {
        if (hubAction.isNotEmpty()) {
            lightStateHolder.restore(parseLightStates(hubActionSnapshot))
            undoSnapshot = hubActionUndo
            currentScreen = hubActionOrigin
        }
        rollbackHubLight()
    }

    // Permiso de red local (Android 17): se pide antes de la primera orden; si se niega, error y
    // se vuelve a pedir en la siguiente orden o en "Reintentar".
    val localNetworkPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.i("HubPermission", "ACCESS_LOCAL_NETWORK concedido=$granted")
        if (hubStatus != HubStatus.PERMISSION) return@rememberLauncherForActivityResult
        if (granted) {
            hubStatus = HubStatus.CONNECTING
        } else {
            rollbackHubOrder()
            hubStatus = HubStatus.ERROR
        }
    }

    /** Envía hubTarget, pidiendo antes el permiso de red local si falta. */
    fun startHubRequest() {
        if (needsLocalNetworkPermission(context)) {
            hubStatus = HubStatus.PERMISSION
            localNetworkPermissionLauncher.launch(LOCAL_NETWORK_PERMISSION)
        } else {
            hubStatus = HubStatus.CONNECTING
        }
    }

    /** Aplica el cambio en LightStateHolder y, si la luz es remota, lo encola para el hub. */
    fun changeLight(lightId: String, immediate: Boolean, change: () -> Unit) {
        if (!providers.isRemote(lightId)) {
            change()
            return
        }
        val before = lightStateHolder.states[lightId]
        // El rollback se toma al empezar una ráfaga: es el último estado confirmado.
        if (hubStatus == HubStatus.IDLE) hubRollback = encodeLightState(before)
        change()
        val after = lightStateHolder.states[lightId]
        if (hubStatus == HubStatus.IDLE && before == after) return
        hubAction = ""
        hubTarget = encodeLightState(after)
        if (immediate) startHubRequest() else hubStatus = HubStatus.DEBOUNCING
    }

    /**
     * Acción masiva (escena, Apagar todo, Deshacer, Restaurar demo): se aplica en local y, si dejó la
     * Lámpara distinta, envía UNA orden con su estado final. Si no la cambió, no envía nada.
     */
    fun runHubAction(action: String, change: () -> Unit) {
        val before = lightStateHolder.toMap()
        val undoBefore = undoSnapshot
        val origin = currentScreen
        change()
        val lampBefore = before[HUB_LIGHT_ID]
        val lampAfter = lightStateHolder.states[HUB_LIGHT_ID]
        if (lampBefore == lampAfter) {
            Log.i(HUB_SYNC_TAG, "$action: la Lámpara no cambia (${encodeLightState(lampAfter)}), no se envía")
            return
        }
        Log.i(HUB_SYNC_TAG, "$action: Lámpara ${encodeLightState(lampBefore)} → ${encodeLightState(lampAfter)}, se envía")
        // Igual que en changeLight: con una orden ya pendiente se conserva el último estado confirmado.
        if (hubStatus == HubStatus.IDLE) hubRollback = encodeLightState(lampBefore)
        hubAction = action
        hubActionSnapshot = serializeLightStates(before)
        hubActionUndo = undoBefore
        hubActionOrigin = origin
        hubTarget = encodeLightState(lampAfter)
        startHubRequest()
    }

    fun turnOffAll() = runHubAction(HUB_ACTION_TURN_OFF_ALL) { lightStateHolder.turnOffAll() }

    fun restoreDemo() = runHubAction(HUB_ACTION_RESTORE_DEMO) {
        updateFavorites(parseFavoriteIds(DEFAULT_FAVORITES))
        lightStateHolder.restore(defaultLightStates())
        undoSnapshot = ""
        selectedSceneId = ""
        suggestionDismissed = false
        openedFromFavorites = false
        currentScreen = ScreenType.FAVORITES.name
    }

    fun activateScene(scene: Scene) = runHubAction(HUB_ACTION_SCENE_PREFIX + scene.id) {
        undoSnapshot = serializeLightStates(lightStateHolder.toMap())
        lightStateHolder.applyScene(scene)
        selectedSceneId = scene.id
        currentScreen = ScreenType.SCENE_ACTIVE.name
    }

    fun undoScene() = runHubAction(HUB_ACTION_UNDO) {
        lightStateHolder.restore(parseLightStates(undoSnapshot))
        undoSnapshot = ""
        currentScreen = ScreenType.SCENE_DETAIL.name
    }

    fun clearHubOrder() {
        hubRollback = ""
        hubAction = ""
        hubStatus = HubStatus.IDLE
    }

    fun cancelHub() {
        rollbackHubOrder()
        clearHubOrder()
    }

    fun retryHub() {
        val action = hubAction
        if (action.isEmpty()) {
            decodeLightState(hubTarget)?.let { lightStateHolder.restore(mapOf(HUB_LIGHT_ID to it)) }
            startHubRequest()
            return
        }
        // Las luces ya volvieron al estado previo: se repite la acción completa.
        clearHubOrder()
        when {
            action == HUB_ACTION_TURN_OFF_ALL -> turnOffAll()
            action == HUB_ACTION_UNDO -> undoScene()
            action == HUB_ACTION_RESTORE_DEMO -> restoreDemo()
            action.startsWith(HUB_ACTION_SCENE_PREFIX) ->
                sceneById(action.removePrefix(HUB_ACTION_SCENE_PREFIX))?.let { activateScene(it) }
        }
    }

    fun backFromHubError() {
        // Tras una acción masiva ya se volvió a la pantalla de origen; si no, al control de la Lámpara.
        if (hubAction.isEmpty()) {
            selectedLightId = HUB_LIGHT_ID
            currentScreen = ScreenType.LIGHT_CONTROL.name
        }
        clearHubOrder()
    }

    // Vive fuera de las pantallas: un cambio pendiente no se pierde al navegar. Tras recrear en
    // CONNECTING la orden se reenvía (es idempotente: lleva el estado completo).
    LaunchedEffect(hubStatus, hubTarget) {
        if (hubStatus != HubStatus.CONNECTING) hubConnectingVisible = false
        when (hubStatus) {
            HubStatus.DEBOUNCING -> {
                delay(HUB_DEBOUNCE_MS)
                startHubRequest()
            }
            HubStatus.CONNECTING -> {
                val target = decodeLightState(hubTarget)
                if (target == null) {
                    hubStatus = HubStatus.IDLE
                    return@LaunchedEffect
                }
                val reveal = launch {
                    delay(HUB_CONNECTING_REVEAL_MS)
                    hubConnectingVisible = true
                }
                val result = providers.providerFor(HUB_LIGHT_ID)
                    .setLight(HUB_LIGHT_ID, target.isOn, target.brightness)
                reveal.cancel()
                if (result.isSuccess) {
                    clearHubOrder()
                } else {
                    rollbackHubOrder()
                    hubStatus = HubStatus.ERROR
                }
            }
            // PERMISSION lo resuelve el callback del diálogo (también tras recrear la Activity).
            HubStatus.IDLE, HubStatus.PERMISSION, HubStatus.ERROR -> Unit
        }
    }
    val hubOverlay = (hubStatus == HubStatus.CONNECTING && hubConnectingVisible) || hubStatus == HubStatus.ERROR

    BackHandler(currentScreen != ScreenType.HOME.name || hubOverlay) {
        if (hubStatus == HubStatus.CONNECTING && hubConnectingVisible) {
            cancelHub()
            return@BackHandler
        }
        if (hubStatus == HubStatus.ERROR) {
            backFromHubError()
            return@BackHandler
        }
        when (currentScreen) {
            ScreenType.LIGHT_CONTROL.name -> {
                if (openedFromFavorites) {
                    openedFromFavorites = false
                    currentScreen = ScreenType.FAVORITES.name
                } else {
                    val backLight = appLights.firstOrNull { it.id == selectedLightId }
                    if (backLight != null) selectedRoomId = backLight.roomId
                    currentScreen = ScreenType.ROOM_DETAIL.name
                }
            }
            ScreenType.FAVORITES.name -> currentScreen = ScreenType.HOME.name
            ScreenType.FAVORITES_EDIT.name -> currentScreen = ScreenType.FAVORITES.name
            ScreenType.ROOM_DETAIL.name -> currentScreen = ScreenType.ROOMS.name
            ScreenType.ROOMS.name -> currentScreen = ScreenType.HOME.name
            ScreenType.SETTINGS.name -> currentScreen = ScreenType.HOME.name
            ScreenType.SUGGESTION.name -> currentScreen = ScreenType.HOME.name
            ScreenType.SCENES.name -> currentScreen = ScreenType.HOME.name
            ScreenType.SCENE_DETAIL.name -> {
                if (openedFromFavorites) {
                    openedFromFavorites = false
                    currentScreen = ScreenType.FAVORITES.name
                } else {
                    currentScreen = ScreenType.SCENES.name
                }
            }
            ScreenType.SCENE_ACTIVE.name -> {
                undoSnapshot = ""
                openedFromFavorites = false
                currentScreen = ScreenType.SCENES.name
            }
        }
    }

    LuminaWearTheme(themeMode = themeMode) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val stateHolder = rememberSaveableStateHolder()
            val currentKey = when {
                hubOverlay -> "HUB_${hubStatus.name}"
                else -> when (currentScreen) {
                    ScreenType.ROOM_DETAIL.name -> "${currentScreen}_$selectedRoomId"
                    ScreenType.LIGHT_CONTROL.name -> "${currentScreen}_$selectedLightId"
                    else -> currentScreen
                }
            }

            stateHolder.SaveableStateProvider(currentKey) {
                val listState = rememberTransformingLazyColumnState()
                val transformationSpec = rememberTransformationSpec()
                ScreenScaffold(
                    scrollState = listState,
                ) { contentPadding ->
                    if (hubStatus == HubStatus.CONNECTING) {
                        HubConnectingScreen(
                            onCancelClick = ::cancelHub,
                            contentPadding = contentPadding,
                            listState = listState,
                            transformationSpec = transformationSpec
                        )
                    } else if (hubStatus == HubStatus.ERROR) {
                        HubErrorScreen(
                            onRetryClick = ::retryHub,
                            onBackClick = ::backFromHubError,
                            contentPadding = contentPadding,
                            listState = listState,
                            transformationSpec = transformationSpec
                        )
                    } else when (currentScreen) {
                        ScreenType.HOME.name -> {
                            HomeScreen(
                                lightsOn = totalLightsOn,
                                totalLights = totalLights,
                                onTurnOffAllClick = ::turnOffAll,
                                onRoomsClick = { currentScreen = ScreenType.ROOMS.name },
                                onScenesClick = { currentScreen = ScreenType.SCENES.name },
                                onFavoritesClick = { currentScreen = ScreenType.FAVORITES.name },
                                onSettingsClick = { currentScreen = ScreenType.SETTINGS.name },
                                suggestion = if (suggestionDismissed) null else suggestion,
                                onSuggestionClick = { currentScreen = ScreenType.SUGGESTION.name },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.FAVORITES.name -> {
                            FavoritesScreen(
                                favorites = favorites,
                                onFavoriteClick = { favorite ->
                                    openedFromFavorites = true
                                    when (favorite) {
                                        is Favorite.LightFavorite -> {
                                            selectedLightId = favorite.light.id
                                            currentScreen = ScreenType.LIGHT_CONTROL.name
                                        }
                                        is Favorite.SceneFavorite -> {
                                            selectedSceneId = favorite.scene.id
                                            currentScreen = ScreenType.SCENE_DETAIL.name
                                        }
                                    }
                                },
                                onEditClick = { currentScreen = ScreenType.FAVORITES_EDIT.name },
                                onRemoveClick = ::removeFavorite,
                                onRestoreDemoClick = ::restoreDemo,
                                onAddPlafonClick = {
                                    updateFavorites(parseFavoriteIds(favoritesRaw) + FAVORITE_PLAFON_ID)
                                },
                                onHomeClick = { currentScreen = ScreenType.HOME.name },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.FAVORITES_EDIT.name -> {
                            FavoritesEditScreen(
                                favorites = favorites,
                                onRemoveClick = ::removeFavorite,
                                onBackClick = { currentScreen = ScreenType.FAVORITES.name },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.SCENES.name -> {
                            ScenesScreen(
                                onSceneClick = { sceneId ->
                                    openedFromFavorites = false
                                    selectedSceneId = sceneId
                                    currentScreen = ScreenType.SCENE_DETAIL.name
                                },
                                onHomeClick = { currentScreen = ScreenType.HOME.name },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.SCENE_DETAIL.name -> {
                            val scene = sceneById(selectedSceneId)
                            if (scene == null) {
                                LaunchedEffect(selectedSceneId) { currentScreen = ScreenType.SCENES.name }
                            } else {
                                SceneDetailScreen(
                                    scene = scene,
                                    onActivateClick = { activateScene(scene) },
                                    onScenesClick = {
                                        if (openedFromFavorites) {
                                            openedFromFavorites = false
                                            currentScreen = ScreenType.FAVORITES.name
                                        } else {
                                            currentScreen = ScreenType.SCENES.name
                                        }
                                    },
                                    contentPadding = contentPadding,
                                    listState = listState,
                                    transformationSpec = transformationSpec
                                )
                            }
                        }
                        ScreenType.SCENE_ACTIVE.name -> {
                            val scene = sceneById(selectedSceneId)
                            if (scene == null) {
                                LaunchedEffect(selectedSceneId) { currentScreen = ScreenType.SCENES.name }
                            } else {
                                SceneActiveScreen(
                                    scene = scene,
                                    onUndoClick = ::undoScene,
                                    onHomeClick = {
                                        undoSnapshot = ""
                                        openedFromFavorites = false
                                        currentScreen = ScreenType.HOME.name
                                    },
                                    contentPadding = contentPadding,
                                    listState = listState,
                                    transformationSpec = transformationSpec
                                )
                            }
                        }
                        ScreenType.SUGGESTION.name -> {
                            SuggestionScreen(
                                scene = suggestion,
                                onActivateClick = {
                                    openedFromFavorites = false
                                    activateScene(suggestion)
                                },
                                onViewSceneClick = {
                                    openedFromFavorites = false
                                    selectedSceneId = suggestion.id
                                    currentScreen = ScreenType.SCENE_DETAIL.name
                                },
                                onDismissClick = {
                                    suggestionDismissed = true
                                    currentScreen = ScreenType.HOME.name
                                },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.SETTINGS.name -> {
                            SettingsScreen(
                                themeMode = themeMode,
                                language = language,
                                onThemeModeChange = {
                                    themeMode = it
                                    onThemeModeChange(it)
                                },
                                onLanguageChange = onLanguageChange,
                                onHomeClick = { currentScreen = ScreenType.HOME.name },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.ROOMS.name -> {
                            RoomsScreen(
                                rooms = appRooms,
                                getLightsCount = { roomId ->
                                    val roomLights = appLights.filter { it.roomId == roomId }
                                    val onCount = roomLights.count { lightStateHolder.states[it.id]?.isOn == true }
                                    Pair(onCount, roomLights.size)
                                },
                                onRoomClick = { roomId ->
                                    selectedRoomId = roomId
                                    currentScreen = ScreenType.ROOM_DETAIL.name
                                },
                                onBackClick = { currentScreen = ScreenType.HOME.name },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.ROOM_DETAIL.name -> {
                            val room = appRooms.firstOrNull { it.id == selectedRoomId }
                            if (room == null) {
                                LaunchedEffect(selectedRoomId) {
                                    currentScreen = ScreenType.ROOMS.name
                                }
                            } else {
                                val roomLights = appLights.filter { it.roomId == selectedRoomId }
                                RoomDetailScreen(
                                    roomNameRes = room.nameRes,
                                    lights = roomLights,
                                    lightStateHolder = lightStateHolder,
                                    onLightClick = { lightId ->
                                        openedFromFavorites = false
                                        selectedLightId = lightId
                                        currentScreen = ScreenType.LIGHT_CONTROL.name
                                    },
                                    onBackClick = { currentScreen = ScreenType.ROOMS.name },
                                    contentPadding = contentPadding,
                                    listState = listState,
                                    transformationSpec = transformationSpec
                                )
                            }
                        }
                        ScreenType.LIGHT_CONTROL.name -> {
                            val light = appLights.firstOrNull { it.id == selectedLightId }
                            if (light == null) {
                                LaunchedEffect(selectedLightId) {
                                    currentScreen = ScreenType.ROOMS.name
                                }
                            } else {
                                val room = appRooms.firstOrNull { it.id == light.roomId }
                                val state = lightStateHolder.states[light.id] ?: LightState(false, 50)

                                LightControlScreen(
                                    light = light,
                                    roomNameRes = room?.nameRes ?: R.string.room_living,
                                    isOn = state.isOn,
                                    brightness = state.brightness,
                                    onBrightnessChange = { newBr ->
                                        changeLight(light.id, immediate = false) {
                                            lightStateHolder.setBrightness(light.id, newBr)
                                        }
                                    },
                                    onTogglePower = {
                                        changeLight(light.id, immediate = true) {
                                            lightStateHolder.setLightOn(light.id, !state.isOn)
                                        }
                                    },
                                    onBackClick = {
                                        if (openedFromFavorites) {
                                            openedFromFavorites = false
                                            currentScreen = ScreenType.FAVORITES.name
                                        } else {
                                            selectedRoomId = light.roomId
                                            currentScreen = ScreenType.ROOM_DETAIL.name
                                        }
                                    },
                                    contentPadding = contentPadding,
                                    listState = listState,
                                    transformationSpec = transformationSpec
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Previews
private val previewInitialLightsOn = "living_plafon,living_lampara,bed_techo,bed_mesa,study_escritorio".split(",").toSet()

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewHomeLightsOn() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                HomeScreen(
                    lightsOn = 5,
                    totalLights = 8,
                    onTurnOffAllClick = {},
                    onRoomsClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewHomeAllOff() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                HomeScreen(
                    lightsOn = 0,
                    totalLights = 8,
                    onTurnOffAllClick = {},
                    onRoomsClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewRoomsScreen() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomsScreen(
                    rooms = appRooms,
                    getLightsCount = { roomId ->
                        val roomLights = appLights.filter { it.roomId == roomId }
                        val onCount = roomLights.count { previewInitialLightsOn.contains(it.id) }
                        Pair(onCount, roomLights.size)
                    },
                    onRoomClick = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewRoomsScreenAllOff() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomsScreen(
                    rooms = appRooms,
                    getLightsCount = { roomId ->
                        val roomLights = appLights.filter { it.roomId == roomId }
                        Pair(0, roomLights.size)
                    },
                    onRoomClick = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewRoomDetailScreen() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            val holder = rememberLightStateHolder()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomDetailScreen(
                    roomNameRes = R.string.room_living,
                    lights = appLights.filter { it.roomId == "living_room" },
                    lightStateHolder = holder,
                    onLightClick = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewRoomDetailScreenAllOff() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            val holder = LightStateHolder(emptyMap())
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomDetailScreen(
                    roomNameRes = R.string.room_living,
                    lights = appLights.filter { it.roomId == "living_room" },
                    lightStateHolder = holder,
                    onLightClick = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewLightControlOn() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                LightControlScreen(
                    light = appLights.first { it.id == "living_plafon" },
                    roomNameRes = R.string.room_living,
                    isOn = true,
                    brightness = 72,
                    onBrightnessChange = {},
                    onTogglePower = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewLightControlOff() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                LightControlScreen(
                    light = appLights.first { it.id == "living_plafon" },
                    roomNameRes = R.string.room_living,
                    isOn = false,
                    brightness = 72,
                    onBrightnessChange = {},
                    onTogglePower = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewLightControlMin() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                LightControlScreen(
                    light = appLights.first { it.id == "living_plafon" },
                    roomNameRes = R.string.room_living,
                    isOn = true,
                    brightness = 1,
                    onBrightnessChange = {},
                    onTogglePower = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewLightControlMax() {
    LuminaWearTheme {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                LightControlScreen(
                    light = appLights.first { it.id == "living_plafon" },
                    roomNameRes = R.string.room_living,
                    isOn = true,
                    brightness = 100,
                    onBrightnessChange = {},
                    onTogglePower = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

// Previews por tema: claro (predeterminado) y oscuro

@WearPreviewDevices
@Composable
fun PreviewHomeLight() {
    LuminaWearTheme(themeMode = ThemeMode.LIGHT) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                HomeScreen(
                    lightsOn = 5,
                    totalLights = 8,
                    onTurnOffAllClick = {},
                    onRoomsClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewHomeDark() {
    LuminaWearTheme(themeMode = ThemeMode.DARK) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                HomeScreen(
                    lightsOn = 5,
                    totalLights = 8,
                    onTurnOffAllClick = {},
                    onRoomsClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewRoomsLight() {
    LuminaWearTheme(themeMode = ThemeMode.LIGHT) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomsScreen(
                    rooms = appRooms,
                    getLightsCount = { roomId ->
                        val roomLights = appLights.filter { it.roomId == roomId }
                        val onCount = roomLights.count { previewInitialLightsOn.contains(it.id) }
                        Pair(onCount, roomLights.size)
                    },
                    onRoomClick = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewRoomsDark() {
    LuminaWearTheme(themeMode = ThemeMode.DARK) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomsScreen(
                    rooms = appRooms,
                    getLightsCount = { roomId ->
                        val roomLights = appLights.filter { it.roomId == roomId }
                        val onCount = roomLights.count { previewInitialLightsOn.contains(it.id) }
                        Pair(onCount, roomLights.size)
                    },
                    onRoomClick = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewRoomDetailLight() {
    LuminaWearTheme(themeMode = ThemeMode.LIGHT) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            val holder = rememberLightStateHolder()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomDetailScreen(
                    roomNameRes = R.string.room_living,
                    lights = appLights.filter { it.roomId == "living_room" },
                    lightStateHolder = holder,
                    onLightClick = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewRoomDetailDark() {
    LuminaWearTheme(themeMode = ThemeMode.DARK) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            val holder = rememberLightStateHolder()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomDetailScreen(
                    roomNameRes = R.string.room_living,
                    lights = appLights.filter { it.roomId == "living_room" },
                    lightStateHolder = holder,
                    onLightClick = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewLightControlLight() {
    LuminaWearTheme(themeMode = ThemeMode.LIGHT) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                LightControlScreen(
                    light = appLights.first { it.id == "living_plafon" },
                    roomNameRes = R.string.room_living,
                    isOn = true,
                    brightness = 80,
                    onBrightnessChange = {},
                    onTogglePower = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewLightControlDark() {
    LuminaWearTheme(themeMode = ThemeMode.DARK) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                LightControlScreen(
                    light = appLights.first { it.id == "living_plafon" },
                    roomNameRes = R.string.room_living,
                    isOn = true,
                    brightness = 80,
                    onBrightnessChange = {},
                    onTogglePower = {},
                    onBackClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewSettingsLight() {
    LuminaWearTheme(themeMode = ThemeMode.LIGHT) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                SettingsScreen(
                    themeMode = ThemeMode.LIGHT,
                    language = AppLanguage.ES,
                    onThemeModeChange = {},
                    onLanguageChange = {},
                    onHomeClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewSettingsDark() {
    LuminaWearTheme(themeMode = ThemeMode.DARK) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                SettingsScreen(
                    themeMode = ThemeMode.DARK,
                    language = AppLanguage.EN,
                    onThemeModeChange = {},
                    onLanguageChange = {},
                    onHomeClick = {},
                    contentPadding = contentPadding,
                    listState = listState,
                    transformationSpec = transformationSpec
                )
            }
        }
    }
}

// Previews de Favoritos (LW-009b): lista, edición, un favorito y vacío; claro y oscuro.

private val previewFavoritesTwo = resolveFavorites(DEFAULT_FAVORITES)
private val previewFavoritesOne = resolveFavorites("relax")

@Composable
private fun FavoritesPreviewHost(
    themeMode: ThemeMode,
    content: @Composable (PaddingValues, TransformingLazyColumnState, TransformationSpec) -> Unit
) {
    LuminaWearTheme(themeMode = themeMode) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                content(contentPadding, listState, transformationSpec)
            }
        }
    }
}

@Composable
private fun PreviewFavorites(themeMode: ThemeMode, favorites: List<Favorite>) {
    FavoritesPreviewHost(themeMode) { padding, state, spec ->
        FavoritesScreen(
            favorites = favorites,
            onFavoriteClick = {},
            onEditClick = {},
            onRemoveClick = {},
            onRestoreDemoClick = {},
            onAddPlafonClick = {},
            onHomeClick = {},
            contentPadding = padding,
            listState = state,
            transformationSpec = spec
        )
    }
}

@Composable
private fun PreviewFavoritesEdit(themeMode: ThemeMode) {
    FavoritesPreviewHost(themeMode) { padding, state, spec ->
        FavoritesEditScreen(
            favorites = previewFavoritesTwo,
            onRemoveClick = {},
            onBackClick = {},
            contentPadding = padding,
            listState = state,
            transformationSpec = spec
        )
    }
}

@WearPreviewDevices
@Composable
fun PreviewFavoritesLight() = PreviewFavorites(ThemeMode.LIGHT, previewFavoritesTwo)

@WearPreviewDevices
@Composable
fun PreviewFavoritesDark() = PreviewFavorites(ThemeMode.DARK, previewFavoritesTwo)

@WearPreviewDevices
@Composable
fun PreviewFavoritesEditLight() = PreviewFavoritesEdit(ThemeMode.LIGHT)

@WearPreviewDevices
@Composable
fun PreviewFavoritesEditDark() = PreviewFavoritesEdit(ThemeMode.DARK)

@WearPreviewDevices
@Composable
fun PreviewFavoritesOneLight() = PreviewFavorites(ThemeMode.LIGHT, previewFavoritesOne)

@WearPreviewDevices
@Composable
fun PreviewFavoritesOneDark() = PreviewFavorites(ThemeMode.DARK, previewFavoritesOne)

@WearPreviewDevices
@Composable
fun PreviewFavoritesEmptyLight() = PreviewFavorites(ThemeMode.LIGHT, emptyList())

@WearPreviewDevices
@Composable
fun PreviewFavoritesEmptyDark() = PreviewFavorites(ThemeMode.DARK, emptyList())

// Previews del hub (LW-010a): conectando y error, claro y oscuro.

@WearPreviewDevices
@Composable
fun PreviewHubConnectingLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    HubConnectingScreen(onCancelClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@Composable
fun PreviewHubConnectingDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    HubConnectingScreen(onCancelClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@Composable
fun PreviewHubErrorLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    HubErrorScreen(onRetryClick = {}, onBackClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@Composable
fun PreviewHubErrorDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    HubErrorScreen(onRetryClick = {}, onBackClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}
