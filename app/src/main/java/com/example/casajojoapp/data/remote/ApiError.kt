package com.example.casajojoapp.data.remote

import com.google.gson.Gson
import retrofit2.HttpException

// =========================================================
// Traduce las excepciones de red a un mensaje legible para la UI.
// El backend responde errores como {"error": "mensaje"} (GlobalExceptionHandler),
// así que en un 4xx/5xx se extrae ese texto en vez de mostrar "HTTP 404".
// =========================================================
object ApiError {

    private val gson = Gson()

    fun mensaje(e: Throwable): String = when (e) {
        is HttpException -> mensajeDeBackend(e) ?: "Error ${e.code()} del servidor. Intenta de nuevo."
        is java.net.UnknownHostException -> "Sin conexión con el servidor. Revisa la URL o tu internet."
        is java.net.SocketTimeoutException -> "El servidor tardó demasiado en responder."
        // URL mal formada al crear el Retrofit (p. ej. "ftp://..." o "foo").
        is IllegalArgumentException -> "URL del backend inválida (debe empezar con http:// o https://)."
        else -> e.message ?: "Error inesperado. Intenta de nuevo."
    }

    private fun mensajeDeBackend(e: HttpException): String? = try {
        val cuerpo = e.response()?.errorBody()?.string() ?: return null
        gson.fromJson(cuerpo, Map::class.java)?.get("error") as? String
    } catch (_: Exception) {
        null
    }
}
