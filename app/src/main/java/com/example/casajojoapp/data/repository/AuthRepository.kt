package com.example.casajojoapp.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.casajojoapp.data.local.dao.UsuarioDao
import com.example.casajojoapp.data.local.entity.UsuarioEntity
import com.example.casajojoapp.data.local.mappers.aSesion
import com.example.casajojoapp.domain.model.Rol
import com.example.casajojoapp.domain.model.Sesion
import com.example.casajojoapp.util.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

// =========================================================
// AUTENTICACIÓN (interfaz híbrida):
// - Hoy: AuthRepositoryLocal (Room + SharedPreferences), sin backend.
// - Mañana: AuthApi contra Spring Boot (JWT). Agregar la implementación
//   remota aquí NO toca la UI: ver docs/backend_login_paso_a_paso.md.
// Roles: INVITADO (sin sesión, pedido con usuarioId=null), USUARIO y ADMIN.
// =========================================================
sealed class ResultadoAuth {
    data object Exito : ResultadoAuth()
    data class Error(val mensaje: String) : ResultadoAuth()
}

interface AuthRepository {
    /** Sesión vigente; Sesion.INVITADO = nadie ha iniciado sesión. */
    val sesion: StateFlow<Sesion>

    suspend fun iniciarSesion(username: String, password: String): ResultadoAuth
    suspend fun registrarse(
        username: String,
        password: String,
        nombre: String,
        telefono: String
    ): ResultadoAuth

    /** Sesión efímera de invitado: no persiste ni toca la base. */
    fun continuarComoInvitado()

    fun cerrarSesion()

    /** Actualiza nombre/teléfono del usuario de la sesión actual. */
    suspend fun actualizarPerfil(nombre: String, telefono: String): ResultadoAuth

    /** Restaura la sesión persistida al arrancar (prefs o backend). */
    suspend fun restaurarSesion()
}

// Implementación local. La sesión se guarda solo como username en prefs
// (NUNCA la contraseña); al arrancar se reconstruye desde Room.
class AuthRepositoryLocal(
    private val dao: UsuarioDao,
    context: Context
) : AuthRepository {

    private val prefs = context.getSharedPreferences("casa_jojo_prefs", Context.MODE_PRIVATE)

    private val _sesion = MutableStateFlow(Sesion.INVITADO)
    override val sesion: StateFlow<Sesion> = _sesion.asStateFlow()

    // Restaura la sesión persistida (si el usuario sigue en la base).
    override suspend fun restaurarSesion() {
        val username = prefs.getString(CLAVE_SESION, null) ?: return
        val entidad = dao.buscarPorUsername(username)
        if (entidad == null) {
            prefs.edit { remove(CLAVE_SESION) }
        } else {
            _sesion.value = entidad.aSesion()
        }
    }

    // Siembra la cuenta admin la primera vez que se usa el login
    // (password: casajojo2026 — cambiar antes de entregar al cliente).
    suspend fun asegurarAdminSembrado() {
        val existeAdmin = dao.buscarPorUsername(ADMIN_USERNAME) != null
        if (existeAdmin) return
        val hash = PasswordHasher.hash(ADMIN_PASSWORD_SEMILLA)
        dao.insertar(
            UsuarioEntity(
                username = ADMIN_USERNAME,
                passwordHash = hash.hashHex,
                salt = hash.saltHex,
                rol = Rol.ADMIN.name,
                nombre = "Administrador",
                telefono = ""
            )
        )
    }

    override suspend fun iniciarSesion(username: String, password: String): ResultadoAuth {
        val limpio = username.trim().lowercase(Locale.ROOT)
        if (limpio.isBlank() || password.isBlank()) {
            return ResultadoAuth.Error("Ingresa tu usuario y contraseña.")
        }
        val entidad = dao.buscarPorUsername(limpio)
            ?: return ResultadoAuth.Error("Usuario o contraseña incorrectos.")
        val ok = PasswordHasher.verificar(password, entidad.salt, entidad.passwordHash)
        if (!ok) return ResultadoAuth.Error("Usuario o contraseña incorrectos.")
        persistirSesion(limpio)
        _sesion.value = entidad.aSesion()
        return ResultadoAuth.Exito
    }

    override suspend fun registrarse(
        username: String,
        password: String,
        nombre: String,
        telefono: String
    ): ResultadoAuth {
        val user = username.trim().lowercase(Locale.ROOT)
        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()
        when {
            user.length < 3 ->
                return ResultadoAuth.Error("El usuario debe tener al menos 3 caracteres.")
            password.length < 6 ->
                return ResultadoAuth.Error("La contraseña debe tener al menos 6 caracteres.")
            nombreLimpio.isBlank() || telefonoLimpio.isBlank() ->
                return ResultadoAuth.Error("Ingresa tu nombre y teléfono.")
        }
        val hash = PasswordHasher.hash(password)
        val id = try {
            dao.insertar(
                UsuarioEntity(
                    username = user,
                    passwordHash = hash.hashHex,
                    salt = hash.saltHex,
                    // El registro NUNCA otorga admin: ese rol solo lo tiene
                    // la cuenta sembrada (o el backend en el futuro).
                    rol = Rol.USUARIO.name,
                    nombre = nombreLimpio,
                    telefono = telefonoLimpio
                )
            )
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            return ResultadoAuth.Error("Ese nombre de usuario ya existe.")
        }
        persistirSesion(user)
        _sesion.value = Sesion(
            usuarioId = id,
            username = user,
            rol = Rol.USUARIO,
            nombre = nombreLimpio,
            telefono = telefonoLimpio
        )
        return ResultadoAuth.Exito
    }

    override fun continuarComoInvitado() {
        _sesion.value = Sesion.INVITADO
    }

    override fun cerrarSesion() {
        prefs.edit { remove(CLAVE_SESION) }
        _sesion.value = Sesion.INVITADO
    }

    override suspend fun actualizarPerfil(nombre: String, telefono: String): ResultadoAuth {
        val actual = _sesion.value
        val id = actual.usuarioId ?: return ResultadoAuth.Error("No hay sesión iniciada.")
        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()
        if (nombreLimpio.isBlank() || telefonoLimpio.isBlank()) {
            return ResultadoAuth.Error("El nombre y el teléfono no pueden estar vacíos.")
        }
        dao.actualizarContacto(id, nombreLimpio, telefonoLimpio)
        _sesion.value = actual.copy(nombre = nombreLimpio, telefono = telefonoLimpio)
        return ResultadoAuth.Exito
    }

    private fun persistirSesion(username: String) {
        prefs.edit { putString(CLAVE_SESION, username) }
    }

    companion object {
        private const val CLAVE_SESION = "sesion_username"
        const val ADMIN_USERNAME = "admin"
        // Semilla MVP: cambiar antes de entregar al cliente real.
        const val ADMIN_PASSWORD_SEMILLA = "casajojo2026"
    }
}
