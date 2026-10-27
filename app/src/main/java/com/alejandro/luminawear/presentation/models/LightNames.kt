package com.alejandro.luminawear.presentation.models

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alejandro.luminawear.R

/** Nombre visible en el idioma actual: no se guarda, se deriva del tipo y del número. */
@Composable
fun lightName(light: Light): String =
    if (light.number > 1) stringResource(R.string.light_name_numbered, stringResource(light.nameRes), light.number)
    else stringResource(light.nameRes)

fun lightName(context: Context, light: Light): String =
    if (light.number > 1) context.getString(R.string.light_name_numbered, context.getString(light.nameRes), light.number)
    else context.getString(light.nameRes)
