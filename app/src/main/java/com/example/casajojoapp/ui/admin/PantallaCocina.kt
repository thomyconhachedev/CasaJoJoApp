package com.example.casajojoapp.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.casajojoapp.ui.theme.EstadoEnPreparacion
import com.example.casajojoapp.ui.theme.EstadoRecibido
import com.example.casajojoapp.ui.theme.JoJoAcento
import com.example.casajojoapp.ui.theme.JoJoBorde
import com.example.casajojoapp.ui.theme.JoJoFondoApp
import com.example.casajojoapp.ui.theme.JoJoNaranjo
import com.example.casajojoapp.ui.theme.JoJoRojoClaro
import com.example.casajojoapp.ui.theme.JoJoSuperficie
import com.example.casajojoapp.ui.theme.JoJoTextoRestriccion
import com.example.casajojoapp.ui.theme.JoJoTextoSecundario
import kotlinx.coroutines.delay

// =========================================================
// PANTALLA DE COCINA (solo ADMIN): la vista de trabajo del encargado.
// - Dos pestañas superiores: "Recibidos" (RECIBIDO) y "En preparación"
//   (EN_PREPARACION). Una pestaña = un estado del enum EstadoPedido.
// - Cada tarjeta muestra el CONTENIDO del pedido para cocinarlo sin abrir
//   otra pantalla: hora de entrega, número de pedido y la especificación
//   de cada plato (proteína, acompañamientos, ensalada, extras y notas).
// - Botones directos de estado: RECIBIDO -> "Iniciar preparación" y
//   EN_PREPARACION -> "Marcar listo para retiro". Al marcar listo el
//   pedido SALE de esta pantalla y queda en la cola completa (Cola).
// - Los pedidos LISTO_PARA_RETIRO / ENTREGADO / CANCELADO no se muestran
//   aquí: se gestionan desde la pantalla Cola.
// =========================================================

@Composable
fun PantallaCocina(
    navController: NavController,
    viewModel: OrderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresco en segundo plano (45 s): igual que las otras vistas de admin,
    // la cola se mantiene sincronizada con el backend sin recargar a mano.
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.cargarPedidos()
            delay(45_000)
        }
    }

    // Pestaña activa: RECIBIDO o EN_PREPARACION (1 pestaña = 1 estado).
    // Se guarda por nombre para que rememberSaveable sobreviva a la rotación.
    var filtro by rememberSaveable { mutableStateOf(EstadoPedido.RECIBIDO.name) }
    val estadoFiltro = EstadoPedido.desde(filtro)

    // Misma cola ordenada que Cola: por hora de retiro (hora de entrega)
    // ascendente; como cada pestaña tiene un solo estado, no hace falta
    // empujar los ENTREGADOS al final (aquí nunca se muestran).
    val pedidosFiltrados = uiState.pedidosEnviados
        .filter { it.estado == estadoFiltro }
        .sortedBy { it.horaRetiro }

    val conteoRecibidos = uiState.pedidosEnviados.count { it.estado == EstadoPedido.RECIBIDO }
    val conteoEnPreparacion =
        uiState.pedidosEnviados.count { it.estado == EstadoPedido.EN_PREPARACION }

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
                text = "Cocina",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pedidos para preparar, ordenados por hora de entrega",
                color = JoJoTextoSecundario,
                fontSize = 14.sp
            )
        }

        // Pestañas superiores: Recibidos / En preparación (con conteo).
        SelectorEstadoCocina(
            seleccionado = estadoFiltro,
            conteoRecibidos = conteoRecibidos,
            conteoEnPreparacion = conteoEnPreparacion,
            onSeleccionar = { nuevo -> filtro = nuevo.name }
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
                        text = if (estadoFiltro == EstadoPedido.EN_PREPARACION) {
                            "No hay pedidos en preparación en este momento."
                        } else {
                            "No hay pedidos recibidos en este momento."
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
                        TarjetaPedidoCocina(
                            pedido = pedido,
                            modifier = Modifier.animateItem(),
                            onCambioEstado = { nuevoEstado ->
                                viewModel.actualizarEstadoPedido(pedido.id, nuevoEstado)
                            }
                        )
                    }
                }
            }
        }

        // Acceso discreto al panel de datos (LOCAL/API + URL): como la
        // pestaña Cocina reemplazó a Inicio en la barra del admin, el panel
        // sigue estando a un toque de distancia.
        OutlinedButton(
            onClick = { navController.navigate("admin") },
            border = BorderStroke(1.dp, JoJoBorde),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = JoJoTextoSecundario),
            contentPadding = PaddingValues(vertical = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Panel de datos", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

// Dos botones de pestaña (Recibidos / En preparación) con su conteo.
// El color de cada uno es el del estado que representa, para que se lea
// igual que el chip de estado de la tarjeta.
@Composable
private fun SelectorEstadoCocina(
    seleccionado: EstadoPedido,
    conteoRecibidos: Int,
    conteoEnPreparacion: Int,
    onSeleccionar: (EstadoPedido) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BotonPestañaCocina(
            etiqueta = "Recibidos",
            cantidad = conteoRecibidos,
            color = EstadoRecibido,
            seleccionado = seleccionado == EstadoPedido.RECIBIDO,
            onClic = { onSeleccionar(EstadoPedido.RECIBIDO) },
            modifier = Modifier.weight(1f)
        )
        BotonPestañaCocina(
            etiqueta = "En preparación",
            cantidad = conteoEnPreparacion,
            color = EstadoEnPreparacion,
            seleccionado = seleccionado == EstadoPedido.EN_PREPARACION,
            onClic = { onSeleccionar(EstadoPedido.EN_PREPARACION) },
            modifier = Modifier.weight(1f)
        )
    }
}

