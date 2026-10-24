package com.example.casajojoapp.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.data.modo.ModoDatos
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.Pedido
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.theme.EstadoEnPreparacion
import com.example.casajojoapp.ui.theme.EstadoEntregado
import com.example.casajojoapp.ui.theme.EstadoListo
import com.example.casajojoapp.ui.theme.EstadoRecibido
import com.example.casajojoapp.ui.theme.*

// =========================================================
// PANTALLA ADMIN: muestra la COLA de pedidos, ordenados por hora de retiro
// (lo más próximo primero), con los estados del backend
// (RECIBIDO -> EN_PREPARACION -> LISTO_PARA_RETIRO -> ENTREGADO).
// La cola se carga con GET /api/v1/pedidos en modo API (o desde la cola en
// memoria en modo LOCAL) y el cambio de estado viaja en
// PATCH /api/v1/pedidos/{id}/estado, siempre al siguiente estado.
// =========================================================

@Composable
fun PantallaAdmin(
    navController: NavController,
    viewModel: OrderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val modo by viewModel.modoDatos.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()

    // Recarga la cola al entrar al panel (refleja cambios hechos en el backend
    // o un cambio de modo ocurrido fuera de esta pantalla).
    LaunchedEffect(Unit) { viewModel.cargarPedidos() }

    // Cola: primero los NO entregados por hora de retiro (lo más próximo
    // primero); los ENTREGADOS quedan al final de la lista.
    val pedidosEnCola = uiState.pedidosEnviados.sortedWith(
        compareBy(
            { it.estado == EstadoPedido.ENTREGADO },
            { it.horaRetiro }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Pedidos en cola",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ordenados por hora de retiro (lo más próximo primero)",
                color = JoJoTextoSecundario,
                fontSize = 14.sp
            )
        }

        // Interruptor LOCAL/API + URL del backend (solo aplica en modo API).
        SeccionModoDatos(
            modo = modo,
            baseUrl = baseUrl,
            isLoadingMenu = uiState.isLoadingMenu,
            menuError = uiState.menuError,
            menuAdvertencia = uiState.menuAdvertencia,
            menuMensajeExito = uiState.menuMensajeExito,
            onCambioModo = { viewModel.cambiarModoDatos(it) },
            onCambioBaseUrl = { viewModel.cambiarBaseUrl(it) },
            onRefrescarMenu = { viewModel.refrescarMenu() }
        )

        uiState.errorMessage?.let { mensaje ->
            Text(text = mensaje, color = JoJoRojoClaro, fontSize = 13.sp)
            TextButton(onClick = { viewModel.cargarPedidos() }) {
                Text(text = "Reintentar", color = JoJoNaranjo, fontSize = 13.sp)
            }
        }

        if (pedidosEnCola.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay pedidos en la cola.",
                    color = JoJoTextoSecundario,
                    fontSize = 16.sp
                )
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                ContadoresPorEstado(pedidos = pedidosEnCola)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ANIMACIÓN (IE 2.2.2): key por id de pedido + animateItem,
                    // así al cambiar de estado los pedidos se reordenan con
                    // desplazamiento fluido (los ENTREGADOS bajan al final).
                    items(pedidosEnCola, key = { it.id }) { pedido ->
                        TarjetaPedidoAdmin(
                            pedido = pedido,
                            modifier = Modifier.animateItem(),
                            onCambioEstado = { nuevoEstado ->
                                // LOCAL: cambia el estado en memoria.
                                // API: PATCH /api/v1/pedidos/{id}/estado.
                                viewModel.actualizarEstadoPedido(pedido.id, nuevoEstado)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Interruptor LOCAL / API + campo de URL del backend (solo visible en modo API).
@Composable
private fun SeccionModoDatos(
    modo: ModoDatos,
    baseUrl: String,
    isLoadingMenu: Boolean,
    menuError: String?,
    menuAdvertencia: String?,
    menuMensajeExito: String?,
    onCambioModo: (ModoDatos) -> Unit,
    onCambioBaseUrl: (String) -> Boolean,
    onRefrescarMenu: () -> Unit
) {
    // Estado local del campo URL: se resetea cuando cambia la URL guardada.
    var urlInput by remember(baseUrl) { mutableStateOf(baseUrl) }
    // Error de validación del último intento de cambiar la URL.
    var urlError by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, JoJoBorde),
        colors = CardDefaults.cardColors(containerColor = JoJoSuperficie),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Origen de datos",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (modo == ModoDatos.LOCAL) "Local (en memoria)" else "API (backend)",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (modo == ModoDatos.LOCAL) {
                            "Cola en memoria; se pierde al reiniciar."
                        } else {
                            "Consume el backend en la URL de abajo."
                        },
                        color = JoJoTextoSecundario,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = (modo == ModoDatos.API),
                    onCheckedChange = { onCambioModo(if (it) ModoDatos.API else ModoDatos.LOCAL) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = JoJoNaranjo,
                        checkedThumbColor = Color.Black
                    )
                )
            }

            // El campo de URL solo tiene sentido cuando la app consume la API.
            if (modo == ModoDatos.API) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("URL del backend") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JoJoNaranjo,
                            unfocusedBorderColor = JoJoBorde,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = JoJoNaranjo,
                            focusedLabelColor = JoJoNaranjo,
                            unfocusedLabelColor = JoJoGrisMedio,
                            focusedContainerColor = JoJoGrisOscuro,
                            unfocusedContainerColor = JoJoGrisOscuro
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Cambio URL: si el formato es inválido se avisa y
                            // NO se pisa la URL anterior (ModoDatosRepository).
                            urlError = if (onCambioBaseUrl(urlInput.trim())) {
                                null
                            } else {
                                "URL inválida: debe empezar con http:// o https://"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JoJoNaranjo,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(text = "Aplicar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                urlError?.let { mensaje ->
                    Text(text = mensaje, color = JoJoRojoClaro, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = JoJoBorde, thickness = 1.dp)

            // Refresca el menú contra el origen vigente (útil tras cambiar el
            // Google Sheet y sincronizar desde el backend).
            OutlinedButton(
                onClick = onRefrescarMenu,
                enabled = !isLoadingMenu,
                border = BorderStroke(1.dp, JoJoNaranjo),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JoJoNaranjo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isLoadingMenu) "Refrescando..." else "Refrescar menú",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Feedback del refresco: éxito (verde), error (rojo) o
            // advertencia de fallback local (ámbar) si el backend está caído.
            menuMensajeExito?.let { mensaje ->
                Text(text = mensaje, color = JoJoVerdeClaro, fontSize = 13.sp)
            }
            menuError?.let { mensaje ->
                Text(text = mensaje, color = JoJoRojoClaro, fontSize = 13.sp)
            }
            menuAdvertencia?.let { mensaje ->
                Text(text = mensaje, color = JoJoAmbar, fontSize = 13.sp)
            }
        }
    }
}

