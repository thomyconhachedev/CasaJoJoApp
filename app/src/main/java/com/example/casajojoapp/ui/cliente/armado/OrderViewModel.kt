package com.example.casajojoapp.ui.cliente.armado

import androidx.lifecycle.ViewModel
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.domain.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// =========================================================
// CAMBIO MVVM (prompt sección 2):
// - Se MOVIO desde viewmodel/OrderViewModel.kt a ui/cliente/armado.
// - Estado expuesto como StateFlow inmutable a partir de un
//   MutableStateFlow encapsulado (_uiState).
// - Todas las mutaciones pasan por _uiState.value = ...copy(...).
// =========================================================
class OrderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    fun seleccionarProteina(proteina: Producto) {
        _uiState.value = _uiState.value.copy(
            platoEnArmadoActual = _uiState.value.platoEnArmadoActual.copy(proteina = proteina)
        )
    }

    fun seleccionarEnsalada(ensalada: Producto?) {
        _uiState.value = _uiState.value.copy(
            platoEnArmadoActual = _uiState.value.platoEnArmadoActual.copy(ensalada = ensalada)
        )
    }

    // =========================================================
    // CAMBIO FLUJO (5 pasos): los acompañamientos son OBLIGATORIOS y
    // se eligen EXACTAMENTE 2 (1 proteína + 2 acompañamientos + ensalada
    // componen el plato base de $4.800).
    // - Si ya hay 2 seleccionados, los nuevos quedan bloqueados en la UI
    //   (patrón de lista con límite igual al de SeleccionAcompanamientosSC).
    // - También se expone alternarAcompanamiento para poder deseleccionar.
    // =========================================================
    fun alternarAcompanamiento(producto: Producto) {
        val actual = _uiState.value.platoEnArmadoActual
        val MAX_ACOMPANAMIENTOS = 2
        val seleccionado = actual.acompanamientos.any { it.id == producto.id }
        val nuevaLista = when {
            seleccionado -> actual.acompanamientos.filterNot { it.id == producto.id }
            actual.acompanamientos.size < MAX_ACOMPANAMIENTOS -> actual.acompanamientos + producto
            else -> actual.acompanamientos // límite alcanzado: no se agrega
        }
        _uiState.value = _uiState.value.copy(
            platoEnArmadoActual = actual.copy(acompanamientos = nuevaLista)
        )
    }

    // =========================================================
    // POSTRES Y BEBIDAS (paso 4/5): son extras OPCIONALES y NO tienen
    // límite de selección; cada uno suma su precio al plato base de $4.800.
    // agregarExtra es idempotente por id (evita duplicados al recomponer).
    // =========================================================
    fun agregarExtra(producto: Producto) {
        val actual = _uiState.value.platoEnArmadoActual
        if (actual.extras.any { it.id == producto.id }) return
        _uiState.value = _uiState.value.copy(
            platoEnArmadoActual = actual.copy(extras = actual.extras + producto)
        )
    }

    fun quitarExtra(producto: Producto) {
        _uiState.value = _uiState.value.copy(
            platoEnArmadoActual = _uiState.value.platoEnArmadoActual.copy(
                extras = _uiState.value.platoEnArmadoActual.extras
                    .filterNot { it.id == producto.id }
            )
        )
    }

    // =========================================================
    // CAMBIO FLUJO: guard de integridad del plato. Un plato solo es
    // confirmable si tiene 1 proteína y AL MENOS 1 acompañamiento
    // (hasta 2; próximamente habrá pedidos especiales con otras reglas).
    // =========================================================
    fun platoCompleto(): Boolean {
        val plato = _uiState.value.platoEnArmadoActual
        return plato.proteina != null && plato.acompanamientos.isNotEmpty()
    }

    // =========================================================
    // CAMBIO RESTRICCIONES: las notas ahora son POR PLATO y se escriben
    // en el paso final de armado (ResumenPlatoScreen), no en el carrito.
    // Se guardan en PlatoArmado.notas y viajan con el plato al carrito.
    // =========================================================
    fun actualizarNotasPlato(nuevasNotas: String) {
        _uiState.value = _uiState.value.copy(
            platoEnArmadoActual = _uiState.value.platoEnArmadoActual.copy(notas = nuevasNotas)
        )
    }

    fun agregarPlatoAlCarrito() {
        if (!platoCompleto()) return // CAMBIO FLUJO: no se agregan platos inválidos
        val listaActual = _uiState.value.carritoPlatos.toMutableList()
        listaActual.add(_uiState.value.platoEnArmadoActual)
        _uiState.value = _uiState.value.copy(
            carritoPlatos = listaActual,
            platoEnArmadoActual = PlatoArmado() // Reinicia el plato actual para armar otro plato si se desea
        )
    }

    fun actualizarHoraRetiro(hora: String) {
        // CAMBIO HORARIO: ahora sí se usa desde SeleccionarHoraRetiroSC
        _uiState.value = _uiState.value.copy(horaRetiroSeleccionada = hora)
    }

    fun enviarPedido() {
        // Preparado para llamada asíncrona mediante viewModelScope.launch con Retrofit
        //
        // TODO(Sprint 4): POST /api/pedidos
        //   val request = CrearPedidoRequestDto(
        //       clienteNombre = ...,
        //       clienteTelefono = ...,
        //       horaRetiro = _uiState.value.horaRetiroSeleccionada,
        //       notasRestricciones = _uiState.value.carritoPlatos
        //           .mapNotNull { plato.notas.ifBlank { null } } // CAMBIO RESTRICCIONES: son por plato
        //       detalles = _uiState.value.carritoPlatos.flatMap { plato ->
        //           listOfNotNull(plato.proteina) + plato.acompanamientos +
        //               listOfNotNull(plato.ensalada) + plato.extras
        //       }.map { DetallePedidoRequestDto(it.id, 1, it.precio) }
        //   )
        //   viewModelScope.launch {
        //       _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        //       try {
        //           api.crearPedido(request)
        //           _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        //       } catch (e: Exception) {
        //           _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        //       }
        //   }
    }
}
