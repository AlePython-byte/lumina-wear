package com.alejandro.luminawear.presentation

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alejandro.luminawear.BuildConfig
import com.alejandro.luminawear.presentation.state.AppPreferences
import com.alejandro.luminawear.presentation.state.HUB_URL_RESET
import com.alejandro.luminawear.presentation.state.HubUrlCheck
import com.alejandro.luminawear.presentation.state.checkHubUrl
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val locale = Locale.forLanguageTag(AppPreferences(newBase).language.tag)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    /**
     * Solo en depuración: el extra `hub_url` fija la dirección del hub sin recompilar
     * (`--es hub_url http://192.168.80.18:8787`) y `reset` vuelve a la de fábrica. Una dirección
     * inválida se ignora y solo se registra la categoría del rechazo.
     */
    private fun applyHubUrlExtra(prefs: AppPreferences) {
        val extra = intent.getStringExtra(EXTRA_HUB_URL) ?: return
        if (extra.trim().equals(HUB_URL_RESET, ignoreCase = true)) {
            prefs.resetHubUrl()
            Log.i(TAG, "dirección del hub restablecida")
            return
        }
        when (val check = checkHubUrl(extra)) {
            is HubUrlCheck.Valid -> {
                prefs.hubUrl = check.url
                Log.i(TAG, "dirección del hub fijada")
            }
            is HubUrlCheck.Rejected -> Log.w(TAG, "dirección del hub rechazada: ${check.reason.name.lowercase()}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = AppPreferences(this)
        if (BuildConfig.DEBUG && savedInstanceState == null) applyHubUrlExtra(prefs)
        // Se leen antes de dibujar para no mostrar un tema equivocado en el primer frame.
        val initialThemeMode = prefs.themeMode
        val language = prefs.language
        val initialFavorites = prefs.favorites
        val hubUrl = prefs.hubUrl
        val initialLights = prefs.lightsConfig
        setContent {
            WearApp(
                initialThemeMode = initialThemeMode,
                language = language,
                initialFavorites = initialFavorites,
                onFavoritesChange = { prefs.favorites = it },
                hubUrl = hubUrl,
                initialLights = initialLights,
                onLightsChange = { prefs.lightsConfig = it },
                onThemeModeChange = { prefs.themeMode = it },
                onLanguageChange = {
                    prefs.language = it
                    recreate()
                }
            )
        }
    }

    private companion object {
        const val TAG = "HubUrl"
        const val EXTRA_HUB_URL = "hub_url"
    }
}
