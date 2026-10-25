package com.example.casajojoapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casajojoapp.data.ServicioLocator
import com.example.casajojoapp.data.repository.AuthRepository
import com.example.casajojoapp.data.repository.AuthRepositoryLocal
import com.example.casajojoapp.data.repository.ResultadoAuth
import com.example.casajojoapp.domain.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Modo de la pantalla: iniciar sesión o crear cuenta.
enum class ModoAuth { LOGIN, REGISTRO }

data class LoginUiState(
    val modo: ModoAuth = ModoAuth.LOGIN,
    val username: String = "",
    val password: String = "",
    val passwordConfirmacion: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    // true tras el primer intento de envío: habilita los errores inline
    // por campo, que el usuario ve corregirse en vivo al escribir.
    val enviado: Boolean = false,
    // Rol de la sesión recién iniciada; la UI navega cuando deja de ser null.
    val exitoRol: Rol? = null
)

// CAMBIO ROLES: login/registro local (PBKDF2) + "Continuar como invitado".
// Envia a AuthRepository (hoy local; futuro contra backend sin tocar UI).
class LoginViewModel : ViewModel() {

    private val authRepository: AuthRepository = ServicioLocator.authRepository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Siembra el admin la primera vez que se abre el login y restaura
        // la sesión previa (si la app se reinició con sesión iniciada).
        viewModelScope.launch {
            // Con login local siembra su admin; con auth de backend no hace
            // nada (la cuenta admin la siembra el propio backend).
            (authRepository as? AuthRepositoryLocal)?.asegurarAdminSembrado()
            authRepository.restaurarSesion()
        }
    }

    fun cambiarModo(nuevo: ModoAuth) {
        _uiState.value = _uiState.value.copy(
            modo = nuevo,
            error = null,
            enviado = false // al cambiar de formulario se reinician los inline
        )
    }

    fun actualizarUsername(valor: String) {
        _uiState.value = _uiState.value.copy(username = valor, error = null)
    }

    fun actualizarPassword(valor: String) {
        _uiState.value = _uiState.value.copy(password = valor, error = null)
    }

    fun actualizarPasswordConfirmacion(valor: String) {
        _uiState.value = _uiState.value.copy(passwordConfirmacion = valor, error = null)
    }

    fun actualizarNombre(valor: String) {
        _uiState.value = _uiState.value.copy(nombre = valor, error = null)
    }

    fun actualizarTelefono(valor: String) {
        _uiState.value = _uiState.value.copy(telefono = valor, error = null)
    }

    fun enviar() {
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.value = estado.copy(isLoading = true, error = null, enviado = true)

            // VALIDACIÓN CRÍTICA: las dos contraseñas deben coincidir.
            // (Antes se enviaba la confirmación sin compararla: si no
            // coincidían, la cuenta se creaba con el campo equivocado.)
            if (estado.modo == ModoAuth.REGISTRO && estado.password != estado.passwordConfirmacion) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Las contraseñas no coinciden."
                )
                return@launch
            }

            val resultado = when (estado.modo) {
                ModoAuth.LOGIN -> authRepository.iniciarSesion(estado.username, estado.password)
                ModoAuth.REGISTRO -> authRepository.registrarse(
                    estado.username,
                    estado.passwordConfirmacion,
                    estado.nombre,
                    estado.telefono
                )
            }
            when (resultado) {
                is ResultadoAuth.Exito -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exitoRol = authRepository.sesion.value.rol
                )
                is ResultadoAuth.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = resultado.mensaje
                )
            }
        }
    }

    // =========================================================
    // ERRORES INLINE POR CAMPO (IE 2.1.2): funciones puras evaluadas en la
    // UI a partir del estado — la validación vive aquí, no en el Composable.
    // Solo aplican después del primer intento (enviado=true) y el usuario
    // los ve desaparecer en vivo al corregir el campo.
    // =========================================================
    fun errorUsername(estado: LoginUiState): String? =
        if (!estado.enviado) null
        else if (estado.username.trim().length < 3) "Mínimo 3 caracteres"
        else null

    fun errorPassword(estado: LoginUiState): String? =
        if (!estado.enviado) null
        else if (estado.password.length < 6) "Mínimo 6 caracteres"
        else null

    fun errorConfirmacion(estado: LoginUiState): String? = when {
        !estado.enviado || estado.modo == ModoAuth.LOGIN -> null
        estado.passwordConfirmacion != estado.password -> "Las contraseñas no coinciden"
        estado.passwordConfirmacion.length < 6 -> "Mínimo 6 caracteres"
        else -> null
    }

    fun errorNombre(estado: LoginUiState): String? =
        if (estado.enviado && estado.modo == ModoAuth.REGISTRO && estado.nombre.isBlank())
            "Ingresa tu nombre"
        else null

    fun errorTelefono(estado: LoginUiState): String? {
        if (!estado.enviado || estado.modo != ModoAuth.REGISTRO) return null
        if (estado.telefono.isBlank()) return "Ingresa tu teléfono"
        val digitos = estado.telefono.count { it.isDigit() }
        return if (digitos < 8) "Mínimo 8 dígitos (+56 9 1234 5678)" else null
    }

    fun continuarComoInvitado() {
        authRepository.continuarComoInvitado()
        _uiState.value = _uiState.value.copy(exitoRol = Rol.INVITADO)
    }

    // Login correcto pero la pantalla exige rol ADMIN: se limpia el éxito
    // y se muestra el motivo.
    fun advertirNoEsAdmin() {
        _uiState.value = _uiState.value.copy(
            exitoRol = null,
            error = "Esa cuenta no es de administrador. Inicia sesión con una cuenta admin."
        )
    }

    fun limpiarExito() {
        _uiState.value = _uiState.value.copy(exitoRol = null)
    }
}
