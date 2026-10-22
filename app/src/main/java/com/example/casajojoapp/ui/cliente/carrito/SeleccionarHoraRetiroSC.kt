package com.example.casajojoapp.ui.cliente.carrito

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.theme.*

// =========================================================
// CAMBIO HORARIO (pantalla NUEVA):
// - Selector de hora de retiro con slots cada 10 minutos
//   desde 12:00 hasta 18:00 (TEMPORAL: antes 16:00, extendido a 18:00 solo
//   para pruebas; ver TOTAL_MINUTOS).
// - El valor se guarda en OrderUiState.horaRetiroSeleccionada a través
//   de OrderViewModel.actualizarHoraRetiro() (antes era un "13:00" fijo
//   sin UI para cambiarlo).
// - Flujo: Resumen ("Ir al carrito") -> ESTA PANTALLA -> Carrito.
//   Desde el Carrito también se puede volver a elegir (fila de hora).
// =========================================================

@Composable
fun SeleccionarHoraRetiroSC(navController: NavController, viewModel: OrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val horaSeleccionada = uiState.horaRetiroSeleccionada

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
                text = "Elige tu hora de retiro",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Disponibilidad cada 10 minutos entre 12:00 y 22:00",
                color = JoJoTextoSecundario,
                fontSize = 14.sp
            )
        }

        // Grid scrollable de horas (4 por fila). Solo se ofrecen las que
        // respetan la regla del backend: al menos 20 minutos de anticipación.
        val horasDisponibles = generarHorasRetiro()

        if (horasDisponibles.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ya no quedan horas de retiro disponibles hoy.\nEl menú se reinicia cada día.",
                    color = JoJoTextoSecundario,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(horasDisponibles) { hora ->
                    ChipHora(
                        hora = hora,
                        isSelected = hora == horaSeleccionada,
                        onSelect = { viewModel.actualizarHoraRetiro(hora) }
                    )
                }
            }
        }

        BotonContinuarHora(
            horaSeleccionada = horaSeleccionada,
            habilitado = horaSeleccionada in horasDisponibles,
            onClick = {
                // FIX NAV: el plato se commitea AQUÍ (al confirmar la hora), no
                // en el resumen. El guard de agregarPlatoAlCarrito() (platoCompleto)
                // evita duplicados cuando venimos del carrito (platoEnArmadoActual
                // ya vacío tras el primer commit).
                viewModel.agregarPlatoAlCarrito()
                // popUpTo pantalla_dos limpia el flujo de armado del stack: el
                // carrito queda como destino limpio y "atrás" desde el carrito va
                // al home, no a pasos obsoletos ni al resumen (que quedaría vacío).
                navController.navigate("carritoSC") {
                    popUpTo("pantalla_dos")
                }
            }
        )
    }
}

// Horarios "12:00" .. "22:00" cada 10 minutos, descartando los que el
// backend rechazaría: exige al menos 20 minutos de anticipación.
private const val ANTICIPACION_MINIMA_MINUTOS = 20

// TEMPORAL PARA PRUEBAS: 10 horas (12:00 -> 22:00). Volver a 6 para 18:00.
private const val TOTAL_HORAS = 10

private fun generarHorasRetiro(): List<String> {
    val totalMinutos = TOTAL_HORAS * 60 // de 12:00 a 22:00 = 600 minutos
    val horaMinima = java.time.LocalTime.now().plusMinutes(ANTICIPACION_MINIMA_MINUTOS.toLong())
    return buildList {
        for (minuto in 0..totalMinutos step 10) {
            val hora = (12 + minuto / 60).toString().padStart(2, '0')
            val minutos = (minuto % 60).toString().padStart(2, '0')
            val slot = "$hora:$minutos"
            if (!java.time.LocalTime.parse(slot).isBefore(horaMinima)) add(slot)
        }
    }
}

@Composable
private fun ChipHora(hora: String, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) JoJoSeleccion else JoJoSuperficie
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) JoJoNaranjo else JoJoBorde
        ),
        modifier = Modifier.aspectRatio(1.6f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = hora,
                color = if (isSelected) JoJoAcento else Color.White,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BotonContinuarHora(horaSeleccionada: String, habilitado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        colors = ButtonDefaults.buttonColors(
            containerColor = JoJoNaranjo,
            contentColor = Color.Black,
            disabledContainerColor = JoJoDeshabilitado,
            disabledContentColor = JoJoGrisMedio
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (habilitado) {
                "Confirmar hora ($horaSeleccionada)"
            } else {
                "Elige una hora disponible"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}