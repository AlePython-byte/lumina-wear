package com.alejandro.luminawear.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.alejandro.luminawear.BuildConfig
import com.alejandro.luminawear.R
import com.alejandro.luminawear.data.AssistantClient
import com.alejandro.luminawear.data.AssistantResult
import com.alejandro.luminawear.data.DEFAULT_HUB_URL
import com.alejandro.luminawear.data.HUB_LIGHT_ID
import com.alejandro.luminawear.data.LOCAL_NETWORK_PERMISSION
import com.alejandro.luminawear.data.LightProviders
import com.alejandro.luminawear.data.needsLocalNetworkPermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import com.alejandro.luminawear.presentation.assistant.AssistantConfirmScreen
import com.alejandro.luminawear.presentation.assistant.AssistantDoneScreen
import com.alejandro.luminawear.presentation.assistant.AssistantErrorScreen
import com.alejandro.luminawear.presentation.assistant.AssistantInputScreen
import com.alejandro.luminawear.presentation.assistant.AssistantLoadingScreen
import com.alejandro.luminawear.presentation.assistant.AssistantScreen
import com.alejandro.luminawear.presentation.favorites.FavoritesEditScreen
import com.alejandro.luminawear.presentation.favorites.FavoritesScreen
import com.alejandro.luminawear.presentation.home.HomeScreen
import com.alejandro.luminawear.presentation.lights.HubConnectingScreen
import com.alejandro.luminawear.presentation.lights.HubErrorScreen
import com.alejandro.luminawear.presentation.lights.LightControlScreen
import com.alejandro.luminawear.presentation.models.demoLights
import com.alejandro.luminawear.presentation.onboarding.StartOverScreen
import com.alejandro.luminawear.presentation.onboarding.StartPointScreen
import com.alejandro.luminawear.presentation.onboarding.WelcomeScreen
import com.alejandro.luminawear.presentation.models.lightName
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
import com.alejandro.luminawear.presentation.state.ASSISTANT_MAX_TEXT
import com.alejandro.luminawear.presentation.state.AssistantAction
import com.alejandro.luminawear.presentation.state.AssistantError
import com.alejandro.luminawear.presentation.state.AssistantLight
import com.alejandro.luminawear.presentation.state.applyAssistantActions
import com.alejandro.luminawear.presentation.state.decodeAssistantActions
import com.alejandro.luminawear.presentation.state.encodeAssistantActions
import com.alejandro.luminawear.presentation.state.parseLocalCommand
import com.alejandro.luminawear.presentation.state.validateAssistantActions
import com.alejandro.luminawear.presentation.state.DEFAULT_FAVORITES
import com.alejandro.luminawear.presentation.state.HUB_ACTION_ASSISTANT_PREFIX
import com.alejandro.luminawear.presentation.state.HUB_ACTION_ASSISTANT_UNDO
import com.alejandro.luminawear.presentation.state.FAVORITE_PLAFON_ID
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
import com.alejandro.luminawear.presentation.state.DEFAULT_REMEMBERED_BRIGHTNESS
import com.alejandro.luminawear.presentation.state.DEMO_LIGHTS_CONFIG
import com.alejandro.luminawear.presentation.state.LightsConfig
import com.alejandro.luminawear.presentation.state.lightAliases
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
private const val ASSISTANT_TAG = "Assistant"

/** Clave de estado de la lista de las pantallas Conectando / No se pudo del hub. */
private fun hubStatusKey(status: HubStatus) = "HUB_${status.name}"

/**
 * Claves de las pantallas de un solo paso (resultado y estado): no se vuelven a abrir donde se
 * dejaron, siempre empiezan arriba.
 */
private val START_AT_TOP_KEYS = setOf(
    ScreenType.ASSISTANT_LOADING.name,
    ScreenType.ASSISTANT_CONFIRM.name,
    ScreenType.ASSISTANT_DONE.name,
    ScreenType.ASSISTANT_ERROR.name,
    hubStatusKey(HubStatus.CONNECTING),
    hubStatusKey(HubStatus.ERROR)
)