// Botón individual de pestaña: resaltado cuando está activo.
@Composable
private fun BotonPestañaCocina(
    etiqueta: String,
    cantidad: Int,
    color: Color,
    seleccionado: Boolean,
    onClic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondo = color.copy(alpha = if (seleccionado) 0.40f else 0.15f)
    val borde = BorderStroke(if (seleccionado) 2.dp else 1.dp, color)
    Surface(
        onClick = onClic,
        shape = RoundedCornerShape(10.dp),
        color = fondo,
        border = borde,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = etiqueta,
                color = color,
                fontSize = 13.sp,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.SemiBold
            )
            Text(
                text = " $cantidad",
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =========================================================
// TARJETA DE COCINA: una card autosuficiente para el encargado (sin
// navegación al detalle): hora de entrega, número de pedido, el contenido
// de cada plato y el botón de acción de estado según el estado actual.
// =========================================================
@Composable
fun TarjetaPedidoCocina(
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
            // Hora de entrega destacada + botón de acción de estado.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pedido.horaRetiro,
                    color = JoJoNaranjo,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                BotonEstadoCocina(estado = pedido.estado, onCambio = onCambioEstado)
            }

            // Número de pedido (+ código de seguimiento si existe).
            Text(
                text = "Pedido #${pedido.id}" +
                    (pedido.codigoSeguimiento?.takeIf { it.isNotBlank() }
                        ?.let { " · $it" } ?: ""),
                color = JoJoTextoSecundario,
                fontSize = 12.sp
            )

            HorizontalDivider(color = JoJoBorde, thickness = 1.dp)

            // Contenido del pedido: ficha por plato (LOCAL) o líneas del
            // backend (API). Es lo que cocina necesita para preparar.
            if (pedido.platos.isNotEmpty()) {
                val mostrarRotulo = pedido.platos.size > 1
                pedido.platos.forEachIndexed { indice, plato ->
                    BloquePlatoCocina(
                        numero = indice + 1,
                        totalPlatos = pedido.platos.size,
                        mostrarRotulo = mostrarRotulo,
                        plato = plato
                    )
                }
            } else if (pedido.detalles.isEmpty()) {
                Text(
                    text = "Este pedido no trae detalle de productos.",
                    color = JoJoTextoSecundario,
                    fontSize = 13.sp
                )
            } else if (pedido.detalles.all { it.platoId != null }) {
                // Modo API con agrupación: cada plato en su bloque.
                val grupos = pedido.detalles
                    .groupBy { it.platoId!! }
                    .toList()
                    .sortedBy { (platoId, _) -> platoId }
                grupos.forEachIndexed { indice, (_, lineasDelPlato) ->
                    BloqueLineasCocina(
                        numero = indice + 1,
                        totalPlatos = grupos.size,
                        mostrarRotulo = grupos.size > 1,
                        lineas = lineasDelPlato
                    )
                }
            } else {
                // Backend sin platoId: líneas planas.
                BloqueLineasCocina(
                    numero = 1,
                    totalPlatos = 1,
                    mostrarRotulo = false,
                    lineas = pedido.detalles
                )
            }
        }
    }
}

