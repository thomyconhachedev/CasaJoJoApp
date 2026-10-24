package com.example.casajojoapp.domain.model

// =========================================================
// USUARIOS Y ROLES del sistema (híbrido: local hoy, backend mañana).
// - INVITADO: sin cuenta. Pide como antes; el pedido viaja con usuarioId=null.
// - USUARIO: se registró/login. Su pedido viaja con usuarioId y autocompleta
//   nombre/teléfono desde su cuenta.
// - ADMIN: como USUARIO + acceso al panel de cocina (oculto para los demás).
// La migración al backend (JWT) solo requiere una nueva implementación de
// AuthRepository: la UI no cambia (ver docs/backend_login_paso_a_paso.md).
// =========================================================
enum class Rol {
    INVITADO,
    USUARIO,
    ADMIN
}

data class Usuario(
    val id: Long,
    val username: String,
    val rol: Rol,
    val nombre: String,
    val telefono: String
)

// Estado de sesión vigente. INVITADO = sesión efímera, nunca se persiste.
data class Sesion(
    val usuarioId: Long?,
    val username: String,
    val rol: Rol,
    val nombre: String,
    val telefono: String
) {
    companion object {
        val INVITADO = Sesion(usuarioId = null, username = "", rol = Rol.INVITADO, nombre = "", telefono = "")
    }
}
