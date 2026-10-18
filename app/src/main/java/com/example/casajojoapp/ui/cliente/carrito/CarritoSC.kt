package com.example.casajojoapp.ui.cliente.carrito

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.componentes.HeaderPrincipal

// =========================================================
// CAMBIO MVVM (prompt sección 4.B):
// - Antes era un stub que solo mostraba el Header (y su función se llamaba
//   carritoSC sin usar el ViewModel).
// - Ahora recibe "viewModel: OrderViewModel", colecta uiState con
//   collectAsState() y consume uiState.carritoPlatos,
//   horaRetiroSeleccionada, totalCarrito, isLoading y errorMessage.
// - LazyColumn con los platos del carrito + estado "carrito vacío".
// - CAMBIO HORARIO: la fila de hora es clicable -> SeleccionarHoraRetiroSC.
// - CAMBIO RESTRICCIONES: cada plato muestra sus propias notas
//   (PlatoArmado.notas); ya NO hay campo de notas global en el carrito.
// - Botón "Confirmar Pedido" -> viewModel.enviarPedido(), deshabilitado
//   si el carrito está vacío o si isLoading es true.
// =========================================================

@Composable
fun carritoSC(navController: NavController, viewModel: OrderViewModel) {
    // CAMBIO MVVM: estado compartido colectado desde el OrderViewModel
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Confirma tu Pedido",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Revisa los platos antes de enviarlo a cocina",
                color = Color(0xFFC9BDB3),
                fontSize = 14.sp
            )
        }

        // LISTA DE PLATOS: LazyColumn con los platos del carrito
        if (uiState.carritoPlatos.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tu carrito está vacío.\nArma tu combo desde el menú.",
                    color = Color(0xFFC9BDB3),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                // CAMBIO MVVM: se recorre uiState.carritoPlatos (antes no había nada)
                items(uiState.carritoPlatos) { plato ->
                    TarjetaPlatoCarrito(plato = plato)
                }
            }
        }

        // CAMBIO RESTRICCIONES: el campo de notas generales se ELIMINÓ del
        // carrito: ahora cada plato trae sus propias restricciones, escritas
        // en ResumenPlatoScreen y guardadas en PlatoArmado.notas.

        // CAMBIO HORARIO: la hora de retiro es editable; al tocarla se abre
        // el selector con slots cada 10 minutos (12:00 a 16:00).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("horaRetiroSC") },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Hora de retiro", color = Color(0xFFC9BDB3), fontSize = 14.sp)
            Text(
                text = "${uiState.horaRetiroSeleccionada}  ✏️",
                color = Color(0xFFF2722C),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // CAMBIO MVVM: error devuelto por el ViewModel (futuro POST /api/pedidos)
        uiState.errorMessage?.let { mensaje ->
            Text(text = mensaje, color = Color(0xFFE88B8A), fontSize = 13.sp)
        }

        HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

        // BOTÓN FINAL: costo total acumulado + enviarPedido()
        Button(
            onClick = { viewModel.enviarPedido() },
            // CAMBIO MVVM: deshabilitado con carrito vacío o durante la carga
            enabled = uiState.carritoPlatos.isNotEmpty() && !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF2722C),
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF382C24),
                disabledContentColor = Color(0xFF9C9A92)
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                // No se usa la palabra "Pago" (alcance Sprint 4)
                text = if (uiState.isLoading) {
                    "Enviando pedido..."
                } else {
                    "Confirmar Pedido ($${uiState.totalCarrito.toLong()})"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// TARJETA DE CADA PLATO DENTRO DEL CARRITO
// CAMBIO MVVM: componente nuevo; muestra el desglose de un PlatoArmado.
@Composable
private fun TarjetaPlatoCarrito(plato: PlatoArmado) {
    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF333333)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plato.proteina?.nombre ?: "Combo sin proteína",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${plato.precioTotal.toLong()}",
                    color = Color(0xFFF2722C),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Ensalada: ${plato.ensalada?.nombre ?: "Sin ensalada"}",
                color = Color(0xFFC9BDB3),
                fontSize = 13.sp
            )

            if (plato.acompanamientos.isNotEmpty()) {
                Text(
                    text = plato.acompanamientos.joinToString(", ") { it.nombre },
                    color = Color(0xFFC9BDB3),
                    fontSize = 13.sp
                )
            }

            if (plato.extras.isNotEmpty()) {
                // CAMBIO PRECIOS: los extras (postres/bebidas) son los únicos
                // con precio propio; se muestra el aporte individual de cada uno
                Text(
                    text = "Extras: " + plato.extras.joinToString(", ") {
                        "${it.nombre} (+$${it.precio.toLong()})"
                    },
                    color = Color(0xFFFFAA75),
                    fontSize = 13.sp
                )
            }

            // CAMBIO RESTRICCIONES: cada plato arrastra sus propias restricciones
            if (plato.notas.isNotBlank()) {
                Text(
                    text = "Restricciones: ${plato.notas}",
                    color = Color(0xFFE8D9CF),
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
