package com.example.casajojoapp.ui.cliente.cuenta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.casajojoapp.data.local.AppDatabase
import com.example.casajojoapp.data.repository.ClienteRepository
import com.example.casajojoapp.data.repository.ClienteRepositoryImpl
import com.example.casajojoapp.domain.model.Cliente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// CAMBIO CUENTAS: gestiona el perfil local del cliente (Room, sin PIN).
// AndroidViewModel para obtener el Context y construir la BD sin DI.
class CuentaViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val repositorio: ClienteRepository =
        ClienteRepositoryImpl(AppDatabase.obtener(aplicacion).clienteDao())

    private val _uiState = MutableStateFlow(CuentaUiState())
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val cliente = repositorio.obtenerCliente()
            _uiState.value = _uiState.value.copy(
                perfilExiste = cliente != null,
                nombre = cliente?.nombre.orEmpty(),
                telefono = cliente?.telefono.orEmpty(),
                isLoading = false
            )
        }
    }

    fun crearCuenta(nombre: String, telefono: String) {
        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()
        if (nombreLimpio.isBlank() || telefonoLimpio.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Ingresa tu nombre y teléfono.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repositorio.guardar(nombreLimpio, telefonoLimpio)
                _uiState.value = _uiState.value.copy(
                    perfilExiste = true,
                    nombre = nombreLimpio,
                    telefono = telefonoLimpio,
                    isLoading = false,
                    mensajeExito = "Cuenta guardada. Ya puedes ordenar más rápido."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo guardar la cuenta. Inténtalo de nuevo."
                )
            }
        }
    }

    fun actualizarPerfil(nombre: String, telefono: String) {
        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()
        if (nombreLimpio.isBlank() || telefonoLimpio.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El nombre y el teléfono no pueden estar vacíos.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repositorio.actualizar(Cliente(nombre = nombreLimpio, telefono = telefonoLimpio))
                _uiState.value = _uiState.value.copy(
                    nombre = nombreLimpio,
                    telefono = telefonoLimpio,
                    isLoading = false,
                    mensajeExito = "Datos actualizados."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudieron actualizar los datos."
                )
            }
        }
    }

    fun eliminarCuenta() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repositorio.eliminar()
                _uiState.value = CuentaUiState(
                    isLoading = false,
                    cuentaEliminada = true,
                    mensajeExito = "Cuenta eliminada de este dispositivo."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo eliminar la cuenta."
                )
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(error = null, mensajeExito = null)
    }
}
