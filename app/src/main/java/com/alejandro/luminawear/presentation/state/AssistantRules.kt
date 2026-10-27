package com.alejandro.luminawear.presentation.state

import java.text.Normalizer
import java.util.Locale

/*
 * Reglas locales del asistente (funciones puras, sin Android). Funcionan sin internet y se prueban
 * antes que el servidor. Entienden español e inglés sin importar acentos ni mayúsculas.
 * Si no reconocen la frase devuelven null y la app consulta al servidor.
 */

private val OFF_WORDS = listOf(
    "apaga", "apagar", "apague", "apagame", "desactiva", "desactivar",
    "turn off", "switch off", "shut off", "off"
)
private val ON_WORDS = listOf(
    "enciende", "encender", "encienda", "prende", "prender", "prenda", "activa", "activar",
    "turn on", "switch on", "on"
)
private val ALL_WORDS = listOf(
    "todo", "todas", "todos", "toda la casa", "las luces", "luces",
    "all", "everything", "lights"
)
/** Si se nombra una habitación, "las luces" no significa toda la casa: decide el servidor. */
private val ROOM_WORDS = listOf(
    "sala", "salon", "living", "dormitorio", "cuarto", "habitacion", "recamara", "estudio", "oficina",
    "bedroom", "study", "office"
)

/** Sinónimos por escena (ya normalizados). */
private val SCENE_WORDS = mapOf(
    "relax" to listOf("descanso", "descansar", "relax", "relajar", "relajarme", "relajante", "relajacion", "relaxing", "rest"),
    "read" to listOf("lectura", "leer", "read", "reading"),
    "focus" to listOf("concentracion", "concentrarme", "concentrar", "enfoque", "enfocar", "focus"),
    "sleep" to listOf("buenas noches", "dormir", "sleep", "good night", "goodnight", "bedtime")
)

/** Nombres de cada tipo de luz en español e inglés (ya normalizados), además del nombre visible. */
private val LIGHT_TYPE_ALIASES = mapOf(
    "plafon" to listOf("plafon", "ceiling light"),
    "lampara" to listOf("lampara", "lamp"),
    "hub_lamp" to listOf("lampara", "lamp"),
    "tira" to listOf("tira led", "tira", "led strip", "strip"),
    "techo" to listOf("techo", "ceiling"),
    "mesa" to listOf("mesa de noche", "mesita", "nightstand", "bedside lamp"),
    "armario" to listOf("armario", "closet", "wardrobe"),
    "escritorio" to listOf("escritorio", "desk", "desk lamp"),
    "ambiente" to listOf("luz ambiente", "ambiente", "ambient light", "ambient")
)

/** Otros nombres de una luz según su tipo; con número ("Plafón 2") cada alias lleva el número. */
fun lightAliases(typeKey: String, number: Int): List<String> =
    LIGHT_TYPE_ALIASES[typeKey].orEmpty().map { if (number > 1) "$it $number" else it }

private val PERCENT_PATTERN = Regex("""(\d{1,3}) (%|por ciento|porciento|percent)(?: |$)""")
private val TO_NUMBER_PATTERN = Regex("""(?:^| )(?:al|a|to|at) (\d{1,3})(?: |$)""")

/** Minúsculas, sin acentos, sin puntuación (salvo %), espacios simples. */
fun normalizeAssistantText(text: String): String =
    Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("""\p{Mn}+"""), "")
        .replace("%", " % ")
        .replace(Regex("""[^a-z0-9% ]"""), " ")
        .replace(Regex(""" +"""), " ")
        .trim()

/**
 * Reconoce: apagar/encender todo, escenas por nombre o sinónimo, encender/apagar una o varias luces
 * por nombre y "al N %" para el brillo. Devuelve las acciones o null si no entiende la frase.
 */
fun parseLocalCommand(text: String, lights: List<AssistantLight>): List<AssistantAction>? {
    val normalized = normalizeAssistantText(text)
    if (normalized.isEmpty()) return null
    val padded = " $normalized "
    fun has(phrase: String) = padded.contains(" $phrase ")

    val off = OFF_WORDS.any(::has)
    val on = ON_WORDS.any(::has)
    val percent = findPercent(normalized)

    val lightIds = findLights(padded, lights)
    if (lightIds.isNotEmpty()) {
        if (off && on) return null
        return lightIds.map { id ->
            when {
                off -> AssistantAction.SetLight(id, on = false, brightness = null)
                percent != null -> AssistantAction.SetLight(id, on = percent > 0, brightness = percent)
                on -> AssistantAction.SetLight(id, on = true, brightness = null)
                else -> return null
            }
        }
    }

    if (ALL_WORDS.any(::has) && ROOM_WORDS.none(::has)) {
        if (off && !on) return listOf(AssistantAction.TurnOffAll)
        if (on && !off) return listOf(AssistantAction.TurnOnAll)
    }

    val scenes = SCENE_WORDS.filterValues { words -> words.any(::has) }.keys
    if (scenes.size == 1) return listOf(AssistantAction.ApplyScene(scenes.first()))

    return null
}

private fun findPercent(normalized: String): Int? {
    val match = PERCENT_PATTERN.find(normalized) ?: TO_NUMBER_PATTERN.find(normalized) ?: return null
    return match.groupValues[1].toIntOrNull()?.coerceIn(0, 100)
}

/** Luces nombradas, en orden de aparición. El nombre más largo gana ("ceiling light" antes que "ceiling"). */
private fun findLights(padded: String, lights: List<AssistantLight>): List<String> {
    val aliases = lights.flatMap { light ->
        (light.aliases + normalizeAssistantText(light.name))
            .filter { it.isNotEmpty() }
            .map { alias -> alias to light.id }
    }.sortedByDescending { it.first.length }

    var remaining = padded
    val found = mutableListOf<Pair<Int, String>>()
    aliases.forEach { (alias, id) ->
        val index = remaining.indexOf(" $alias ")
        if (index >= 0) {
            found += index to id
            // Se borra lo encontrado para que un nombre contenido en otro no vuelva a coincidir.
            remaining = remaining.replaceRange(index + 1, index + 1 + alias.length, " ".repeat(alias.length))
        }
    }
    return found.sortedBy { it.first }.map { it.second }.distinct()
}
