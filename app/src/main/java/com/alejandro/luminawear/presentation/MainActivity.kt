package com.alejandro.luminawear.presentation

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alejandro.luminawear.presentation.state.AppPreferences
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val locale = Locale.forLanguageTag(AppPreferences(newBase).language.tag)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = AppPreferences(this)
        // Se leen antes de dibujar para no mostrar un tema equivocado en el primer frame.
        val initialThemeMode = prefs.themeMode
        val language = prefs.language
        setContent {
            WearApp(
                initialThemeMode = initialThemeMode,
                language = language,
                onThemeModeChange = { prefs.themeMode = it },
                onLanguageChange = {
                    prefs.language = it
                    recreate()
                }
            )
        }
    }
}
