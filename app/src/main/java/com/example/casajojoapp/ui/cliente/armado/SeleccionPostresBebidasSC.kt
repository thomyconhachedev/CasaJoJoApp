package com.example.casajojoapp.ui.cliente.armado

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.Producto
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.StepProgressBar
import com.example.casajojoapp.ui.theme.*

// =========================================================
// CAMBIO FLUJO (pantalla NUEVA, paso 4 de 5):
// - Reemplaza a SeleccionAgregadosSC ("Agregados y Extras"):
//   los ÚNICOS extras del plato ahora son POSTRES y BEBIDAS.
// - Son OPCIONALES (se puede continuar sin elegir nada) y NO tienen
//   límite de selección (a diferencia de los acompañamientos).
// - SÍ muestran precio y SUMAN aparte al plato base de $4.800
//   (decisión de diseño: solo bebidas y postres son extras).
// - La selección vive en OrderViewModel.platoEnArmadoActual.extras
//   con categoría POSTRE o BEBIDA en cada Producto.
// =========================================================

// Modelo visual de postres/bebidas (con precio real en pesos)
data class PostreBebida(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val emoji: String,
    val categoria: CategoriaItem, // POSTRE o BEBIDA
    val estaDisponible: Boolean = true
)

@Composable
fun SeleccionPostresBebidasSC(navController: NavController, viewModel: OrderViewModel) {
    // CAMBIO API: los extras vienen del OrderViewModel (LOCAL o API). Son una
    // sola sección "EXTRAS" (postres y bebidas combinados, decisión G3).
    val uiState by viewModel.uiState.collectAsState()
    val extras: List<Producto> = uiState.extrasDisponibles
    // Extras seleccionados derivados del estado global (sin límite de cantidad)
    val idsSeleccionados: Set<Long> = uiState.platoEnArmadoActual.extras.map { it.id }.toSet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        EncabezadoPostresBebidas()

        SeccionProgresoPostresBebidas()

        when {
            uiState.isLoadingMenu -> Text(
                text = "Cargando menú…",
                color = JoJoTextoSecundario,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            uiState.menuError != null -> Text(
                text = uiState.menuError ?: "",
                color = JoJoRojoClaro,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            // Sección única con scroll; los extras suman adicional al plato base
            else -> Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "EXTRAS",
                    color = JoJoAcento,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                extras.forEach { producto ->
                    TarjetaPostreBebida(producto, idsSeleccionados, viewModel)
                }
            }
        }

        // Total de extras calculado en vivo desde el estado global
        val totalExtras = uiState.platoEnArmadoActual.extras.sumOf { it.precio }

        BotonContinuarPostresBebidas(
            totalExtras = totalExtras.toInt(),
            onClick = {
                navController.navigate("resumen_plato")
            }
        )
    }
}

@Composable
fun EncabezadoPostresBebidas() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Postres y Bebidas",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Puedes omitir este paso si no quieres extras",
            color = JoJoTextoSecundario,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SeccionProgresoPostresBebidas() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepProgressBar(totalSteps = 5, currentStep = 4)

        Text(
            text = "Opcional. Estos extras suman adicional a tu plato de $4.800.",
            color = JoJoTextoClaro,
            fontSize = 14.sp
        )
    }
}

// Tarjeta individual con checkbox y precio visible (suman al total)
@Composable
private fun TarjetaPostreBebida(
    producto: Producto,
    idsSeleccionados: Set<Long>,
    viewModel: OrderViewModel
) {
    val item = producto.aPostreBebidaVisual()
    val estaSeleccionado = producto.id in idsSeleccionados

    Card(
        onClick = {
            if (estaSeleccionado) {
                viewModel.quitarExtra(producto)
            } else {
                viewModel.agregarExtra(producto)
            }
        },
        enabled = producto.disponible,
        colors = CardDefaults.cardColors(
            containerColor = if (estaSeleccionado) JoJoSeleccion else JoJoSuperficie
        ),
        border = BorderStroke(
            width = if (estaSeleccionado) 2.dp else 1.dp,
            color = if (estaSeleccionado) JoJoNaranjo else JoJoBorde
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.emoji,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = item.nombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    // CAMBIO PRECIOS: los extras son los únicos con precio visible
                    Text(
                        text = "+$${item.precio} • ${item.descripcion}",
                        color = JoJoTextoSecundario,
                        fontSize = 12.sp
                    )
                }
            }
            Checkbox(
                checked = estaSeleccionado,
                onCheckedChange = {
                    if (estaSeleccionado) viewModel.quitarExtra(producto)
                    else viewModel.agregarExtra(producto)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = JoJoNaranjo,
                    checkmarkColor = Color.Black,
                    uncheckedColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun BotonContinuarPostresBebidas(totalExtras: Int, onClick: () -> Unit) {
    val textoBoton = if (totalExtras > 0) "Continuar (+$${totalExtras})" else "Omitir / Continuar"

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = JoJoNaranjo,
            contentColor = Color.Black
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = textoBoton,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
