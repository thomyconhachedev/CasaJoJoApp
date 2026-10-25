package com.example.casajojoapp.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.theme.JoJoFondoApp
import com.example.casajojoapp.ui.theme.JoJoRojoClaro
import com.example.casajojoapp.ui.theme.JoJoTextoSecundario

// =========================================================
// VISTA DE PEDIDOS DEL ADMINISTRADOR (solo cola + contadores):
// - Muestra ÚNICAMENTE la cola de comandas y el cuadro de contadores por
//   estado (sin el panel de modo de datos/URL).
// - Al presionar un contador se filtra la cola por ese estado (p. ej.
//   "Listo para retiro" muestra solo esos); "Todos" o volver a presionar
//   limpia el filtro. El filtro inicial puede venir de la ruta
//   (?filtro=ESTADO) cuando se abre desde el panel actual.
// - Se accede desde el botón "Pedidos/Cola" de la barra inferior o desde
//   el botón "Ver cola filtrable" del panel de administración.
// =========================================================
@Composable
fun PantallaPedidosAdmin(
    navController: NavController,
    viewModel: OrderViewModel,
    filtroInicial: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    // Recarga la cola al entrar (refleja cambios hechos en otra pantalla).
    LaunchedEffect(Unit) { viewModel.cargarPedidos() }

    // Filtro por estado: null = Todos. Se persiste al rotar/recrear.
    var filtro by rememberSaveable { mutableStateOf(filtroInicial) }
    val estadoFiltro = filtro?.let { nombre ->
        EstadoPedido.entries.firstOrNull { it.name == nombre }
    }

    // Misma cola ordenada que el panel: no entregados primero, por hora.
    val pedidosOrdenados = uiState.pedidosEnviados.sortedWith(
        compareBy(
            { it.estado == EstadoPedido.ENTREGADO },
            { it.horaRetiro }
        )
    )
    val pedidosFiltrados = if (estadoFiltro == null) {
        pedidosOrdenados
    } else {
        pedidosOrdenados.filter { it.estado == estadoFiltro }
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
                text = "Pedidos",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (estadoFiltro == null) {
                    "Todos los pedidos (${pedidosOrdenados.size}), ordenados por hora de retiro"
                } else {
                    "Filtrado: ${estadoFiltro.nombre} · ${pedidosFiltrados.size} pedido(s)"
                },
                color = JoJoTextoSecundario,
                fontSize = 14.sp
            )
        }

        // Cuadro de contadores: EN ESTA vista los chips filtran la lista.
        ContadoresPorEstado(
            pedidos = pedidosOrdenados,
            filtroSeleccionado = estadoFiltro,
            onFiltrar = { nuevoFiltro -> filtro = nuevoFiltro?.name }
        )

        uiState.errorMessage?.let { mensaje ->
            Text(text = mensaje, color = JoJoRojoClaro, fontSize = 13.sp)
        }

        when {
            pedidosFiltrados.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (estadoFiltro == null) {
                            "Aún no hay pedidos en la cola."
                        } else {
                            "No hay pedidos ${estadoFiltro.nombre.lowercase()} en este momento."
                        },
                        color = JoJoTextoSecundario,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pedidosFiltrados, key = { it.id }) { pedido ->
                        TarjetaPedidoAdmin(
                            pedido = pedido,
                            modifier = Modifier.animateItem(),
                            onVerDetalle = {
                                navController.navigate("detallePedido/${pedido.id}")
                            },
                            onCambioEstado = { nuevoEstado ->
                                viewModel.actualizarEstadoPedido(pedido.id, nuevoEstado)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}
