package com.example.casajojoapp.ui.cliente.armado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casajojoapp.data.ServicioLocator
import com.example.casajojoapp.data.modo.ModoDatos
import com.example.casajojoapp.data.modo.ModoDatosRepository
import com.example.casajojoapp.data.remote.ApiError
import com.example.casajojoapp.data.remote.ApiService
import com.example.casajojoapp.data.remote.NetworkModule
import com.example.casajojoapp.data.repository.AuthRepository
import com.example.casajojoapp.data.repository.ClienteRepository
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
import com.example.casajojoapp.domain.model.Rol
import com.example.casajojoapp.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =========================================================
// OrderViewModel: estado global del pedido (armado + carrito + cola admin).
// - Las dependencias vienen de ServicioLocator (instancias únicas: la
//   sesión y el modo de datos se ven en TODAS las pantallas).
// - MODO DE DATOS (LOCAL/API) en runtime, persistido en SharedPreferences.
// - enviarPedido()/actualizarEstadoPedido() delegan al repositorio vigente.
// =========================================================
class OrderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    // MODO DE DATOS (LOCAL / API) + BASE_URL, persistidos en SharedPreferences.
    private val modoDatosRepository: ModoDatosRepository = ServicioLocator.modoDatos
    val modoDatos: StateFlow<ModoDatos> = modoDatosRepository.modo
    val baseUrl: StateFlow<String> = modoDatosRepository.baseUrl

    // CAMBIO ROLES: sesión vigente compartida (INVITADO/USUARIO/ADMIN).
    val sesion: StateFlow<Sesion> = ServicioLocator.authRepository.sesion

    private val pedidoRepositoryLocal: PedidoRepositoryLocal = ServicioLocator.pedidoRepositoryLocal
    private val clienteRepository: ClienteRepository = ServicioLocator.clienteRepository
    private val authRepository: AuthRepository = ServicioLocator.authRepository

    // Cache del ApiService: solo se recrea cuando cambia la BASE_URL.
    private var apiCache: Pair<String, ApiService>? = null

    // Selector del repositorio de pedidos según el modo vigente.
    private fun pedidoRepository(): PedidoRepository = when (modoDatosRepository.modo.value) {
        ModoDatos.LOCAL -> pedidoRepositoryLocal
        ModoDatos.API -> PedidoRepositoryApi(apiServiceVigente())
    }

    // Selector del repositorio de MENÚ (reutiliza el mismo cache de ApiService).
    private fun menuRepository(): MenuRepository = when (modoDatosRepository.modo.value) {
        ModoDatos.LOCAL -> MenuRepositoryLocal()
        ModoDatos.API -> MenuRepositoryApi(apiServiceVigente())
    }

    private fun apiServiceVigente(): ApiService {
        val url = modoDatosRepository.baseUrl.value
        val cacheado = apiCache?.first == url
        return if (cacheado) {
            apiCache!!.second
        } else {
            NetworkModule.crearApiService(url).also { apiCache = url to it }
        }
    }

    init {
        // Restaura la sesión persistida (la construye AuthRepositoryLocal) y
        // carga menú + cola + datos de contacto del perfil vigente.
        viewModelScope.launch {
            (authRepository as? com.example.casajojoapp.data.repository.AuthRepositoryLocal)
                ?.restaurarSesionPrevia()
            cargarDatosClienteGuardados()
        }
        cargarMenu()
        cargarPedidos()
    }

    // Autocompleta nombre y teléfono desde la sesión (USUARIO/ADMIN) o el
    // perfil local de invitado si aún no hay datos en el estado.
    private fun cargarDatosClienteGuardados() {
        viewModelScope.launch {
            val sesionActual = authRepository.sesion.value
            val perfil = if (sesionActual.rol == Rol.INVITADO) {
                clienteRepository.obtenerCliente()
            } else null
            val nombreSugerido = sesionActual.nombre.ifBlank { perfil?.nombre.orEmpty() }
            val telefonoSugerido = sesionActual.telefono.ifBlank { perfil?.telefono.orEmpty() }
            if (nombreSugerido.isBlank() && telefonoSugerido.isBlank()) return@launch
            val actual = _uiState.value
            _uiState.value = actual.copy(
                clienteNombre = actual.clienteNombre.ifBlank { nombreSugerido },
                clienteTelefono = actual.clienteTelefono.ifBlank { telefonoSugerido }
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
            } catch (e: CancellationException) {
                throw e
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

    // Datos del cliente (sesión, perfil o invitado) que viajan con el pedido.
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

            // CAMBIO ROLES: orden de preferencia de contacto:
            // 1) lo que el usuario escribió en el estado,
            // 2) la sesión (USUARIO/ADMIN), 3) el perfil de invitado.
            val sesionActual = authRepository.sesion.value
            val perfil = clienteRepository.obtenerCliente()
            val nombre = estado.clienteNombre.trim()
                .ifBlank { sesionActual.nombre.trim() }
                .ifBlank { perfil?.nombre.orEmpty().trim() }
            val telefono = estado.clienteTelefono.trim()
                .ifBlank { sesionActual.telefono.trim() }
                .ifBlank { perfil?.telefono.orEmpty().trim() }
            if (nombre.isBlank() || telefono.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Faltan tus datos de contacto. Créalos en Mi Cuenta o inicia sesión."
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
                notasRestricciones = notas,
                // CAMBIO ROLES: los pedidos de cuentas quedan asociadas;
                // los de invitados viajan sin usuarioId (como hasta ahora).
                usuarioId = sesionActual.usuarioId
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
            } catch (e: CancellationException) {
                throw e
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
    // En modo API con el backend caído se DEGRADA a la cola local y lo
    // avisa, para que el panel nunca quede mudo en una demo.
    // =========================================================
    fun cargarPedidos() {
        viewModelScope.launch {
            try {
                val lista = pedidoRepository().listarPedidos()
                _uiState.value = _uiState.value.copy(pedidosEnviados = lista, errorMessage = null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val colaLocal = if (modoDatosRepository.modo.value == ModoDatos.API) {
                    pedidoRepositoryLocal.listarPedidos()
                } else emptyList()
                _uiState.value = _uiState.value.copy(
                    pedidosEnviados = colaLocal,
                    errorMessage = "Backend no disponible: ${ApiError.mensaje(e)}"
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
            } catch (e: CancellationException) {
                throw e
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

    // URL del backend (panel Admin). Devuelve false si el formato es inválido.
    fun cambiarBaseUrl(nueva: String): Boolean {
        val aplicada = modoDatosRepository.cambiarBaseUrl(nueva.trim())
        if (aplicada && modoDatosRepository.modo.value == ModoDatos.API) cargarPedidos()
        return aplicada
    }
}
