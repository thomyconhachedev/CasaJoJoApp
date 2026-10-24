package com.example.casajojoapp.ui.cliente.cuenta

// CAMBIO ROLES: estado de la pantalla de cuenta según la sesión vigente:
// - Sesión USUARIO/ADMIN: edita los datos de SU cuenta (Room usuarios).
// - Invitado: perfil local de siempre (Room clientes) + acceso al login.
data class CuentaUiState(
    val sesionIniciada: Boolean = false,
    val username: String = "",
    val rol: String = "",
    val perfilExiste: Boolean = false,
    val nombre: String = "",
    val telefono: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    // Errores inline por campo (IE 2.1.2): se calculan en el ViewModel
    // al guardar y el usuario los ve desaparecer al corregir.
    val errorNombre: String? = null,
    val errorTelefono: String? = null,
    val mensajeExito: String? = null
)
