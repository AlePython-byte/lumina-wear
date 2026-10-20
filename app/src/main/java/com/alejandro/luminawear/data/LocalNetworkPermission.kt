package com.alejandro.luminawear.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Android 17 (API 37) bloquea la red local (p. ej. 10.0.2.2 del emulador) sin este permiso de
 * tiempo de ejecución. Se usa el texto literal porque la constante no existe en APIs anteriores.
 */
const val LOCAL_NETWORK_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"

private const val LOCAL_NETWORK_PERMISSION_SDK = 37

/** true si hay que pedir el permiso antes de hablar con el hub. En versiones anteriores no existe. */
fun needsLocalNetworkPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= LOCAL_NETWORK_PERMISSION_SDK &&
        context.checkSelfPermission(LOCAL_NETWORK_PERMISSION) != PackageManager.PERMISSION_GRANTED
