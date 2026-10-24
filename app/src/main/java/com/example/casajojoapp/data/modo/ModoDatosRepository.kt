package com.example.casajojoapp.data.modo

import android.content.Context
import androidx.core.content.edit
import com.example.casajojoapp.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// =========================================================
// Repositorio del MODO DE DATOS (LOCAL / API) y la BASE_URL.
// Persiste ambos valores en SharedPreferences y los expone como StateFlow
// para que la UI y los repositorios reaccionen al cambio en runtime.
// El interruptor vive en el panel Admin (PantallaAdmin).
// =========================================================
class ModoDatosRepository(context: Context) {

    private val prefs =
        context.getSharedPreferences("casa_jojo_prefs", Context.MODE_PRIVATE)

    private val _modo = MutableStateFlow(cargarModo())
    val modo: StateFlow<ModoDatos> = _modo.asStateFlow()

    private val _baseUrl = MutableStateFlow(cargarBaseUrl())
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    fun cambiarModo(nuevo: ModoDatos) {
        _modo.value = nuevo
        prefs.edit { putString(CLAVE_MODO, nuevo.name) }
    }

    // CAMBIO URL: valida el esquema antes de persistir. Devuelve false si la
    // URL es inválida (no http/https) y conserva la anterior.
    fun cambiarBaseUrl(nueva: String): Boolean {
        val candidata = nueva.trim().ifBlank { BuildConfig.BASE_URL }
        val normalizada = normalizar(candidata)
        val esValida = normalizada.startsWith("http://") || normalizada.startsWith("https://")
        if (!esValida) return false
        _baseUrl.value = normalizada
        prefs.edit { putString(CLAVE_BASE_URL, normalizada) }
        return true
    }

    private fun cargarModo(): ModoDatos {
        val guardado = prefs.getString(CLAVE_MODO, null)
        return ModoDatos.entries.firstOrNull { it.name == guardado } ?: ModoDatos.LOCAL
    }

    private fun cargarBaseUrl(): String {
        val guardada = prefs.getString(CLAVE_BASE_URL, null) ?: BuildConfig.BASE_URL
        return normalizar(guardada)
    }

    // Retrofit exige que la baseUrl termine en "/".
    private fun normalizar(url: String): String =
        if (url.endsWith("/")) url else "$url/"

    companion object {
        private const val CLAVE_MODO = "modo_datos"
        private const val CLAVE_BASE_URL = "base_url_api"
    }
}
