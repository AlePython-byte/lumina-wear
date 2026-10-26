package com.alejandro.luminawear.data

import android.util.Log
import com.alejandro.luminawear.presentation.state.ASSISTANT_MAX_TEXT
import com.alejandro.luminawear.presentation.state.AssistantAction
import com.alejandro.luminawear.presentation.state.AssistantLight
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.net.UnknownServiceException
import javax.net.ssl.SSLException

sealed interface AssistantResult {
    /** Acciones tal como llegaron (sin validar: la app las valida siempre antes de mostrarlas). */
    data class Success(val reply: String, val actions: List<AssistantAction>) : AssistantResult
    data object NotConfigured : AssistantResult
    data object NetworkError : AssistantResult
    data object ServerError : AssistantResult
}

/**
 * Servidor intermedio del asistente: POST con {"text", "lang", "lights"} y cabecera X-App-Token.
 * URL y token vienen de BuildConfig (local.properties); nunca se registran en el log.
 */
class AssistantClient(private val url: String, private val token: String) {
    val isConfigured: Boolean get() = url.isNotBlank() && token.isNotBlank()

    /** Tope total de TOTAL_TIMEOUT_MS. Se cancela con la corrutina que la llama (botón Cancelar). */
    suspend fun ask(text: String, lang: String, lights: List<AssistantLight>): AssistantResult {
        if (!isConfigured) return AssistantResult.NotConfigured
        return try {
            withTimeout(TOTAL_TIMEOUT_MS) { post(requestBody(text, lang, lights)) }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Sin conexión · categoría=tope_total en $TOTAL_TIMEOUT_MS ms")
            AssistantResult.NetworkError
        }
    }

    private suspend fun post(body: String): AssistantResult = coroutineScope {
        val connection = try {
            URL(url).openConnection() as HttpURLConnection
        } catch (e: Exception) {
            Log.w(TAG, "URL del asistente inválida")
            return@coroutineScope AssistantResult.NotConfigured
        }
        // La lectura bloqueante no atiende la cancelación: al cancelar se corta la conexión.
        val abortOnCancel = launch {
            try {
                awaitCancellation()
            } finally {
                connection.disconnect()
            }
        }
        try {
            withContext(Dispatchers.IO) { exchange(connection, body) }
        } finally {
            abortOnCancel.cancel()
        }
    }

    private fun exchange(connection: HttpURLConnection, body: String): AssistantResult {
        val startedAt = System.currentTimeMillis()
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = TOTAL_TIMEOUT_MS.toInt()
            connection.readTimeout = TOTAL_TIMEOUT_MS.toInt()
            // Solo se contacta la URL configurada: no se siguen redirecciones.
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("X-App-Token", token)
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val elapsed = System.currentTimeMillis() - startedAt
            if (code != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "HTTP $code en $elapsed ms")
                return AssistantResult.ServerError
            }
            val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val text = StringBuilder()
                val buffer = CharArray(1024)
                while (text.length < MAX_RESPONSE_CHARS) {
                    val read = reader.read(buffer)
                    if (read < 0) break
                    text.append(buffer, 0, read)
                }
                text.toString()
            }
            val result = parseResponse(response)
            Log.i(TAG, "HTTP 200 en $elapsed ms · ${(result as? AssistantResult.Success)?.actions?.size ?: 0} acciones")
            result
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            // Sin red, timeout, conexión cortada al cancelar o HTTPS rechazado.
            // Solo categoría, clase y duración: el mensaje de la excepción puede traer el host.
            Log.w(TAG, "Sin conexión · categoría=${failureCategory(e)} clase=${e.javaClass.simpleName} en ${System.currentTimeMillis() - startedAt} ms")
            AssistantResult.NetworkError
        } catch (e: Exception) {
            Log.w(TAG, "Respuesta inválida · clase=${e.javaClass.simpleName} en ${System.currentTimeMillis() - startedAt} ms")
            AssistantResult.ServerError
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TAG = "Assistant"
        const val TOTAL_TIMEOUT_MS = 8_000L
        const val MAX_RESPONSE_CHARS = 16 * 1024
    }
}

/** Categoría legible de un fallo de red para el registro (nunca incluye URL ni mensajes). */
internal fun failureCategory(e: IOException): String = when (e) {
    is UnknownHostException -> "dns"
    is SocketTimeoutException -> "timeout"
    is ConnectException -> "conexion_rechazada_o_sin_ruta"
    is SSLException -> "tls"
    is UnknownServiceException -> "http_sin_cifrar_bloqueado"
    else -> "io"
}

/** Solo text, lang y lights (id, name, room, on, brightness). */
internal fun requestBody(text: String, lang: String, lights: List<AssistantLight>): String {
    val lightsJson = JSONArray()
    lights.forEach { light ->
        lightsJson.put(
            JSONObject()
                .put("id", light.id)
                .put("name", light.name)
                .put("room", light.room)
                .put("on", light.on)
                .put("brightness", light.brightness)
        )
    }
    return JSONObject()
        .put("text", text.trim().take(ASSISTANT_MAX_TEXT))
        .put("lang", lang)
        .put("lights", lightsJson)
        .toString()
}

/** {"reply", "actions": [...]}. Las acciones desconocidas o mal formadas se ignoran. */
internal fun parseResponse(body: String): AssistantResult = try {
    val json = JSONObject(body)
    val actionsJson = json.optJSONArray("actions") ?: JSONArray()
    val actions = (0 until actionsJson.length()).mapNotNull { index ->
        val action = actionsJson.optJSONObject(index) ?: return@mapNotNull null
        when (action.optString("type")) {
            "set_light" -> {
                val id = action.optString("id")
                val on = action.opt("on") as? Boolean
                val brightness = (action.opt("brightness") as? Number)?.toInt()
                if (id.isEmpty() || on == null) null else AssistantAction.SetLight(id, on, brightness)
            }
            "apply_scene" -> action.optString("id").takeIf { it.isNotEmpty() }?.let { AssistantAction.ApplyScene(it) }
            "turn_off_all" -> AssistantAction.TurnOffAll
            else -> null
        }
    }
    val reply = if (json.isNull("reply")) "" else json.optString("reply").trim().take(MAX_REPLY_CHARS)
    AssistantResult.Success(reply = reply, actions = actions)
} catch (e: JSONException) {
    AssistantResult.ServerError
}

private const val MAX_REPLY_CHARS = 120