// Tarjeta de un pedido de la cola (hora + estado + detalle)
@Composable
fun TarjetaPedidoAdmin(
    pedido: Pedido,
    modifier: Modifier = Modifier,
    onCambioEstado: (EstadoPedido) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, JoJoBorde),
        colors = CardDefaults.cardColors(containerColor = JoJoSuperficie),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hora de retiro destacada + menú para cambiar el estado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pedido.horaRetiro,
                    color = JoJoNaranjo,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EstadoMenu(estado = pedido.estado, onCambio = onCambioEstado)
            }
            Text(
                text = "#${pedido.id}",
                color = JoJoTextoSecundario,
                fontSize = 12.sp
            )
            HorizontalDivider(color = JoJoBorde, thickness = 1.dp)

            FilaAdmin(etiqueta = "Cliente", valor = pedido.clienteNombre)
            FilaAdmin(etiqueta = "Teléfono", valor = pedido.clienteTelefono)
            FilaAdmin(
                etiqueta = "Unidades",
                valor = "${pedido.unidades} ítem(s)"
            )
            if (!pedido.notasRestricciones.isNullOrBlank()) {
                FilaAdmin(
                    etiqueta = "Restricciones",
                    valor = pedido.notasRestricciones
                )
            }

            // Total a pagar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$${pedido.total.toLong()}",
                    color = JoJoNaranjo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Contadores por estado (chips) mostrados arriba de la cola
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContadoresPorEstado(pedidos: List<Pedido>) {
    val conteo = pedidos.groupingBy { it.estado }.eachCount()
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EstadoPedido.entries.forEach { estado ->
            val color = colorDeEstado(estado)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, color)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = estado.nombre,
                        color = color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${conteo[estado] ?: 0}",
                        color = color,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Menú desplegable para avanzar el estado del pedido. El backend solo admite
// pasar al SIGUIENTE estado (409 en cualquier otro salto), así que la lista
// tiene exactamente una opción; si el pedido ya está ENTREGADO no hay menú.
@Composable
private fun EstadoMenu(
    estado: EstadoPedido,
    onCambio: (EstadoPedido) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val siguiente = estado.siguiente

    if (siguiente == null && estado != EstadoPedido.RECIBIDO && estado != EstadoPedido.EN_PREPARACION) {
        Text(
            text = estado.nombre,
            color = colorDeEstado(estado),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        return
    }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            border = BorderStroke(1.dp, colorDeEstado(estado)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorDeEstado(estado)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = estado.nombre,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (siguiente != null) {
                DropdownMenuItem(
                    text = { Text("→ ${siguiente.nombre}") },
                    onClick = {
                        onCambio(siguiente)
                        expanded = false
                    }
                )
            }
            if (estado == EstadoPedido.RECIBIDO || estado == EstadoPedido.EN_PREPARACION) {
                DropdownMenuItem(
                    text = { Text("Cancelar pedido") },
                    onClick = {
                        onCambio(EstadoPedido.CANCELADO)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Color de cada estado para el badge/menú (paleta semántica del tema)
private fun colorDeEstado(estado: EstadoPedido): Color = when (estado) {
    EstadoPedido.RECIBIDO -> EstadoRecibido
    EstadoPedido.EN_PREPARACION -> EstadoEnPreparacion
    EstadoPedido.LISTO_PARA_RETIRO -> EstadoListo
    EstadoPedido.ENTREGADO -> EstadoEntregado
    EstadoPedido.CANCELADO -> JoJoRojoClaro
}

// Fila etiqueta: valor dentro de la tarjeta
@Composable
private fun FilaAdmin(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            color = JoJoTextoSecundario,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

// CAMBIO ROLES: gate del panel. Sin sesión ADMIN se ofrece ir al login
// (con origenAdmin para exigir una cuenta de administrador).
@Composable
fun AccesoRestringidoAdmin(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderPrincipal(navController)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Panel de administración",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Esta sección es solo para cuentas de administrador.\nInicia sesión con una cuenta admin para ver la cola de cocina.",
            color = JoJoTextoSecundario,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = {
                navController.navigate("login?origenAdmin=true") {
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = JoJoNaranjo,
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Iniciar sesión como administrador", fontWeight = FontWeight.Bold)
        }
    }
}