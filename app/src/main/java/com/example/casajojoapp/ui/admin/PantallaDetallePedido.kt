package com.example.casajojoapp.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.LineaDetallePedido
import com.example.casajojoapp.domain.model.Pedido
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.theme.*

// =========================================================
// DETALLE COMPLETO DE UN PEDIDO (vista del administrador):
// - Si el pedido tiene N platos armados (modo LOCAL), muestra la ficha
//   ESPECÍFICA de cada uno: proteína, acompañamientos, ensalada, extras
//   con sus precios y sus restricciones.
// - Si viene del backend (modo API, detalles planos), muestra cada línea
//   con cantidad, producto, precio y observación.
// - Se abre desde "Ver detalle completo" en las tarjetas de la cola.
// =========================================================
@Composable
fun PantallaDetallePedido(
    navController: NavController,
    viewModel: OrderViewModel,
    pedidoId: Long
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresca la cola al entrar para tener los datos más recientes.
    LaunchedEffect(Unit) { viewModel.cargarPedidos() }

    val pedido = uiState.pedidosEnviados.firstOrNull { it.id == pedidoId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        when {
            pedido == null -> {
                EstadoDetalleNoEncontrado(
                    modifier = Modifier.weight(1f),
                    pedidoId = pedidoId,
                    onVolver = { navController.navigateUp() }
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumenPedidoDetalle(pedido = pedido)

                    Text(
                        text = "ESPECIFICACIÓN DEL PEDIDO",
                        color = JoJoAcento,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (pedido.platos.isNotEmpty()) {
                        // Modo LOCAL: ficha por cada plato armado.
                        pedido.platos.forEachIndexed { indice, plato ->
                            TarjetaPlatoDetalle(
                                numero = indice + 1,
                                totalPlatos = pedido.platos.size,
                                plato = plato
                            )
                        }
                    } else if (pedido.detalles.isNotEmpty()) {
                        // Modo API: líneas planas devueltas por el backend.
                        pedido.detalles.forEach { linea ->
                            TarjetaLineaDetalle(linea = linea)
                        }
                    } else {
                        Text(
                            text = "Este pedido no trae detalle de productos.",
                            color = JoJoTextoSecundario,
                            fontSize = 14.sp
                        )
                    }

                    if (!pedido.notasRestricciones.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, JoJoAmbar),
                            colors = CardDefaults.cardColors(containerColor = JoJoSuperficie),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Observaciones generales",
                                    color = JoJoAmbar,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = pedido.notasRestricciones,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Cabecera con los datos del pedido: hora, estado, cliente y total.
@Composable
private fun ResumenPedidoDetalle(pedido: Pedido) {
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
        modifier = Modifier.fillMaxWidth()
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
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
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
                text = "Pedido #${pedido.id}" +
                    (pedido.codigoSeguimiento?.takeIf { it.isNotBlank() }
                        ?.let { " · $it" } ?: ""),
                color = JoJoTextoSecundario,
                fontSize = 13.sp
            )

            HorizontalDivider(color = JoJoBorde, thickness = 1.dp)

            FilaDetalle(etiqueta = "Cliente", valor = pedido.clienteNombre)
            if (pedido.clienteTelefono.isNotBlank()) {
                FilaDetalle(etiqueta = "Teléfono", valor = pedido.clienteTelefono)
            }
            FilaDetalle(
                etiqueta = "Platos / líneas",
                valor = if (pedido.platos.isNotEmpty()) "${pedido.platos.size} plato(s)"
                else "${pedido.detalles.size} línea(s) · ${pedido.unidades} unidad(es)"
            )
            FilaDetalle(etiqueta = "Total", valor = "$${pedido.total.toLong()}")
        }
    }
}

// Ficha de UN plato armado (modo LOCAL): especificación completa.
@Composable
private fun TarjetaPlatoDetalle(
    numero: Int,
    totalPlatos: Int,
    plato: PlatoArmado
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, JoJoBorde),
        colors = CardDefaults.cardColors(containerColor = JoJoSuperficie),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PLATO $numero DE $totalPlatos",
                color = JoJoNaranjo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            ItemPlato(titulo = "Proteína", valor = plato.proteina?.nombre ?: "Sin proteína")
            ItemPlato(
                titulo = "Acompañamientos (${plato.acompanamientos.size})",
                valor = if (plato.acompanamientos.isEmpty()) "Ninguno"
                else plato.acompanamientos.joinToString("\n") { "• ${it.nombre}" }
            )
            ItemPlato(titulo = "Ensalada", valor = plato.ensalada?.nombre ?: "Sin ensalada")
            if (plato.extras.isNotEmpty()) {
                ItemPlato(
                    titulo = "Extras (${plato.extras.size})",
                    valor = plato.extras.joinToString("\n") { "• ${it.nombre} (+$${it.precio.toLong()})" }
                )
            }

            if (plato.notas.isNotBlank()) {
                HorizontalDivider(color = JoJoBorde, thickness = 1.dp)
                ItemPlato(
                    titulo = "Restricciones de este plato",
                    valor = plato.notas,
                    esRestriccion = true
                )
            }
        }
    }
}

// Línea plana de un pedido del backend (modo API).
@Composable
private fun TarjetaLineaDetalle(linea: LineaDetallePedido) {
    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, JoJoBorde),
        colors = CardDefaults.cardColors(containerColor = JoJoSuperficie),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${linea.cantidad}× ${linea.nombreItem}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$${linea.precioUnitario.toLong()}",
                    color = JoJoNaranjo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!linea.observacion.isNullOrBlank()) {
                Text(
                    text = "Restricción: ${linea.observacion}",
                    color = JoJoTextoRestriccion,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

// Par etiqueta/valor dentro de la ficha de resumen.
@Composable
private fun FilaDetalle(etiqueta: String, valor: String) {
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
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

// Campo de especificación dentro de la ficha de un plato.
@Composable
private fun ItemPlato(
    titulo: String,
    valor: String,
    esRestriccion: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = titulo,
            color = JoJoTextoSecundario,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = valor,
            color = if (esRestriccion) JoJoTextoRestriccion else Color.White,
            fontSize = 14.sp,
            fontStyle = if (esRestriccion) FontStyle.Italic else FontStyle.Normal
        )
    }
}

// Estado vacío: el pedido ya no está en la cola.
@Composable
private fun EstadoDetalleNoEncontrado(
    modifier: Modifier = Modifier,
    pedidoId: Long,
    onVolver: () -> Unit
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
                text = "No se encontró el pedido #$pedidoId.\nPuede que ya no esté en la cola.",
                color = JoJoTextoSecundario,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoJoNaranjo,
                    contentColor = Color.Black
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 24.dp, vertical = 14.dp
                )
            ) {
                Text(text = "Volver", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
