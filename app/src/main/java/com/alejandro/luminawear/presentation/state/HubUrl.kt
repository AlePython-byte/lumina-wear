package com.alejandro.luminawear.presentation.state

/** Valor del extra que devuelve la dirección del hub a la de fábrica. */
const val HUB_URL_RESET = "reset"

/** Por qué se rechazó una dirección. Es lo único que se registra: nunca la dirección. */
enum class HubUrlRejection { SCHEME, HOST, PORT, PATH, FORMAT }

sealed interface HubUrlCheck {
    data class Valid(val url: String) : HubUrlCheck
    data class Rejected(val reason: HubUrlRejection) : HubUrlCheck
}

private val OCTET = Regex("""\d{1,3}""")

/**
 * Valida una dirección de hub: `http://<IP privada>:<puerto>` sin ruta. IP literal de 10.x.x.x,
 * 172.16–31.x.x o 192.168.x.x (10.0.2.2 del emulador entra en 10.x). Devuelve la forma normalizada.
 */
fun checkHubUrl(raw: String): HubUrlCheck {
    val text = raw.trim()
    if (!text.startsWith("http://")) return HubUrlCheck.Rejected(HubUrlRejection.SCHEME)
    val rest = text.removePrefix("http://")
    val slash = rest.indexOf('/')
    val authority = if (slash < 0) rest else rest.substring(0, slash)
    val path = if (slash < 0) "" else rest.substring(slash)
    if (authority.any { it in "@?#" || it.isWhitespace() }) return HubUrlCheck.Rejected(HubUrlRejection.FORMAT)
    if (path.isNotEmpty() && path != "/") return HubUrlCheck.Rejected(HubUrlRejection.PATH)

    val colon = authority.indexOf(':')
    if (colon < 0) return HubUrlCheck.Rejected(HubUrlRejection.PORT)
    val hostText = authority.substring(0, colon)
    val portText = authority.substring(colon + 1)

    val parts = hostText.split('.')
    if (parts.size != 4 || parts.any { !OCTET.matches(it) }) return HubUrlCheck.Rejected(HubUrlRejection.HOST)
    val octets = parts.map { it.toInt() }
    if (octets.any { it > 255 } || !isPrivateIpv4(octets)) return HubUrlCheck.Rejected(HubUrlRejection.HOST)

    val port = portText.takeIf { it.length in 1..5 && it.all(Char::isDigit) }?.toInt()
    if (port == null || port !in 1..65535) return HubUrlCheck.Rejected(HubUrlRejection.PORT)

    return HubUrlCheck.Valid("http://${octets.joinToString(".")}:$port")
}

private fun isPrivateIpv4(o: List<Int>): Boolean =
    o[0] == 10 || (o[0] == 172 && o[1] in 16..31) || (o[0] == 192 && o[1] == 168)
