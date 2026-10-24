package com.example.casajojoapp.ui.cliente.cuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casajojoapp.data.ServicioLocator
import com.example.casajojoapp.data.repository.AuthRepository
import com.example.casajojoapp.data.repository.ClienteRepository
import com.example.casajojoapp.data.repository.ResultadoAuth
import com.example.casajojoapp.domain.model.Cliente
import com.example.casajojoapp.domain.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// CAMBIO ROLES: gestiona el perfil según la sesión:
// - USUARIO/ADMIN -> actualiza su registro en la tabla usuarios.
// - INVITADO -> perfil local de siempre (tabla clientes).
class CuentaViewModel : ViewModel() {

    private val authRepository: AuthRepository = ServicioLocator.authRepository
    private val clienteRepository: ClienteRepository = ServicioLocator.clienteRepository

    private val _uiState = MutableStateFlow(CuentaUiState())
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val sesion = authRepository.sesion.value
            if (sesion.rol != Rol.INVITADO) {
                _uiState.value = CuentaUiState(
                    sesionIniciada = true,
                    username = sesion.username,
                    rol = sesion.rol.name,
                    perfilExiste = true,
                    nombre = sesion.nombre,
                    telefono = sesion.telefono,
                    isLoading = false
                )
            } else {
                val cliente = clienteRepository.obtenerCliente()
                _uiState.value = CuentaUiState(
                    sesionIniciada = false,
                    perfilExiste = cliente != null,
                    nombre = cliente?.nombre.orEmpty(),
                    telefono = cliente?.telefono.orEmpty(),
                    isLoading = false
                )
            }
        }
    }

    fun guardar(nombre: String, telefono: String) {
        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()
        if (nombreLimpio.isBlank() || telefonoLimpio.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El nombre y el teléfono no pueden estar vacíos.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, mensajeExito = null)
            if (_uiState.value.sesionIniciada) {
                when (val resultado = authRepository.actualizarPerfil(nombreLimpio, telefonoLimpio)) {
                    is ResultadoAuth.Exito -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombre = nombreLimpio,
                        telefono = telefonoLimpio,
                        mensajeExito = "Datos actualizados."
                    )
                    is ResultadoAuth.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = resultado.mensaje
                    )
                }
            } else {
                try {
                    // Perfil de invitado: crear si no existe, actualizar si ya.
                    if (_uiState.value.perfilExiste) {
                        clienteRepository.actualizar(Cliente(nombre = nombreLimpio, telefono = telefonoLimpio))
                    } else {
                        clienteRepository.guardar(nombreLimpio, telefonoLimpio)
                    }
                    _uiState.value = _uiState.value.copy(
                        perfilExiste = true,
                        isLoading = false,
                        nombre = nombreLimpio,
                        telefono = telefonoLimpio,
                        mensajeExito = "Datos guardados en este dispositivo."
                    )
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudieron guardar los datos. Inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        cargarPerfil()
    }
}
