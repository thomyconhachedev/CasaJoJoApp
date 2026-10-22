package com.example.casajojoapp.ui.cliente.cuenta

// CAMBIO CUENTAS: estado de la pantalla de cuenta. Un solo perfil local.
data class CuentaUiState(
    val perfilExiste: Boolean = false,
    val nombre: String = "",
    val telefono: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val mensajeExito: String? = null,
    val cuentaEliminada: Boolean = false
)
