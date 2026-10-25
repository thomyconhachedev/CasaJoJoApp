package com.example.casajojoapp.ui.cliente.carrito

import android.app.TimePickerDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.theme.*
import com.example.casajojoapp.util.HorarioRetiro

// =========================================================
// CAMBIO HORARIO (pantalla NUEVA):
// - Selector de hora de retiro con slots cada 10 minutos generados por
//   util/HorarioRetiro (única fuente de verdad del rango; hoy 12:00–02:00
//   TEMPORAL para pruebas — cambiar HORAS_VENTANA de 14 a 4 el día de la
//   demo para que quede 12:00–16:00).
// - El valor se guarda en OrderUiState.horaRetiroSeleccionada a través
//   de OrderViewModel.actualizarHoraRetiro().
// - Flujo: Resumen ("Ir al carrito") -> ESTA PANTALLA -> Carrito.
//   Desde el Carrito también se puede volver a elegir (fila de hora).
// =========================================================

@Composable
fun SeleccionarHoraRetiroSC(navController: NavController, viewModel: OrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val horaSeleccionada = uiState.horaRetiroSeleccionada

    // Error de la última hora personalizada elegida (TimePicker nativo).
    var errorHora by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController, mostrarBotonCuenta = false)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Elige tu hora de retiro",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Disponibilidad cada 10 minutos entre ${HorarioRetiro.rangoVisible()}",
                color = JoJoTextoSecundario,
                fontSize = 14.sp
            )
        }

        // Grid scrollable de horas (4 por fila). Solo se ofrecen las que
        // respetan la regla del backend: al menos 20 minutos de anticipación.
        val horasDisponibles = HorarioRetiro.generar()

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

        // RECURSO NATIVO (rubrica IE 2.4.1): TimePicker del sistema para
        // elegir una hora exacta fuera de la grilla de 10 minutos. La
        // validación vive en HorarioRetiro (desacoplada de la UI).
        val contexto = LocalContext.current
        OutlinedButton(
            onClick = {
                val ahora = java.time.LocalTime.now()
                TimePickerDialog(
                    contexto,
                    { _, horaElegida, minutoElegido ->
                        val elegida = "%02d:%02d".format(horaElegida, minutoElegido)
                        val error = HorarioRetiro.validarHoraPersonalizada(elegida)
                        errorHora = error
                        if (error == null) {
                            viewModel.actualizarHoraRetiro(elegida)
                        }
                    },
                    ahora.hour,
                    ahora.minute,
                    true // formato 24 hrs
                ).show()
            },
            border = BorderStroke(1.dp, JoJoNaranjo),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = JoJoNaranjo),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🕐 Elegir hora personalizada",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        errorHora?.let { mensaje ->
            Text(text = mensaje, color = JoJoRojoClaro, fontSize = 13.sp)
        }

        BotonContinuarHora(
            horaSeleccionada = horaSeleccionada,
            // Válida si viene de la grilla O si pasó la validación de la
            // hora personalizada elegida con el TimePicker nativo.
            habilitado = horaSeleccionada in horasDisponibles ||
                HorarioRetiro.validarHoraPersonalizada(horaSeleccionada) == null,
            onClick = {
                // FIX NAV: el plato se commitea AQUÍ (al confirmar la hora), no
                // en el resumen. El guard de agregarPlatoAlCarrito() (platoCompleto)
                // evita duplicados cuando venimos del carrito (platoEnArmadoActual
                // ya vacío tras el primer commit).
                viewModel.agregarPlatoAlCarrito()
                // popUpTo(home) limpia el flujo de armado del stack: el carrito
                // queda como destino limpio y "atrás" desde el carrito va al home.
                // (Antes usaba popUpTo("pantalla_dos"), ruta que nunca está en el
                // stack desde el home, así que no limpiaba nada.)
                navController.navigate("carritoSC") {
                    popUpTo("home")
                    launchSingleTop = true
                }
            }
        )
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