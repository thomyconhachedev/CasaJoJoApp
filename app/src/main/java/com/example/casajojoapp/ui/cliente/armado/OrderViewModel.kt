package com.example.casajojoapp.ui.cliente.armado

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.casajojoapp.data.local.AppDatabase
import com.example.casajojoapp.data.modo.ModoDatos
import com.example.casajojoapp.data.modo.ModoDatosRepository
import com.example.casajojoapp.data.remote.ApiError
import com.example.casajojoapp.data.remote.ApiService
import com.example.casajojoapp.data.remote.NetworkModule
import com.example.casajojoapp.data.repository.ClienteRepository
import com.example.casajojoapp.data.repository.ClienteRepositoryImpl
import com.example.casajojoapp.data.repository.PedidoRepository
import com.example.casajojoapp.data.repository.PedidoRepositoryApi
import com.example.casajojoapp.data.repository.PedidoRepositoryLocal
import com.example.casajojoapp.data.repository.MenuRepository
import com.example.casajojoapp.data.repository.MenuRepositoryApi
import com.example.casajojoapp.data.repository.MenuRepositoryLocal
import com.example.casajojoapp.data.repository.deCategoria
import com.example.casajojoapp.data.repository.extras
import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.Pedido
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.domain.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =========================================================
// OrderViewModel (AndroidViewModel): necesita Application para construir
// ModoDatosRepository (SharedPreferences) y el repositorio de pedidos.
// - MODO DE DATOS (LOCAL/API) en runtime, persistido en SharedPreferences.
// - Cola de pedidos (admin) cargada desde el repositorio vigente.
// - enviarPedido()/actualizarEstadoPedido() delegan al repositorio.
// El menu de armado sigue hardcodeado en las pantallas (se cableara en la
// Fase 3); aqui solo viajan como Producto ya mapeados.
// =========================================================
class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    // MODO DE DATOS (LOCAL / API) + BASE_URL, persistidos en SharedPreferences.
    private val modoDatosRepository = ModoDatosRepository(application)
    val modoDatos: StateFlow<ModoDatos> = modoDatosRepository.modo
    val baseUrl: StateFlow<String> = modoDatosRepository.baseUrl

    // Cola en memoria (implementación LOCAL por defecto). NO se persiste:
    // se pierde al reiniciar la app.
    private val pedidoRepositoryLocal = PedidoRepositoryLocal()

    // Perfil local (Room) del cliente: aporta nombre y teléfono al pedido
    // cuando la pantalla de Cuenta ya los guardó.
    private val clienteRepository: ClienteRepository =
        ClienteRepositoryImpl(AppDatabase.obtener(application).clienteDao())

    // Cache del ApiService: solo se recrea cuando cambia la BASE_URL.
    private var apiCache: Pair<String, ApiService>? = null

    // Selector del repositorio de pedidos según el modo vigente.
    private fun pedidoRepository(): PedidoRepository = when (modoDatosRepository.modo.value) {
        ModoDatos.LOCAL -> pedidoRepositoryLocal
        ModoDatos.API -> {
            val url = modoDatosRepository.baseUrl.value
            val api = if (apiCache?.first == url) {
                apiCache!!.second
            } else {
                NetworkModule.crearApiService(url).also { apiCache = url to it }
            }
            PedidoRepositoryApi(api)
        }
    }

    // Selector del repositorio de MENÚ (reutiliza el mismo cache de ApiService).
    private fun menuRepository(): MenuRepository = when (modoDatosRepository.modo.value) {
        ModoDatos.LOCAL -> MenuRepositoryLocal()
        ModoDatos.API -> {
            val url = modoDatosRepository.baseUrl.value
            val api = if (apiCache?.first == url) {
                apiCache!!.second
            } else {
                NetworkModule.crearApiService(url).also { apiCache = url to it }
            }
            MenuRepositoryApi(api)
        }
    }

    init {
        // Carga inicial: menú (LOCAL o API), cola de pedidos y perfil local.
        cargarMenu()
        cargarPedidos()
        cargarDatosClienteGuardados()
    }

    // Autocompleta nombre y teléfono desde el perfil de Room si aún no hay
    // datos en el estado (el backend exige nombreCliente para crear el pedido).
    private fun cargarDatosClienteGuardados() {
        viewModelScope.launch {
            val perfil = clienteRepository.obtenerCliente() ?: return@launch
            val actual = _uiState.value
            _uiState.value = actual.copy(
                clienteNombre = actual.clienteNombre.ifBlank { perfil.nombre },
                clienteTelefono = actual.clienteTelefono.ifBlank { perfil.telefono }
            )
        }
    }

    // =========================================================
    // MENÚ: carga los productos del repositorio vigente y los reparte por
    // categoría en el estado. Se puede refrescar desde el panel Admin.
    // =========================================================
    fun cargarMenu() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingMenu = true,
                menuError = null,
                menuMensajeExito = null
            )
            try {
                val productos = menuRepository().listarProductos()
                _uiState.value = _uiState.value.copy(
                    isLoadingMenu = false,
                    proteinasDisponibles = productos.deCategoria(CategoriaItem.PROTEINA),
                    acompanamientosDisponibles = productos.deCategoria(CategoriaItem.ACOMPANAMIENTO),
                    ensaladasDisponibles = productos.deCategoria(CategoriaItem.ENSALADA),
                    extrasDisponibles = productos.extras(),
                    menuMensajeExito = "Menú refrescado con éxito (${productos.size} productos)."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMenu = false,
                    menuError = "No se pudo refrescar el menú: ${ApiError.mensaje(e)}"
                )
            }
        }
    }

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

    // Acompañamientos: se eligen EXACTAMENTE 2 (plato base $4.800).
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

    // Extras (postres/bebidas): opcionales, sin límite, con precio propio.
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

    // Guard de integridad: plato confirmable con proteína + ≥1 acompañamiento.
    fun platoCompleto(): Boolean {
        val plato = _uiState.value.platoEnArmadoActual
        return plato.proteina != null && plato.acompanamientos.isNotEmpty()
    }

    // Notas/restricciones por plato (se escriben en ResumenPlatoScreen).
    fun actualizarNotasPlato(nuevasNotas: String) {
        _uiState.value = _uiState.value.copy(
            platoEnArmadoActual = _uiState.value.platoEnArmadoActual.copy(notas = nuevasNotas)
        )
    }

    fun agregarPlatoAlCarrito() {
        if (!platoCompleto()) return
        val listaActual = _uiState.value.carritoPlatos.toMutableList()
        listaActual.add(_uiState.value.platoEnArmadoActual)
        _uiState.value = _uiState.value.copy(
            carritoPlatos = listaActual,
            platoEnArmadoActual = PlatoArmado(),
            isSuccess = false,
            pedidoConfirmado = null
        )
    }

    fun actualizarHoraRetiro(hora: String) {
        _uiState.value = _uiState.value.copy(horaRetiroSeleccionada = hora)
    }

    // Datos del cliente (perfil o invitado) que viajan con el pedido.
    fun actualizarDatosCliente(nombre: String, telefono: String) {
        _uiState.value = _uiState.value.copy(
            clienteNombre = nombre.trim(),
            clienteTelefono = telefono.trim()
        )
    }

    // =========================================================
    // ADMIN / ENVÍO: construye el Pedido de dominio y lo delega al
    // repositorio vigente (LOCAL en memoria / API en el backend).
    // El id lo asigna el repositorio LOCAL o el backend en modo API.
    // =========================================================
    fun enviarPedido() {
        val estado = _uiState.value
        if (estado.carritoPlatos.isEmpty()) return

        val notas = estado.carritoPlatos
            .mapNotNull { it.notas.ifBlank { null } }
            .joinToString("; ")
            .ifBlank { null }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // El backend exige nombreCliente: si el estado no lo trae, se toma
            // del perfil local (Room) para no bloquear el pedido.
            val perfil = clienteRepository.obtenerCliente()
            val nombre = estado.clienteNombre.trim().ifBlank { perfil?.nombre.orEmpty().trim() }
            val telefono = estado.clienteTelefono.trim().ifBlank { perfil?.telefono.orEmpty().trim() }
            if (nombre.isBlank() || telefono.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Faltan tus datos de contacto. Créalos en Mi Cuenta."
                )
                return@launch
            }

            val pedidoDominio = Pedido(
                id = 0L, // el repositorio LOCAL o el backend asigna el id real
                clienteNombre = nombre,
                clienteTelefono = telefono,
                horaRetiro = estado.horaRetiroSeleccionada,
                platos = estado.carritoPlatos,
                total = estado.carritoPlatos.sumOf { it.precioTotal },
                notasRestricciones = notas
            )

            try {
                val confirmado = pedidoRepository().crearPedido(pedidoDominio)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    pedidoConfirmado = confirmado,
                    carritoPlatos = emptyList()
                )
                cargarPedidos() // refresca la cola de admin desde el repositorio
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No se pudo enviar el pedido: ${ApiError.mensaje(e)}"
                )
            }
        }
    }

    // =========================================================
    // ADMIN: carga la cola desde el repositorio vigente (LOCAL o API).
    // =========================================================
    fun cargarPedidos() {
        viewModelScope.launch {
            try {
                val lista = pedidoRepository().listarPedidos()
                _uiState.value = _uiState.value.copy(pedidosEnviados = lista)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se pudo cargar la cola: ${ApiError.mensaje(e)}"
                )
            }
        }
    }

    // ADMIN: cambia el estado de un pedido (LOCAL en memoria / PATCH en API).
    fun actualizarEstadoPedido(id: Long, nuevoEstado: EstadoPedido) {
        viewModelScope.launch {
            try {
                pedidoRepository().cambiarEstado(id, nuevoEstado)
                cargarPedidos()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se pudo cambiar el estado: ${ApiError.mensaje(e)}"
                )
            }
        }
    }

    // Interruptor LOCAL / API del panel Admin. Al cambiar, recarga menú y cola.
    fun cambiarModoDatos(nuevo: ModoDatos) {
        modoDatosRepository.cambiarModo(nuevo)
        cargarMenu()
        cargarPedidos()
    }

    // URL del backend (panel Admin). Al aplicar en modo API, recarga la cola.
    fun cambiarBaseUrl(nueva: String) {
        modoDatosRepository.cambiarBaseUrl(nueva)
        if (modoDatosRepository.modo.value == ModoDatos.API) cargarPedidos()
    }
}
