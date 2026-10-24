package com.example.casajojoapp.ui.cliente.armado

import com.example.casajojoapp.domain.model.Pedido
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.domain.model.Producto

// =========================================================
// CAMBIO MVVM (prompt sección 1.B):
// - Se MOVIO desde viewmodel/state/OrderUiState.kt a ui/cliente/armado
//   para que el estado de la UI conviva con el ViewModel y los composable.
// - Se ELIMINO el campo antiguo "platoEnArmado" y se reemplazo por
//   "platoEnArmadoActual" (ya era el nombre que usaba OrderViewModel).
// - Se AGREGO "carritoPlatos" (carrito local de platos armados).
// - Se AGREGO el getter "totalCarrito" con el costo acumulado del carrito.
// - Es data class para disponer de .copy() nativo de Kotlin (sin imports
//   de androidx.room.util.copy ni otras librerías).
// =========================================================
data class OrderUiState(
    val platoEnArmadoActual: PlatoArmado = PlatoArmado(),
    val carritoPlatos: List<PlatoArmado> = emptyList(),
    val proteinasDisponibles: List<Producto> = emptyList(),
    val acompanamientosDisponibles: List<Producto> = emptyList(),
    val ensaladasDisponibles: List<Producto> = emptyList(),
    val extrasDisponibles: List<Producto> = emptyList(),
    val horaRetiroSeleccionada: String = "13:00", // CAMBIO HORARIO: default dentro del rango (ver util/HorarioRetiro)
    // CAMBIO CUENTAS: datos del cliente que viajan con el pedido. Se
    // autocompletan desde la sesión o el perfil local (o se capturan como invitado).
    val clienteNombre: String = "",
    val clienteTelefono: String = "",
    // ADMIN: pedidos ya enviados que quedan en la cola de admin, ordenados
    // por hora de retiro en PantallaAdmin.
    val pedidosEnviados: List<Pedido> = emptyList(),
    // CAMBIO API: estado de la carga del menú (LOCAL o API). El menú se
    // carga desde el repositorio vigente; se puede refrescar desde el panel
    // Admin para ver cambios hechos en el backend.
    val isLoadingMenu: Boolean = false,
    val menuError: String? = null,
    // Backend inaccesible en modo API: el menú/cola se sirven desde la fuente
    // local de respaldo y esta advertencia se muestra en el panel admin.
    val menuAdvertencia: String? = null,
    // ADMIN: mensaje de éxito tras "Refrescar menú" (se limpia en la próxima carga).
    val menuMensajeExito: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    // Último pedido confirmado por el repositorio (trae id y código de
    // seguimiento del backend en modo API). Se limpia al armar otro plato.
    val pedidoConfirmado: Pedido? = null,
    val errorMessage: String? = null
) {
    // CAMBIO MVVM (sección 1.B): costo total acumulado del carrito
    val totalCarrito: Double
        get() = carritoPlatos.sumOf { it.precioTotal }
}
