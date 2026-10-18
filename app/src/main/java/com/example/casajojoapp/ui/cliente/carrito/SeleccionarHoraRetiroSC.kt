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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.componentes.HeaderPrincipal

// =========================================================
// CAMBIO HORARIO (pantalla NUEVA):
// - Selector de hora de retiro con slots cada 10 minutos
//   desde 12:00 hasta 16:00 (25 opciones, ambos inclusive).
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
            .background(Color(0xFF121212))
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
                text = "Disponibilidad cada 10 minutos entre 12:00 y 16:00",
                color = Color(0xFFC9BDB3),
                fontSize = 14.sp
            )
        }

        // Grid scrollable de horas (4 por fila)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // CAMBIO HORARIO: 12:00, 12:10, ... 15:50, 16:00 (25 slots)
            items(generarHorasRetiro()) { hora ->
                ChipHora(
                    hora = hora,
                    isSelected = hora == horaSeleccionada,
                    onSelect = { viewModel.actualizarHoraRetiro(hora) }
                )
            }
        }

        BotonContinuarHora(
            horaSeleccionada = horaSeleccionada,
            onClick = {
                // popUpTo evita apilar carrito si venimos desde el carrito
                // (carrito -> hora -> carrito); si venimos del resumen se
                // comporta como un navigate normal.
                navController.navigate("carritoSC") {
                    popUpTo("carritoSC") { inclusive = true }
                }
            }
        )
    }
}

// Genera los horarios "12:00" .. "16:00" cada 10 minutos (24h, formato fijo)
private fun generarHorasRetiro(): List<String> {
    val totalMinutos = 4 * 60 // de 12:00 a 16:00 = 240 minutos
    return buildList {
        for (minuto in 0..totalMinutos step 10) {
            val hora = (12 + minuto / 60).toString().padStart(2, '0')
            val minutos = (minuto % 60).toString().padStart(2, '0')
            add("$hora:$minutos")
        }
    }
}

@Composable
private fun ChipHora(hora: String, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2A1D17) else Color(0xFF1E1E1E)
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFFF2722C) else Color(0xFF333333)
        ),
        modifier = Modifier.aspectRatio(1.6f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = hora,
                color = if (isSelected) Color(0xFFFFAA75) else Color.White,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BotonContinuarHora(horaSeleccionada: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF2722C),
            contentColor = Color.Black
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Confirmar hora ($horaSeleccionada)",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
