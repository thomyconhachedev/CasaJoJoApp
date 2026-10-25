package com.example.casajojoapp.ui.cliente.armado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.casajojoapp.data.ServicioLocator
import com.example.casajojoapp.data.local.mappers.aDominio
import com.example.casajojoapp.data.local.mappers.aEntity
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

    // CAMBIO HISTORIAL: fuente local de pedidos (Room) para persistir el
    // historial y restaurar la cola tras reiniciar.
    private val pedidosDao = ServicioLocator.baseDeDatos.pedidoDao()

    init {
        // Restaura la sesión persistida, repuebla la cola local desde Room
        // (si está vacía) y recarga todo: la cola ya se ve restaurada antes
        // del primer fetch.
        viewModelScope.launch {
            (authRepository as? com.example.casajojoapp.data.repository.AuthRepositoryLocal)
                ?.restaurarSesionPrevia()
            restaurarColaLocal()
            cargarPedidos()
            cargarDatosClienteGuardados()
        }
        cargarMenu()
    }

    // Restaura en la cola LOCAL los pedidos que Room guardó en modo LOCAL.
    private suspend fun restaurarColaLocal() {
        val guardados = try {
            pedidosDao.listarPorOrigen(ModoDatos.LOCAL.name).map { it.aDominio() }
        } catch (_: Exception) {
            emptyList()
        }
        if (guardados.isNotEmpty()) pedidoRepositoryLocal.sembrar(guardados)
    }

    // Historial completo (cualquier origen): respaldo cuando la cola viene
    // vacía o el backend no responde.
    private suspend fun historialLocal(): List<Pedido> = try {
        pedidosDao.listar().map { it.aDominio() }
    } catch (_: Exception) {
        emptyList()
    }

    // Persiste un pedido sin interrumpir el flujo si algo falla (el
    // historial es un respaldo, nunca debe romper el envío/estado).
    private suspend fun guardarEnHistorial(pedido: Pedido) {
        try {
            pedidosDao.upsert(pedido.aEntity(modoDatosRepository.modo.value.name))
        } catch (_: Exception) {
            // no-op: mejor perder un registro de historial que un envío
        }
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
    // categoría en el estado. En modo API con el backend caído se DEGRADA
    // al menú local con una advertencia (el flujo de armado nunca se bloquea).
    // =========================================================
    fun cargarMenu(refrescarRemoto: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingMenu = true,
                menuError = null,
                menuMensajeExito = null,
                menuAdvertencia = null
            )
            try {
                val productos = if (refrescarRemoto) {
                    menuRepository().refrescar()
                } else {
                    menuRepository().listarProductos()
                }
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
                if (modoDatosRepository.modo.value == ModoDatos.API) {
                    // Fallback LOCAL amable: se pinta el menú hardcodeado y la
                    // advertencia queda para el panel admin (con su reintentar).
                    val respaldo = MenuRepositoryLocal().listarProductos()
                    _uiState.value = _uiState.value.copy(
                        isLoadingMenu = false,
                        proteinasDisponibles = respaldo.deCategoria(CategoriaItem.PROTEINA),
                        acompanamientosDisponibles = respaldo.deCategoria(CategoriaItem.ACOMPANAMIENTO),
                        ensaladasDisponibles = respaldo.deCategoria(CategoriaItem.ENSALADA),
                        extrasDisponibles = respaldo.extras(),
                        menuAdvertencia = "Backend no disponible: se muestra el menú local de respaldo. (${ApiError.mensaje(e)})"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMenu = false,
                        menuError = "No se pudo refrescar el menú: ${ApiError.mensaje(e)}"
                    )
                }
            }
        }
    }

    // Botón "Refrescar menú" del panel Admin: en API hace POST sincronizar
    // + GET menú; en LOCAL recarga la fuente hardcodeada.
    fun refrescarMenu() {
        cargarMenu(refrescarRemoto = true)
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
                // NOTIFICACIÓN: avisa a caja de que llegó una comanda nueva.
                val codigoConfirmado = codigoDePedido(confirmado, confirmado.id)
                com.example.casajojoapp.data.ServicioLocator.notificador.notificar(
                    id = notificacionId(confirmado.id),
                    titulo = "Nueva comanda $codigoConfirmado",
                    texto = "Retiro a las ${confirmado.horaRetiro} · ${confirmado.clienteNombre}"
                )
                // RECORDATORIO LOCAL: aviso 5 minutos antes de la hora de
                // retiro en ESTE dispositivo (AlarmManager, funciona con la
                // app cerrada; se cancela al cancelar o listar el pedido).
                val momentoAviso =
                    com.example.casajojoapp.util.HorarioRetiro.momentoRecordatorio(
                        confirmado.horaRetiro
                    )
                if (momentoAviso != null) {
                    com.example.casajojoapp.data.ServicioLocator.recordatorios.programar(
                        idPedido = confirmado.id,
                        momento = momentoAviso,
                        titulo = "Tu pedido $codigoConfirmado es a las ${confirmado.horaRetiro}",
                        texto = "Faltan 5 minutos para retirar tu pedido en La Casa JoJo."
                    )
                }
                // CAMBIO HISTORIAL: persiste el pedido recién creado.
                guardarEnHistorial(confirmado)
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
                // CAMBIO HISTORIAL: cola vacía (p. ej. reinicio en modo LOCAL
                // o cliente sin pedidos del día) => se sirve desde Room.
                val efectiva = if (lista.isEmpty()) historialLocal() else lista
                _uiState.value = _uiState.value.copy(
                    pedidosEnviados = efectiva,
                    errorMessage = null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Backend inaccesible: memoria local y, si está vacía, Room.
                val respaldo = pedidoRepositoryLocal.listarPedidos()
                    .ifEmpty { historialLocal() }
                _uiState.value = _uiState.value.copy(
                    pedidosEnviados = respaldo,
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
                // El aviso de "5 min antes" deja de tener sentido si el
                // pedido se cancela o ya está listo para retiro.
                if (nuevoEstado == EstadoPedido.CANCELADO ||
                    nuevoEstado == EstadoPedido.LISTO_PARA_RETIRO
                ) {
                    com.example.casajojoapp.data.ServicioLocator.recordatorios.cancelar(id)
                }
                // NOTIFICACIÓN LOCAL con el CÓDIGO del pedido (p. ej.
                // "Tu pedido JOJO-0001 está listo para retiro").
                val pedido = _uiState.value.pedidosEnviados.firstOrNull { it.id == id }
                val codigo = codigoDePedido(pedido, id)
                val titulo = when (nuevoEstado) {
                    EstadoPedido.LISTO_PARA_RETIRO ->
                        "Tu pedido $codigo está listo para retiro"
                    EstadoPedido.CANCELADO ->
                        "Tu pedido $codigo fue cancelado"
                    else ->
                        "Tu pedido $codigo → ${nuevoEstado.nombre}"
                }
                val texto = when (nuevoEstado) {
                    EstadoPedido.LISTO_PARA_RETIRO -> {
                        val hora = pedido?.horaRetiro?.takeIf { it.isNotBlank() }
                        if (hora != null) "Retira tu pedido a las $hora en La Casa JoJo."
                        else "Pasa por tu pedido en La Casa JoJo."
                    }
                    EstadoPedido.CANCELADO ->
                        "Si tienes dudas, contáctanos directamente."
                    else ->
                        "Estado actualizado: ${nuevoEstado.nombre}."
                }
                com.example.casajojoapp.data.ServicioLocator.notificador.notificar(
                    id = notificacionId(id),
                    titulo = titulo,
                    texto = texto
                )
                // CAMBIO HISTORIAL: persiste el cambio de estado en Room
                // (sobre la copia local, sin esperar al refresco).
                _uiState.value.pedidosEnviados.firstOrNull { it.id == id }?.let { previo ->
                    guardarEnHistorial(previo.copy(estado = nuevoEstado))
                }
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

    // Id de notificación derivado del pedido: la misma comanda reemplaza
    // su aviso anterior en vez de apilar notificaciones repetidas.
    private fun notificacionId(idPedido: Long): Int =
        if (idPedido in 1..Int.MAX_VALUE) idPedido.toInt()
        else (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

    // Código de seguimiento del pedido para los textos de notificación
    // (JOJO-xxxx en local, el del backend en API, "#id" como respaldo).
    private fun codigoDePedido(pedido: Pedido?, id: Long): String =
        pedido?.codigoSeguimiento?.takeIf { it.isNotBlank() } ?: "#$id"

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
