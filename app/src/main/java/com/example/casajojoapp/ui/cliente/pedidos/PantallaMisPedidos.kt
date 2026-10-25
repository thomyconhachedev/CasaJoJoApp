package com.example.casajojoapp.ui.cliente.pedidos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.Pedido
import com.example.casajojoapp.domain.model.Rol
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.theme.*
import kotlinx.coroutines.delay

// =========================================================
// MIS PEDIDOS (vista de cliente, botón del medio de la barra inferior):
// - Muestra SOLO los pedidos de la cuenta con sesión iniciada (linkeados
//   a la cuenta): coincide por usuarioId y, si el backend no lo devuelve,
//   por teléfono de contacto.
// - Invitado: se le ofrece iniciar sesión (los pedidos quedan ligados a
//   la cuenta, no al dispositivo).
// =========================================================
@Composable
fun PantallaMisPedidos(
    navController: NavController,
    viewModel: OrderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val sesion by viewModel.sesion.collectAsState()

    // Refresco en segundo plano (45 s): el cliente ve el cambio de estado
    // (p. ej. "Listo para retiro") sin tener que salir y volver a entrar.
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.cargarPedidos()
            delay(45_000)
        }
    }

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
                text = "Mis pedidos",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (sesion.rol == Rol.INVITADO) {
                    "Tus pedidos quedan asociados a tu cuenta"
                } else {
                    "Pedidos de @${sesion.username}, ordenados por hora de retiro"
                },
                color = JoJoTextoSecundario,
                fontSize = 14.sp
            )
        }

        when {
            // Invitado: sin cuenta no hay historial vinculado.
            sesion.rol == Rol.INVITADO -> {
                EstadoVacioMisPedidos(
                    modifier = Modifier.weight(1f),
                    mensaje = "Inicia sesión o crea una cuenta para ver aquí tus pedidos.",
                    textoBoton = "Iniciar sesión",
                    onAccion = { navController.navigate("login") { launchSingleTop = true } }
                )
            }

            else -> {
                val pedidosDelUsuario = uiState.pedidosEnviados
                    .filter { pedido ->
                        val idSesion = sesion.usuarioId
                        when {
                            // Pedido con cuenta: coincide por usuarioId.
                            idSesion != null && pedido.usuarioId != null ->
                                pedido.usuarioId == idSesion
                            // El backend aún no devuelve usuarioId: respaldo
                            // por teléfono de contacto de la misma cuenta.
                            idSesion != null ->
                                pedido.clienteTelefono.isNotBlank() &&
                                    pedido.clienteTelefono == sesion.telefono
                            else -> false
                        }
                    }
                    .sortedWith(
                        compareBy(
                            { it.estado == EstadoPedido.ENTREGADO },
                            { it.horaRetiro }
                        )
                    )

                if (pedidosDelUsuario.isEmpty()) {
                    EstadoVacioMisPedidos(
                        modifier = Modifier.weight(1f),
                        mensaje = "Aún no tienes pedidos.\nArma tu primer plato y aparecerá aquí.",
                        textoBoton = "Armar mi pedido",
                        onAccion = {
                            navController.navigate("seleccionProteinaSC") {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pedidosDelUsuario, key = { it.id }) { pedido ->
                            TarjetaPedidoCliente(
                                pedido = pedido,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

// Estado vacío con mensaje + acción (login o armado).
@Composable
private fun EstadoVacioMisPedidos(
    modifier: Modifier = Modifier,
    mensaje: String,
    textoBoton: String,
    onAccion: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = mensaje,
                color = JoJoTextoSecundario,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onAccion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoJoNaranjo,
                    contentColor = Color.Black
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 24.dp, vertical = 14.dp
                )
            ) {
                Text(text = textoBoton, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// Tarjeta de solo lectura de un pedido del cliente: hora, estado, detalle.
@Composable
private fun TarjetaPedidoCliente(pedido: Pedido, modifier: Modifier = Modifier) {
    val colorEstado = when (pedido.estado) {
        EstadoPedido.RECIBIDO -> EstadoRecibido
        EstadoPedido.EN_PREPARACION -> EstadoEnPreparacion
        EstadoPedido.LISTO_PARA_RETIRO -> EstadoListo
        EstadoPedido.ENTREGADO -> EstadoEntregado
        EstadoPedido.CANCELADO -> EstadoCancelado
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pedido.horaRetiro,
                    color = JoJoNaranjo,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                // Badge de estado con la paleta semántica del tema.
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colorEstado.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, colorEstado)
                ) {
                    Text(
                        text = pedido.estado.nombre,
                        color = colorEstado,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = "#${pedido.id}",
                color = JoJoTextoSecundario,
                fontSize = 12.sp
            )

            if (!pedido.codigoSeguimiento.isNullOrBlank()) {
                Text(
                    text = "Código: ${pedido.codigoSeguimiento}",
                    color = JoJoAcento,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(color = JoJoBorde, thickness = 1.dp)

            // Productos del pedido: en LOCAL vienen agrupados por plato;
            // en API llegan como líneas planas de detalles.
            val resumenProductos = if (pedido.platos.isNotEmpty()) {
                pedido.platos.joinToString("\n") { plato ->
                    val nombres = listOfNotNull(plato.proteina?.nombre) +
                        plato.acompanamientos.map { it.nombre } +
                        listOfNotNull(plato.ensalada?.nombre) +
                        plato.extras.map { it.nombre }
                    "• " + nombres.joinToString(" + ")
                }
            } else {
                pedido.detalles.joinToString("\n") { "• ${it.cantidad}× ${it.nombreItem}" }
            }
            Text(
                text = resumenProductos,
                color = JoJoTextoSecundario,
                fontSize = 13.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
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