enum class ScreenType {
    HOME, ROOMS, ROOM_DETAIL, LIGHT_CONTROL, SETTINGS, SCENES, SCENE_DETAIL, SCENE_ACTIVE, SUGGESTION, FAVORITES, FAVORITES_EDIT,
    ASSISTANT, ASSISTANT_INPUT, ASSISTANT_LOADING, ASSISTANT_CONFIRM, ASSISTANT_DONE, ASSISTANT_ERROR,
    WELCOME, START_POINT, START_OVER
}

@Composable
fun WearApp(
    initialThemeMode: ThemeMode = ThemeMode.LIGHT,
    language: AppLanguage = AppLanguage.ES,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit = {},
    initialFavorites: String = DEFAULT_FAVORITES,
    onFavoritesChange: (String) -> Unit = {},
    hubUrl: String = DEFAULT_HUB_URL,
    initialLights: LightsConfig = DEMO_LIGHTS_CONFIG,
    onLightsChange: (LightsConfig) -> Unit = {},
    onboardingDone: Boolean = true,
    onOnboardingDoneChange: (Boolean) -> Unit = {}
) {
    val lightStateHolder = rememberLightStateHolder(initialLights, onLightsChange)
    // Luces que existen ahora, para dibujar. Las funciones de abajo leen lightStateHolder.lights: una
    // lambda guardada (efecto, callback) vería la lista de la composición en que se creó.
    val lights = lightStateHolder.lights

    val totalLights = lights.size
    val totalLightsOn = lightStateHolder.states.count { it.value.isOn }

    // Sin primer uso completado se empieza en la bienvenida.
    var currentScreen by rememberSaveable {
        mutableStateOf(if (onboardingDone) ScreenType.HOME.name else ScreenType.WELCOME.name)
    }
    // Sube al elegir punto de partida o volver a empezar; reinicia el desplazamiento guardado de cada pantalla.
    var houseGeneration by rememberSaveable { mutableStateOf(0) }
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
    var favoritesRaw by rememberSaveable { mutableStateOf(serializeFavoriteIds(parseFavoriteIds(initialFavorites, lights))) }
    // true cuando el control de luz o el detalle de escena se abrieron desde Favoritos (atrás vuelve allí).
    var openedFromFavorites by rememberSaveable { mutableStateOf(false) }
    val favorites = resolveFavorites(favoritesRaw, lights)

    // Hub: solo la luz del hub (HUB_LIGHT_ID) pide confirmación remota; las demás cambian directo.
    val providers = remember(hubUrl) { LightProviders(hubUrl) }
    var hubStatus by rememberSaveable { mutableStateOf(HubStatus.IDLE) }
    // Orden pendiente y último estado confirmado de la Lámpara, en formato "1:45".
    var hubTarget by rememberSaveable { mutableStateOf("") }
    var hubRollback by rememberSaveable { mutableStateOf("") }
    // Último estado de la Lámpara que el hub confirmó ("" = no se sabe). Una orden igual a ese estado
    // no se vuelve a enviar (p. ej. subir de más y corregir hasta el valor que ya estaba confirmado).
    // No se guarda: si la app se recrea, otro cliente pudo cambiar el hub, así que se vuelve a enviar.
    var hubConfirmed by remember { mutableStateOf("") }
    // Acción masiva en curso (HUB_ACTION_*; "" = control de la luz) y lo necesario para deshacerla:
    // snapshot de todas las luces (mismo formato que Deshacer), Deshacer previo y pantalla de origen.
    var hubAction by rememberSaveable { mutableStateOf("") }
    var hubActionSnapshot by rememberSaveable { mutableStateOf("") }
    var hubActionUndo by rememberSaveable { mutableStateOf("") }
    var hubActionOrigin by rememberSaveable { mutableStateOf("") }
    // CONECTANDO solo se muestra si el hub tarda más de HUB_CONNECTING_REVEAL_MS.
    var hubConnectingVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Asistente: borrador del campo de texto, pedido en curso, propuesta validada y error.
    val assistantClient = remember { AssistantClient(BuildConfig.ASSISTANT_URL, BuildConfig.ASSISTANT_TOKEN) }
    var assistantDraft by rememberSaveable { mutableStateOf("") }
    var assistantRequest by rememberSaveable { mutableStateOf("") }
    var assistantFromInput by rememberSaveable { mutableStateOf(false) }
    var assistantReply by rememberSaveable { mutableStateOf("") }
    var assistantActions by rememberSaveable { mutableStateOf("") }
    var assistantError by rememberSaveable { mutableStateOf(AssistantError.NOT_UNDERSTOOD.name) }

    fun updateFavorites(ids: List<String>) {
        favoritesRaw = serializeFavoriteIds(ids)
        onFavoritesChange(favoritesRaw)
    }

    fun removeFavorite(favorite: Favorite) {
        val remaining = parseFavoriteIds(favoritesRaw, lightStateHolder.lights) - favorite.id
        updateFavorites(remaining)
        if (remaining.isEmpty()) currentScreen = ScreenType.FAVORITES.name
    }

    // Al quitar luces, sus ids salen de los favoritos guardados (solo se escribe si algo cambió).
    LaunchedEffect(lights) {
        val existing = parseFavoriteIds(favoritesRaw, lights)
        if (serializeFavoriteIds(existing) != favoritesRaw) updateFavorites(existing)
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
        // Sin cambio real sobre la orden ya pendiente o en curso: reiniciarla cancelaría el envío en
        // vuelo y lo repetiría con el mismo valor.
        if (before == after && encodeLightState(after) == hubTarget) return
        hubAction = ""
        hubTarget = encodeLightState(after)
        if (immediate) startHubRequest() else hubStatus = HubStatus.DEBOUNCING
    }

    /**
     * Acción masiva (escena, Apagar todo, Deshacer, asistente): se aplica en local y, si dejó la
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

    /** Punto de partida elegido: casa demo o casa vacía. Desde aquí ya no se vuelve a la bienvenida. */
    fun finishOnboarding(demo: Boolean) {
        if (demo) {
            lightStateHolder.loadDemo()
            // Con la casa demo los favoritos por defecto vuelven completos (el plafón ya existe).
            updateFavorites(parseFavoriteIds(DEFAULT_FAVORITES, lightStateHolder.lights))
        } else {
            lightStateHolder.clear()
        }
        onOnboardingDoneChange(true)
        houseGeneration++
        currentScreen = ScreenType.HOME.name
    }

    /** "Borrar y empezar": sin luces, favoritos por defecto, primer uso pendiente y vuelta a la bienvenida. */
    fun startOver() {
        lightStateHolder.clear()
        updateFavorites(parseFavoriteIds(DEFAULT_FAVORITES, lightStateHolder.lights))
        undoSnapshot = ""
        selectedSceneId = ""
        suggestionDismissed = false
        openedFromFavorites = false
        onOnboardingDoneChange(false)
        houseGeneration++
        currentScreen = ScreenType.WELCOME.name
    }

    fun activateScene(scene: Scene) {
        // Sin luces sobre las que actuar no se aplica nada: el detalle muestra "Sin luces en …".
        if (lightStateHolder.isSceneWithoutLights(scene)) {
            selectedSceneId = scene.id
            currentScreen = ScreenType.SCENE_DETAIL.name
            return
        }
        runHubAction(HUB_ACTION_SCENE_PREFIX + scene.id) {
            undoSnapshot = serializeLightStates(lightStateHolder.toMap())
            lightStateHolder.applyScene(scene)
            selectedSceneId = scene.id
            currentScreen = ScreenType.SCENE_ACTIVE.name
        }
    }

    fun undoScene() = runHubAction(HUB_ACTION_UNDO) {
        lightStateHolder.restore(parseLightStates(undoSnapshot))
        undoSnapshot = ""
        currentScreen = ScreenType.SCENE_DETAIL.name
    }

    /** Luces con nombre y habitación en el idioma de la app, para las reglas y el servidor. */
    fun assistantLights(): List<AssistantLight> = lightStateHolder.lights.map { light ->
        val state = lightStateHolder.states[light.id] ?: LightState(false, DEFAULT_REMEMBERED_BRIGHTNESS)
        val room = appRooms.firstOrNull { it.id == light.roomId }
        AssistantLight(
            id = light.id,
            name = lightName(context, light),
            room = room?.let { context.getString(it.nameRes) }.orEmpty(),
            on = state.isOn,
            brightness = state.brightness,
            aliases = lightAliases(light.type.key, light.number)
        )
    }

    fun showAssistantError(error: AssistantError) {
        assistantError = error.name
        currentScreen = ScreenType.ASSISTANT_ERROR.name
    }

    /** Valida siempre (venga de las reglas o del servidor) y pide confirmación; si no queda nada, "no entendí". */
    fun proposeAssistantActions(reply: String, actions: List<AssistantAction>) {
        val valid = validateAssistantActions(actions, lightStateHolder.states)
        Log.i(ASSISTANT_TAG, "${actions.size} acciones recibidas, ${valid.size} válidas")
        assistantReply = reply
        if (valid.isEmpty()) {
            showAssistantError(AssistantError.NOT_UNDERSTOOD)
        } else {
            assistantActions = encodeAssistantActions(valid)
            currentScreen = ScreenType.ASSISTANT_CONFIRM.name
        }
    }

    /** Reglas locales primero; si no entienden y hay configuración, se consulta al servidor. */
    fun submitAssistant(text: String, fromInput: Boolean) {
        val request = text.trim().take(ASSISTANT_MAX_TEXT)
        if (request.isEmpty()) return
        assistantRequest = request
        assistantFromInput = fromInput
        assistantReply = ""
        val local = parseLocalCommand(request, assistantLights())
        when {
            // Sin luces no hay nada que proponer: ni reglas ni servidor.
            lightStateHolder.lights.isEmpty() -> showAssistantError(AssistantError.NO_LIGHTS)
            local != null -> {
                Log.i(ASSISTANT_TAG, "Reglas locales: ${local.size} acciones")
                proposeAssistantActions(reply = "", actions = local)
            }
            !assistantClient.isConfigured -> showAssistantError(AssistantError.NOT_CONFIGURED)
            else -> currentScreen = ScreenType.ASSISTANT_LOADING.name
        }
    }

    /** Vuelve a donde se hizo el pedido (campo de texto o frases rápidas). */
    fun backToAssistantOrigin() {
        currentScreen = if (assistantFromInput) ScreenType.ASSISTANT_INPUT.name else ScreenType.ASSISTANT.name
    }

    /** Aplica por la misma ruta que las escenas: una sola orden al hub si cambia la Lámpara, con rollback. */
    fun applyAssistant(encoded: String) = runHubAction(HUB_ACTION_ASSISTANT_PREFIX + encoded) {
        val actions = validateAssistantActions(decodeAssistantActions(encoded), lightStateHolder.states)
        undoSnapshot = serializeLightStates(lightStateHolder.toMap())
        lightStateHolder.restore(applyAssistantActions(lightStateHolder.toMap(), actions, lightStateHolder.lights))
        assistantActions = encoded
        currentScreen = ScreenType.ASSISTANT_DONE.name
    }

    fun undoAssistant() = runHubAction(HUB_ACTION_ASSISTANT_UNDO) {
        lightStateHolder.restore(parseLightStates(undoSnapshot))
        undoSnapshot = ""
        currentScreen = ScreenType.ASSISTANT.name
    }

    fun clearHubOrder() {
        hubRollback = ""
        hubAction = ""
        hubStatus = HubStatus.IDLE
    }

    fun cancelHub() {
        // La orden cancelada pudo llegar al hub: ya no se sabe qué estado tiene.
        hubConfirmed = ""
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
            action == HUB_ACTION_ASSISTANT_UNDO -> undoAssistant()
            action.startsWith(HUB_ACTION_ASSISTANT_PREFIX) -> applyAssistant(action.removePrefix(HUB_ACTION_ASSISTANT_PREFIX))
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
                if (hubAction.isEmpty() && hubTarget == hubConfirmed) {
                    // El hub ya tiene este estado: solo se envía la última orden que cambia algo.
                    Log.i(HUB_SYNC_TAG, "luz: $hubTarget ya confirmado por el hub, no se reenvía")
                    clearHubOrder()
                } else {
                    startHubRequest()
                }
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
                    hubConfirmed = encodeLightState(target)
                    clearHubOrder()
                } else {
                    // Puede haberse aplicado sin que llegara la confirmación: ya no se sabe qué tiene el hub.
                    hubConfirmed = ""
                    rollbackHubOrder()
                    hubStatus = HubStatus.ERROR
                }
            }
            // PERMISSION lo resuelve el callback del diálogo (también tras recrear la Activity).
            HubStatus.IDLE, HubStatus.PERMISSION, HubStatus.ERROR -> Unit
        }
    }
    // Consulta al servidor mientras se muestra ASSISTANT_LOADING. Salir de esa pantalla (Cancelar,
    // Atrás) cancela la corrutina y corta la conexión. Tras recrear la Activity se vuelve a pedir.
    LaunchedEffect(currentScreen, assistantRequest) {
        if (currentScreen != ScreenType.ASSISTANT_LOADING.name) return@LaunchedEffect
        if (lightStateHolder.lights.isEmpty()) {
            showAssistantError(AssistantError.NO_LIGHTS)
            return@LaunchedEffect
        }
        when (val result = assistantClient.ask(assistantRequest, language.tag, assistantLights())) {
            is AssistantResult.Success -> proposeAssistantActions(result.reply, result.actions)
            AssistantResult.NotConfigured -> showAssistantError(AssistantError.NOT_CONFIGURED)
            AssistantResult.NetworkError -> showAssistantError(AssistantError.NETWORK)
            AssistantResult.ServerError -> showAssistantError(AssistantError.SERVER)
        }
    }

    val hubOverlay = (hubStatus == HubStatus.CONNECTING && hubConnectingVisible) || hubStatus == HubStatus.ERROR

    // En Inicio y en Bienvenida, Atrás lo maneja el sistema (cierra la app).
    val isRootScreen = currentScreen == ScreenType.HOME.name || currentScreen == ScreenType.WELCOME.name
    BackHandler(!isRootScreen || hubOverlay) {
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
                    val backLight = lightStateHolder.lightById(selectedLightId)
                    if (backLight != null) selectedRoomId = backLight.roomId
                    currentScreen = ScreenType.ROOM_DETAIL.name
                }
            }
            ScreenType.FAVORITES.name -> currentScreen = ScreenType.HOME.name
            ScreenType.FAVORITES_EDIT.name -> currentScreen = ScreenType.FAVORITES.name
            ScreenType.ROOM_DETAIL.name -> currentScreen = ScreenType.ROOMS.name
            ScreenType.ROOMS.name -> currentScreen = ScreenType.HOME.name
            ScreenType.SETTINGS.name -> currentScreen = ScreenType.HOME.name
            ScreenType.START_POINT.name -> currentScreen = ScreenType.WELCOME.name
            ScreenType.START_OVER.name -> currentScreen = ScreenType.SETTINGS.name
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
            ScreenType.ASSISTANT.name -> currentScreen = ScreenType.HOME.name
            ScreenType.ASSISTANT_INPUT.name -> currentScreen = ScreenType.ASSISTANT.name
            ScreenType.ASSISTANT_LOADING.name,
            ScreenType.ASSISTANT_CONFIRM.name,
            ScreenType.ASSISTANT_ERROR.name -> backToAssistantOrigin()
            ScreenType.ASSISTANT_DONE.name -> {
                undoSnapshot = ""
                currentScreen = ScreenType.ASSISTANT.name
            }
        }
    }

    LuminaWearTheme(themeMode = themeMode) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            // Nueva generación (camino elegido o volver a empezar): las pantallas empiezan arriba, sin el
            // desplazamiento de la casa anterior.
            val stateHolder = key(houseGeneration) { rememberSaveableStateHolder() }
            val currentKey = when {
                // Conectando se dibuja en cuanto empieza la orden (antes de revelarse): con su propia clave
                // y no la de la pantalla anterior, de la que heredaría el desplazamiento.
                hubStatus == HubStatus.CONNECTING || hubStatus == HubStatus.ERROR -> hubStatusKey(hubStatus)
                else -> when (currentScreen) {
                    ScreenType.ROOM_DETAIL.name -> "${currentScreen}_$selectedRoomId"
                    ScreenType.LIGHT_CONTROL.name -> "${currentScreen}_$selectedLightId"
                    else -> currentScreen
                }
            }

            stateHolder.SaveableStateProvider(currentKey) {
                val listState = rememberTransformingLazyColumnState()
                // Pantallas de un solo paso y de estado: cada visita empieza arriba. Si recuerdan el
                // desplazamiento de la visita anterior, el título queda fuera de vista o pegado a la hora.
                if (currentKey in START_AT_TOP_KEYS) {
                    LaunchedEffect(currentKey) { listState.scrollToItem(0) }
                }
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
                                onAssistantClick = { currentScreen = ScreenType.ASSISTANT.name },
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
                                canAddPlafon = lightStateHolder.lightById(FAVORITE_PLAFON_ID) != null,
                                onAddPlafonClick = {
                                    if (lightStateHolder.lightById(FAVORITE_PLAFON_ID) != null) {
                                        updateFavorites(parseFavoriteIds(favoritesRaw, lightStateHolder.lights) + FAVORITE_PLAFON_ID)
                                    }
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
                                    transformationSpec = transformationSpec,
                                    withoutLights = lightStateHolder.isSceneWithoutLights(scene)
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
                        ScreenType.ASSISTANT.name -> {
                            AssistantScreen(
                                hubLight = lightStateHolder.lightById(HUB_LIGHT_ID),
                                onPhraseClick = { phrase -> submitAssistant(phrase, fromInput = false) },
                                onWriteClick = { currentScreen = ScreenType.ASSISTANT_INPUT.name },
                                onHomeClick = { currentScreen = ScreenType.HOME.name },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.ASSISTANT_INPUT.name -> {
                            AssistantInputScreen(
                                text = assistantDraft,
                                onTextChange = { assistantDraft = it },
                                onSendClick = { submitAssistant(assistantDraft, fromInput = true) },
                                onBackClick = { currentScreen = ScreenType.ASSISTANT.name },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.ASSISTANT_LOADING.name -> {
                            AssistantLoadingScreen(
                                onCancelClick = ::backToAssistantOrigin,
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.ASSISTANT_ERROR.name -> {
                            AssistantErrorScreen(
                                error = AssistantError.entries.firstOrNull { it.name == assistantError } ?: AssistantError.NOT_UNDERSTOOD,
                                reply = assistantReply,
                                onRetryClick = { submitAssistant(assistantRequest, assistantFromInput) },
                                onPhrasesClick = { currentScreen = ScreenType.ASSISTANT.name },
                                onBackClick = ::backToAssistantOrigin,
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.ASSISTANT_CONFIRM.name -> {
                            AssistantConfirmScreen(
                                reply = assistantReply,
                                actions = decodeAssistantActions(assistantActions),
                                lights = lights,
                                onApplyClick = { applyAssistant(assistantActions) },
                                onCancelClick = ::backToAssistantOrigin,
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.ASSISTANT_DONE.name -> {
                            AssistantDoneScreen(
                                actions = decodeAssistantActions(assistantActions),
                                lights = lights,
                                onUndoClick = ::undoAssistant,
                                onHomeClick = {
                                    undoSnapshot = ""
                                    currentScreen = ScreenType.HOME.name
                                },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.WELCOME.name -> {
                            WelcomeScreen(
                                onStartClick = { currentScreen = ScreenType.START_POINT.name },
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.START_OVER.name -> {
                            StartOverScreen(
                                onCancelClick = { currentScreen = ScreenType.SETTINGS.name },
                                onConfirmClick = ::startOver,
                                contentPadding = contentPadding,
                                listState = listState,
                                transformationSpec = transformationSpec
                            )
                        }
                        ScreenType.START_POINT.name -> {
                            StartPointScreen(
                                onDemoClick = { finishOnboarding(demo = true) },
                                onEmptyClick = { finishOnboarding(demo = false) },
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
                                onStartOverClick = { currentScreen = ScreenType.START_OVER.name },
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
                                    val roomLights = lightStateHolder.lightsInRoom(roomId)
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
                                val roomLights = lightStateHolder.lightsInRoom(selectedRoomId)
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
                            val light = lightStateHolder.lightById(selectedLightId)
                            if (light == null) {
                                LaunchedEffect(selectedLightId) {
                                    currentScreen = ScreenType.ROOMS.name
                                }
                            } else {
                                val room = appRooms.firstOrNull { it.id == light.roomId }
                                val state = lightStateHolder.states[light.id] ?: LightState(false, DEFAULT_REMEMBERED_BRIGHTNESS)

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
                        val roomLights = demoLights.filter { it.roomId == roomId }
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
                        val roomLights = demoLights.filter { it.roomId == roomId }
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
            val holder = rememberLightStateHolder(DEMO_LIGHTS_CONFIG) {}
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomDetailScreen(
                    roomNameRes = R.string.room_living,
                    lights = demoLights.filter { it.roomId == "living_room" },
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
            val holder = LightStateHolder(DEMO_LIGHTS_CONFIG, emptyMap()).apply { turnOffAll() }
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomDetailScreen(
                    roomNameRes = R.string.room_living,
                    lights = demoLights.filter { it.roomId == "living_room" },
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
                    light = demoLights.first { it.id == "living_plafon" },
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
                    light = demoLights.first { it.id == "living_plafon" },
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
                    light = demoLights.first { it.id == "living_plafon" },
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
                    light = demoLights.first { it.id == "living_plafon" },
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
                        val roomLights = demoLights.filter { it.roomId == roomId }
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
                        val roomLights = demoLights.filter { it.roomId == roomId }
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
            val holder = rememberLightStateHolder(DEMO_LIGHTS_CONFIG) {}
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomDetailScreen(
                    roomNameRes = R.string.room_living,
                    lights = demoLights.filter { it.roomId == "living_room" },
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
            val holder = rememberLightStateHolder(DEMO_LIGHTS_CONFIG) {}
            ScreenScaffold(scrollState = listState) { contentPadding ->
                RoomDetailScreen(
                    roomNameRes = R.string.room_living,
                    lights = demoLights.filter { it.roomId == "living_room" },
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
                    light = demoLights.first { it.id == "living_plafon" },
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
                    light = demoLights.first { it.id == "living_plafon" },
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
                    onStartOverClick = {},
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
                    onStartOverClick = {},
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

private val previewFavoritesTwo = resolveFavorites(DEFAULT_FAVORITES, demoLights)
private val previewFavoritesOne = resolveFavorites("relax", demoLights)

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
            canAddPlafon = true,
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

// Previews del asistente (LW-010b): frases, escribir, confirmar, listo y errores; claro, oscuro y escalas de fuente.

private val previewAssistantActions = listOf(
    AssistantAction.SetLight("living_lampara", on = true, brightness = 50),
    AssistantAction.ApplyScene("read"),
    AssistantAction.SetLight("study_escritorio", on = false, brightness = null)
)

private val previewHubLight = demoLights.first { it.id == HUB_LIGHT_ID }

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewAssistantLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    AssistantScreen(hubLight = previewHubLight, onPhraseClick = {}, onWriteClick = {}, onHomeClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@Composable
fun PreviewAssistantDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    AssistantScreen(hubLight = previewHubLight, onPhraseClick = {}, onWriteClick = {}, onHomeClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewAssistantInputLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    AssistantInputScreen(
        text = "", onTextChange = {}, onSendClick = {}, onBackClick = {},
        contentPadding = padding, listState = state, transformationSpec = spec
    )
}

@WearPreviewDevices
@Composable
fun PreviewAssistantInputDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    AssistantInputScreen(
        text = "Apaga el escritorio y la tira", onTextChange = {}, onSendClick = {}, onBackClick = {},
        contentPadding = padding, listState = state, transformationSpec = spec
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewAssistantConfirmLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    AssistantConfirmScreen(
        reply = "Listo para leer.", actions = previewAssistantActions, lights = demoLights, onApplyClick = {}, onCancelClick = {},
        contentPadding = padding, listState = state, transformationSpec = spec
    )
}

@WearPreviewDevices
@Composable
fun PreviewAssistantConfirmDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    AssistantConfirmScreen(
        reply = "", actions = previewAssistantActions, lights = demoLights, onApplyClick = {}, onCancelClick = {},
        contentPadding = padding, listState = state, transformationSpec = spec
    )
}

@WearPreviewDevices
@Composable
fun PreviewAssistantDoneLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    AssistantDoneScreen(
        actions = previewAssistantActions, lights = demoLights, onUndoClick = {}, onHomeClick = {},
        contentPadding = padding, listState = state, transformationSpec = spec
    )
}

@WearPreviewDevices
@Composable
fun PreviewAssistantLoadingDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    AssistantLoadingScreen(onCancelClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewAssistantNetworkErrorLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    AssistantErrorScreen(
        error = AssistantError.NETWORK, reply = "", onRetryClick = {}, onPhrasesClick = {}, onBackClick = {},
        contentPadding = padding, listState = state, transformationSpec = spec
    )
}

@WearPreviewDevices
@Composable
fun PreviewAssistantNotConfiguredDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    AssistantErrorScreen(
        error = AssistantError.NOT_CONFIGURED, reply = "", onRetryClick = {}, onPhrasesClick = {}, onBackClick = {},
        contentPadding = padding, listState = state, transformationSpec = spec
    )
}

// Previews del primer uso (LW-014b): claro, oscuro y escalas de fuente.

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewWelcomeLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    WelcomeScreen(onStartClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@Composable
fun PreviewWelcomeDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    WelcomeScreen(onStartClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewStartPointLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    StartPointScreen(onDemoClick = {}, onEmptyClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@Composable
fun PreviewStartPointDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    StartPointScreen(onDemoClick = {}, onEmptyClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewHomeEmptyLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    HomeScreen(lightsOn = 0, totalLights = 0, onTurnOffAllClick = {}, onRoomsClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@Composable
fun PreviewHomeEmptyDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    HomeScreen(lightsOn = 0, totalLights = 0, onTurnOffAllClick = {}, onRoomsClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun PreviewStartOverLight() = FavoritesPreviewHost(ThemeMode.LIGHT) { padding, state, spec ->
    StartOverScreen(onCancelClick = {}, onConfirmClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}

@WearPreviewDevices
@Composable
fun PreviewStartOverDark() = FavoritesPreviewHost(ThemeMode.DARK) { padding, state, spec ->
    StartOverScreen(onCancelClick = {}, onConfirmClick = {}, contentPadding = padding, listState = state, transformationSpec = spec)
}
