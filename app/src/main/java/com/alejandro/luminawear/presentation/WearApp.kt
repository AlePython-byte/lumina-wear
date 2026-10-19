package com.alejandro.luminawear.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.home.HomeScreen
import com.alejandro.luminawear.presentation.lights.LightControlScreen
import com.alejandro.luminawear.presentation.models.appLights
import com.alejandro.luminawear.presentation.models.appRooms
import com.alejandro.luminawear.presentation.rooms.RoomDetailScreen
import com.alejandro.luminawear.presentation.rooms.RoomsScreen
import com.alejandro.luminawear.presentation.settings.SettingsScreen
import com.alejandro.luminawear.presentation.state.AppLanguage
import com.alejandro.luminawear.presentation.state.rememberLightStateHolder
import com.alejandro.luminawear.presentation.state.LightState
import com.alejandro.luminawear.presentation.state.LightStateHolder
import com.alejandro.luminawear.presentation.theme.LuminaTimeText
import com.alejandro.luminawear.presentation.theme.LuminaWearTheme
import com.alejandro.luminawear.presentation.theme.ThemeMode

enum class ScreenType { HOME, ROOMS, ROOM_DETAIL, LIGHT_CONTROL, SETTINGS }

@Composable
fun WearApp(
    initialThemeMode: ThemeMode = ThemeMode.LIGHT,
    language: AppLanguage = AppLanguage.ES,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit = {}
) {
    val lightStateHolder = rememberLightStateHolder()

    val totalLights = appLights.size
    val totalLightsOn = lightStateHolder.states.count { it.value.isOn }

    var currentScreen by rememberSaveable { mutableStateOf(ScreenType.HOME.name) }
    var selectedRoomId by rememberSaveable { mutableStateOf("") }
    var selectedLightId by rememberSaveable { mutableStateOf("") }
    // Único dueño del tema; MainActivity entrega el valor guardado y persiste los cambios.
    var themeMode by rememberSaveable { mutableStateOf(initialThemeMode) }

    BackHandler(currentScreen != ScreenType.HOME.name) {
        when (currentScreen) {
            ScreenType.LIGHT_CONTROL.name -> {
                val backLight = appLights.firstOrNull { it.id == selectedLightId }
                if (backLight != null) selectedRoomId = backLight.roomId
                currentScreen = ScreenType.ROOM_DETAIL.name
            }
            ScreenType.ROOM_DETAIL.name -> currentScreen = ScreenType.ROOMS.name
            ScreenType.ROOMS.name -> currentScreen = ScreenType.HOME.name
            ScreenType.SETTINGS.name -> currentScreen = ScreenType.HOME.name
        }
    }

    LuminaWearTheme(themeMode = themeMode) {
        AppScaffold(timeText = { LuminaTimeText() }) {
            val stateHolder = rememberSaveableStateHolder()
            val currentKey = when (currentScreen) {
                ScreenType.ROOM_DETAIL.name -> "${currentScreen}_$selectedRoomId"
                ScreenType.LIGHT_CONTROL.name -> "${currentScreen}_$selectedLightId"
                else -> currentScreen
            }

            stateHolder.SaveableStateProvider(currentKey) {
                val listState = rememberTransformingLazyColumnState()
                val transformationSpec = rememberTransformationSpec()
                ScreenScaffold(
                    scrollState = listState,
                ) { contentPadding ->
                    when (currentScreen) {
                        ScreenType.HOME.name -> {
                            HomeScreen(
                                lightsOn = totalLightsOn,
                                totalLights = totalLights,
                                onTurnOffAllClick = { lightStateHolder.turnOffAll() },
                                onRoomsClick = { currentScreen = ScreenType.ROOMS.name },
                                onSettingsClick = { currentScreen = ScreenType.SETTINGS.name },
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
                                    onBrightnessChange = { newBr -> lightStateHolder.setBrightness(light.id, newBr) },
                                    onTogglePower = { lightStateHolder.setLightOn(light.id, !state.isOn) },
                                    onBackClick = {
                                        selectedRoomId = light.roomId
                                        currentScreen = ScreenType.ROOM_DETAIL.name
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
