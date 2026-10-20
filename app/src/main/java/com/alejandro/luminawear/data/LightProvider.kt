package com.alejandro.luminawear.data

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Backend de una luz. LightStateHolder sigue siendo la fuente de verdad; el provider solo confirma. */
interface LightProvider {
    suspend fun setLight(id: String, on: Boolean, brightness: Int): Result<Unit>
}

/** Luces simuladas: no hace nada y siempre confirma. */
object DemoProvider : LightProvider {
    override suspend fun setLight(id: String, on: Boolean, brightness: Int): Result<Unit> = Result.success(Unit)
}

/** Hub HTTP local (hub/hub.py): POST {baseUrl}/api/lights/{id} con {"on", "brightness"}. Sin librerías. */
class HubProvider(private val baseUrl: String) : LightProvider {
    override suspend fun setLight(id: String, on: Boolean, brightness: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("${baseUrl.trimEnd('/')}/api/lights/${URLEncoder.encode(id, "UTF-8")}")
                val connection = url.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = TIMEOUT_MS
                    connection.readTimeout = TIMEOUT_MS
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json")
                    val body = JSONObject().put("on", on).put("brightness", brightness).toString()
                    connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                    val code = connection.responseCode
                    if (code in 200..299) {
                        Result.success(Unit)
                    } else {
                        Log.w(TAG, "Hub respondió HTTP $code para $id ($url)")
                        Result.failure(IOException("HTTP $code"))
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // IOException (sin red, timeout, URL inválida) o SecurityException: la orden no se confirmó.
                Log.w(TAG, "No se pudo enviar $id al hub $baseUrl", e)
                Result.failure(e)
            }
        }

    private companion object {
        const val TAG = "HubProvider"
        const val TIMEOUT_MS = 2_000
    }
}

const val DEFAULT_HUB_URL = "http://10.0.2.2:8787"

/** Única luz conectada al hub por ahora: Lámpara de Sala. */
const val HUB_LIGHT_ID = "living_lampara"

/** Lámpara de Sala → hub; las otras 7 luces → DemoProvider. */
class LightProviders(hubUrl: String) {
    private val hub = HubProvider(hubUrl)

    fun providerFor(lightId: String): LightProvider = if (lightId == HUB_LIGHT_ID) hub else DemoProvider

    /** true si la orden necesita confirmación remota (muestra Conectando / error). */
    fun isRemote(lightId: String): Boolean = providerFor(lightId) !is DemoProvider
}