// Botón de acción directa según el estado (la máquina de estados solo
// permite avanzar de a un paso, igual que el backend):
// - RECIBIDO -> "Iniciar preparación" (EN_PREPARACION).
// - EN_PREPARACION -> "Marcar listo para retiro" (LISTO_PARA_RETIRO).
// - LISTO_PARA_RETIRO en adelante: sin botón; se gestionan desde Cola.
@Composable
private fun BotonEstadoCocina(
    estado: EstadoPedido,
    onCambio: (EstadoPedido) -> Unit
) {
    val (etiqueta, destino) = when (estado) {
        EstadoPedido.RECIBIDO -> "Iniciar preparación" to EstadoPedido.EN_PREPARACION
        EstadoPedido.EN_PREPARACION -> "Marcar listo para retiro" to EstadoPedido.LISTO_PARA_RETIRO
        else -> return
    }
    Button(
        onClick = { onCambio(destino) },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorDeEstado(estado),
            contentColor = Color.Black
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = etiqueta, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

// Ficha compacta de UN plato armado (modo LOCAL): el rótulo "PLATO n DE N"
// solo aparece con 2 o más platos (con uno solo estorba más de lo que ayuda).
// Extras y Notas se dibujan únicamente si el plato los tiene.
@Composable
private fun BloquePlatoCocina(
    numero: Int,
    totalPlatos: Int,
    mostrarRotulo: Boolean,
    plato: PlatoArmado
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (mostrarRotulo) {
            Text(
                text = "PLATO $numero DE $totalPlatos",
                color = JoJoAcento,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        CampoPlatoCocina(
            titulo = "Proteína",
            valor = plato.proteina?.nombre ?: "Sin proteína"
        )
        CampoPlatoCocina(
            titulo = "Acomp.",
            valor = if (plato.acompanamientos.isEmpty()) {
                "Ninguno"
            } else {
                plato.acompanamientos.joinToString(", ") { it.nombre }
            }
        )
        CampoPlatoCocina(
            titulo = "Ensalada",
            valor = plato.ensalada?.nombre ?: "Sin ensalada"
        )
        if (plato.extras.isNotEmpty()) {
            CampoPlatoCocina(
                titulo = "Extras",
                valor = plato.extras.joinToString(", ") { it.nombre }
            )
        }
        if (plato.notas.isNotBlank()) {
            CampoPlatoCocina(
                titulo = "Notas",
                valor = plato.notas,
                esRestriccion = true
            )
        }
    }
}

// Bloque de líneas del backend (modo API) para un plato: "2× Arroz" con su
// precio; la observación (restricción del plato) se muestra una sola vez.
@Composable
private fun BloqueLineasCocina(
    numero: Int,
    totalPlatos: Int,
    mostrarRotulo: Boolean,
    lineas: List<LineaDetallePedido>
) {
    val restriccionDelPlato = lineas.firstNotNullOfOrNull { linea ->
        linea.observacion?.takeIf { it.isNotBlank() }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (mostrarRotulo) {
            Text(
                text = "PLATO $numero DE $totalPlatos",
                color = JoJoAcento,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        lineas.forEach { linea ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${linea.cantidad}× ${linea.nombreItem}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$${linea.precioUnitario.toLong()}",
                    color = JoJoNaranjo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (restriccionDelPlato != null) {
            CampoPlatoCocina(
                titulo = "Notas",
                valor = restriccionDelPlato,
                esRestriccion = true
            )
        }
    }
}

// Par etiqueta/valor dentro de la ficha de un plato.
@Composable
private fun CampoPlatoCocina(
    titulo: String,
    valor: String,
    esRestriccion: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            color = JoJoTextoSecundario,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.34f)
        )
        Text(
            text = valor,
            color = if (esRestriccion) JoJoTextoRestriccion else Color.White,
            fontSize = 14.sp,
            fontStyle = if (esRestriccion) FontStyle.Italic else FontStyle.Normal,
            modifier = Modifier.weight(0.66f)
        )
    }
}


