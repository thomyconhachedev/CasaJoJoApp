package com.example.casajojoapp.data.repository

import androidx.core.content.edit
import android.content.Context
import com.example.casajojoapp.data.remote.ApiError
import com.example.casajojoapp.data.remote.ApiService
import com.example.casajojoapp.data.remote.NetworkModule
import com.example.casajojoapp.data.remote.TokenHolder
import com.example.casajojoapp.data.remote.dto.AuthResponseDto
import com.example.casajojoapp.data.remote.dto.LoginRequestDto
import com.example.casajojoapp.data.remote.dto.RegistroRequestDto
import com.example.casajojoapp.domain.model.Rol
import com.example.casajojoapp.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

// =========================================================
// Implementación REMOTA de AuthRepository contra el backend (JWT):
//   POST /auth/login | /auth/registro | y el token viaja en todas las
//   peticiones vía AuthInterceptor (TokenHolder).
// - El usuarioId de la sesión es el ID DEL BACKEND: por eso los pedidos
//   quedan asociados a la cuenta y "Mis Pedidos" puede filtrar por id.
// - La sesión se persiste en SharedPreferences (token + datos); al
//   arrancar se restaura sin necesidad de red.
// La sesión local (AuthRepositoryLocal) queda disponible en el
// ServicioLocator como alternativa de un cambio de una línea.
// =========================================================
class AuthRepositoryApi(
    private val urlProvider: () -> String,
    context: Context
) : AuthRepository {

    // Mismo archivo de prefs que el resto de la app.
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _sesion = MutableStateFlow(Sesion.INVITADO)
    override val sesion: StateFlow<Sesion> = _sesion.asStateFlow()

    // ApiService cacheado por BASE_URL (mismo patrón que OrderViewModel).
    private var cache: Pair<String, ApiService>? = null
    private fun api(): ApiService {
        val url = urlProvider()
        return if (cache?.first == url) cache!!.second
        else NetworkModule.crearApiService(url).also { cache = url to it }
    }

    override suspend fun iniciarSesion(username: String, password: String): ResultadoAuth {
        val usuario = username.trim().lowercase(Locale.ROOT)
        if (usuario.isBlank() || password.isBlank()) {
            return ResultadoAuth.Error("Ingresa tu usuario y contraseña.")
        }
        return try {
            guardarSesion(api().login(LoginRequestDto(usuario, password)))
            ResultadoAuth.Exito
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoAuth.Error(ApiError.mensaje(e))
        }
    }

    override suspend fun registrarse(
        username: String,
        password: String,
        nombre: String,
        telefono: String
    ): ResultadoAuth {
        val usuario = username.trim().lowercase(Locale.ROOT)
        if (usuario.length < 3) return ResultadoAuth.Error("El usuario debe tener al menos 3 caracteres.")
        if (password.length < 6) return ResultadoAuth.Error("La contraseña debe tener al menos 6 caracteres.")
        if (nombre.isBlank() || telefono.isBlank()) return ResultadoAuth.Error("Ingresa tu nombre y teléfono.")
        return try {
            guardarSesion(
                api().registro(RegistroRequestDto(usuario, password, nombre.trim(), telefono.trim()))
            )
            ResultadoAuth.Exito
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoAuth.Error(ApiError.mensaje(e))
        }
    }

    override fun continuarComoInvitado() {
        TokenHolder.token = null
        _sesion.value = Sesion.INVITADO
    }

    override fun cerrarSesion() {
        prefs.edit {
            remove(CLAVE_TOKEN)
            remove(CLAVE_ID)
            remove(CLAVE_USERNAME)
            remove(CLAVE_ROL)
            remove(CLAVE_NOMBRE)
            remove(CLAVE_TELEFONO)
        }
        TokenHolder.token = null
        _sesion.value = Sesion.INVITADO
    }

    // El backend aún no expone edición de perfil: se actualiza la sesión y
    // las prefs locales (los pedidos usan estos datos al enviar). Queda
    // pendiente un PUT /auth/me cuando exista en el backend.
    override suspend fun actualizarPerfil(nombre: String, telefono: String): ResultadoAuth {
        val actual = _sesion.value
        if (actual.usuarioId == null) return ResultadoAuth.Error("No hay sesión iniciada.")
        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()
        if (nombreLimpio.isBlank() || telefonoLimpio.isBlank()) {
            return ResultadoAuth.Error("El nombre y el teléfono no pueden estar vacíos.")
        }
        prefs.edit {
            putString(CLAVE_NOMBRE, nombreLimpio)
            putString(CLAVE_TELEFONO, telefonoLimpio)
        }
        _sesion.value = actual.copy(nombre = nombreLimpio, telefono = telefonoLimpio)
        return ResultadoAuth.Exito
    }

    override suspend fun restaurarSesion() {
        val token = prefs.getString(CLAVE_TOKEN, null) ?: return
        val username = prefs.getString(CLAVE_USERNAME, null) ?: return
        val rol = prefs.getString(CLAVE_ROL, null) ?: Rol.USUARIO.name
        TokenHolder.token = token
        _sesion.value = Sesion(
            usuarioId = prefs.getLong(CLAVE_ID, -1L).takeIf { it > 0 },
            username = username,
            rol = rolDe(rol),
            nombre = prefs.getString(CLAVE_NOMBRE, null).orEmpty(),
            telefono = prefs.getString(CLAVE_TELEFONO, null).orEmpty()
        )
    }

    // Guarda token + datos en prefs y en memoria (TokenHolder para el interceptor).
    private fun guardarSesion(r: AuthResponseDto) {
        TokenHolder.token = r.token
        prefs.edit {
            putString(CLAVE_TOKEN, r.token)
            putLong(CLAVE_ID, r.id)
            putString(CLAVE_USERNAME, r.username)
            putString(CLAVE_ROL, r.rol)
            putString(CLAVE_NOMBRE, r.nombre.orEmpty())
            putString(CLAVE_TELEFONO, r.telefono.orEmpty())
        }
        _sesion.value = Sesion(
            usuarioId = r.id,
            username = r.username,
            rol = rolDe(r.rol),
            nombre = r.nombre.orEmpty(),
            telefono = r.telefono.orEmpty()
        )
    }

    private fun rolDe(valor: String): Rol =
        Rol.entries.firstOrNull { it.name == valor.trim().uppercase(Locale.ROOT) && it != Rol.INVITADO }
            ?: Rol.USUARIO

    companion object {
        // Mismo archivo de prefs que el resto de la app.
        private const val PREFS = "casa_jojo_prefs"
        private const val CLAVE_TOKEN = "be_token"
        private const val CLAVE_ID = "be_id"
        private const val CLAVE_USERNAME = "be_username"
        private const val CLAVE_ROL = "be_rol"
        private const val CLAVE_NOMBRE = "be_nombre"
        private const val CLAVE_TELEFONO = "be_telefono"
    }
}
