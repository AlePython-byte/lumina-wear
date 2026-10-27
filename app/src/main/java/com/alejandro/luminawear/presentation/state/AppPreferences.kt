package com.alejandro.luminawear.presentation.state

import android.content.Context
import com.alejandro.luminawear.data.DEFAULT_HUB_URL
import com.alejandro.luminawear.presentation.theme.ThemeMode

enum class AppLanguage(val tag: String) { ES("es"), EN("en") }

/** Preferencias de usuario sobre SharedPreferences. Valor ausente o inválido: LIGHT y ES. */
class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = if (prefs.getString(KEY_THEME_MODE, null) == "dark") ThemeMode.DARK else ThemeMode.LIGHT
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, if (value == ThemeMode.DARK) "dark" else "light").apply()
        }

    var language: AppLanguage
        get() = if (prefs.getString(KEY_LANGUAGE, null) == AppLanguage.EN.tag) AppLanguage.EN else AppLanguage.ES
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value.tag).apply()
        }

    /** Ids separados por comas. Clave ausente: favoritos por defecto; presente y vacía: sin favoritos. */
    var favorites: String
        get() = prefs.getString(KEY_FAVORITES, null) ?: DEFAULT_FAVORITES
        set(value) {
            prefs.edit().putString(KEY_FAVORITES, value).apply()
        }

    /** URL del hub local. Sin UI; ausente → emulador (10.0.2.2:8787). Se fija con `HubUrlOverride` en depuración. */
    var hubUrl: String
        get() = prefs.getString(KEY_HUB_URL, null) ?: DEFAULT_HUB_URL
        set(value) {
            prefs.edit().putString(KEY_HUB_URL, value).apply()
        }

    /** Borra la dirección fijada: vuelve a la de fábrica. */
    fun resetHubUrl() {
        prefs.edit().remove(KEY_HUB_URL).apply()
    }

    private companion object {
        const val FILE_NAME = "luminawear_prefs"
        const val KEY_HUB_URL = "hub_url"
        const val KEY_FAVORITES = "favorites"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_LANGUAGE = "language"
    }
}
