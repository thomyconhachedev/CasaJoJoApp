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
import androidx.compose.material3.HorizontalDivider
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
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.StepProgressBar

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
    // Listas hardcode temporales (no consumen GET /api/menu todavía)
    val listaPostres = listOf(
        PostreBebida("1", "Leche Asada", "Postre casero de la casa", 1200, "🍮", CategoriaItem.POSTRE),
        PostreBebida("2", "Kuchén", "Porción individual", 1500, "🍰", CategoriaItem.POSTRE),
        PostreBebida("3", "Helado", "Dos bochas a elección", 1300, "🍨", CategoriaItem.POSTRE)
    )
    val listaBebidas = listOf(
        PostreBebida("4", "Agua Mineral", "Botella 500 ml", 800, "💧", CategoriaItem.BEBIDA),
        PostreBebida("5", "Jugo Natural", "Naranja o limón", 1500, "🧃", CategoriaItem.BEBIDA),
        PostreBebida("6", "Gaseosa", "Lata 355 ml", 1200, "🥤", CategoriaItem.BEBIDA),
        PostreBebida("7", "Café", "Americano o con leche", 1000, "☕", CategoriaItem.BEBIDA)
    )

    val uiState by viewModel.uiState.collectAsState()
    // Extras seleccionados derivados del estado global (sin límite de cantidad)
    val idsSeleccionados: Set<Long> = uiState.platoEnArmadoActual.extras.map { it.id }.toSet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        EncabezadoPostresBebidas()

        SeccionProgresoPostresBebidas()

        // Lista combinada con encabezados de sección; scroll único
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "POSTRES",
                color = Color(0xFFFFAA75),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            listaPostres.forEach { item -> TarjetaPostreBebida(item, idsSeleccionados, viewModel) }

            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

            Text(
                text = "BEBIDAS",
                color = Color(0xFFFFAA75),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            listaBebidas.forEach { item -> TarjetaPostreBebida(item, idsSeleccionados, viewModel) }
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
            color = Color(0xFFC9BDB3),
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
            color = Color(0xFFFFF4EC),
            fontSize = 14.sp
        )
    }
}

// Tarjeta individual con checkbox y precio visible (suman al total)
@Composable
private fun TarjetaPostreBebida(
    item: PostreBebida,
    idsSeleccionados: Set<Long>,
    viewModel: OrderViewModel
) {
    val idLargo = item.id.toLongOrNull()
    val estaSeleccionado = idLargo != null && idLargo in idsSeleccionados

    Card(
        onClick = {
            if (estaSeleccionado) {
                viewModel.quitarExtra(item.toProducto())
            } else {
                viewModel.agregarExtra(item.toProducto())
            }
        },
        enabled = item.estaDisponible,
        colors = CardDefaults.cardColors(
            containerColor = if (estaSeleccionado) Color(0xFF2A1D17) else Color(0xFF1E1E1E)
        ),
        border = BorderStroke(
            width = if (estaSeleccionado) 2.dp else 1.dp,
            color = if (estaSeleccionado) Color(0xFFF2722C) else Color(0xFF333333)
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
                        color = Color(0xFFC9BDB3),
                        fontSize = 12.sp
                    )
                }
            }
            Checkbox(
                checked = estaSeleccionado,
                onCheckedChange = {
                    if (estaSeleccionado) viewModel.quitarExtra(item.toProducto())
                    else viewModel.agregarExtra(item.toProducto())
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFF2722C),
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
            containerColor = Color(0xFFF2722C),
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
