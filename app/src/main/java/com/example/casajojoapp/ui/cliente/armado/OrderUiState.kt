package com.example.casajojoapp.ui.cliente.armado

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
    val horaRetiroSeleccionada: String = "13:00", // CAMBIO HORARIO: default dentro del rango 12:00–16:00
    // CAMBIO RESTRICCIONES: las notas ahora son POR PLATO (PlatoArmado.notas).
    // Este campo queda reservado para notas generales del pedido (futuro API).
    val notasCliente: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    // CAMBIO MVVM (sección 1.B): costo total acumulado del carrito
    val totalCarrito: Double
        get() = carritoPlatos.sumOf { it.precioTotal }
}
