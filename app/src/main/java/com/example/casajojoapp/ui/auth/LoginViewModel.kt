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
            (authRepository as? AuthRepositoryLocal)?.asegurarAdminSembrado()
            (authRepository as? AuthRepositoryLocal)?.restaurarSesionPrevia()
        }
    }

    fun cambiarModo(nuevo: ModoAuth) {
        _uiState.value = _uiState.value.copy(modo = nuevo, error = null)
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
            _uiState.value = estado.copy(isLoading = true, error = null)
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
