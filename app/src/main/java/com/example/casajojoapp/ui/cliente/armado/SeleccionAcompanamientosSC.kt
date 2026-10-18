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
import com.example.casajojoapp.ui.componentes.BannerContadorSeleccion
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.StepProgressBar

// =========================================================
// CAMBIO FLUJO (pantalla NUEVA, paso 2 de 5):
// - Reemplaza a la pantalla de Agregados en el orden del flujo:
//   Proteína -> ACOMPANAMIENTOS -> Ensalada -> Postres/Bebidas -> Resumen.
// - El plato SIEMPRE lleva AL MENOS 1 acompañamiento (máximo 2,
//   próximamente habrá pedidos especiales con otras reglas) y NO
//   muestran precio: están incluidos en el plato base de $4.800
//   (ver PlatoArmado.PRECIO_BASE).
// - La selección vive en OrderViewModel.platoEnArmadoActual.acompanamientos
//   y sobrevive a la navegación hacia atrás.
// =========================================================

// Modelo visual de la lista de acompañamientos (sin precio: ver arriba)
data class Acompanamiento(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val emoji: String,
    val estaDisponible: Boolean = true
)

@Composable
fun SeleccionAcompanamientosSC(navController: NavController, viewModel: OrderViewModel) {
    // Lista hardcode temporal (no consume GET /api/menu todavía)
    val listaAcompanamientos = listOf(
        Acompanamiento("1", "Papas Fritas", "Crujientes y doradas", "🍟"),
        Acompanamiento("2", "Arroz Blanco", "Al estilo casero", "🍚"),
        Acompanamiento("3", "Ensalada de Lechuga", "Lechuga fresca con limón", "🥬"),
        Acompanamiento("4", "Puré de Papas", "Cremoso con mantequilla", "🥔"),
        Acompanamiento("5", "Fideos al Ajillo", "Ajo y perejil", "🍝"),
        Acompanamiento("6", "Quinoa", "Mezcla del día", "🌾", estaDisponible = false)
    )

    val uiState by viewModel.uiState.collectAsState()
    // Derivado del estado global: llega intacto al Resumen y al Carrito
    val idsSeleccionados: Set<Long> =
        uiState.platoEnArmadoActual.acompanamientos.map { it.id }.toSet()
    val MAX_SELECCIONES = 2 // Regla de negocio: SIEMPRE 2 acompañamientos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        EncabezadoAcompanamientos()

        SeccionProgresoAcompanamientos()

        BannerContadorSeleccion(
            totalOpciones = listaAcompanamientos.size,
            maxSelecciones = MAX_SELECCIONES,
            actualesSeleccionados = idsSeleccionados.size
        )

        ListaAcompanamientos(
            lista = listaAcompanamientos,
            idsSeleccionados = idsSeleccionados,
            maxSelecciones = MAX_SELECCIONES,
            onToggle = { id ->
                val idLargo = id.toLongOrNull()
                val item = listaAcompanamientos.firstOrNull { it.id == id }
                if (item != null && idLargo != null) {
                    // CAMBIO MVVM: alterna en el estado global; el VM respeta el límite de 2
                    viewModel.alternarAcompanamiento(item.toProducto())
                }
            },
            modifier = Modifier.weight(1f)
        )

        BotonContinuarAcompanamientos(
            // CAMBIO: ahora es MÍNIMO 1 acompañamiento (máximo 2);
            // próximamente habrá pedidos especiales con otras reglas.
            habilitado = idsSeleccionados.size >= 1,
            onClick = {
                navController.navigate("seleccionEnsaladasSC")
            }
        )
    }
}

@Composable
fun EncabezadoAcompanamientos() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Elige 2 Acompañamientos",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Incluidos en tu plato de $4.800",
            color = Color(0xFFC9BDB3),
            fontSize = 14.sp
        )
    }
}

@Composable
fun SeccionProgresoAcompanamientos() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepProgressBar(totalSteps = 5, currentStep = 2)

        Text(
            text = "Obligatorio. Elige al menos 1 acompañamiento (hasta 2).",
            color = Color(0xFFFFF4EC),
            fontSize = 14.sp
        )
    }
}

@Composable
fun ListaAcompanamientos(
    lista: List<Acompanamiento>,
    idsSeleccionados: Set<Long>,
    maxSelecciones: Int,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val limiteAlcanzado = idsSeleccionados.size >= maxSelecciones

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lista.forEach { item ->
            val idLargo = item.id.toLongOrNull()
            val estaSeleccionado = idLargo != null && idLargo in idsSeleccionados

            // Al llegar a 2, los no seleccionados quedan deshabilitados
            val puedeSeleccionar = estaSeleccionado || !limiteAlcanzado

            AcompanamientoCard(
                acompanamiento = item,
                isSelected = estaSeleccionado,
                enabled = item.estaDisponible && puedeSeleccionar,
                onSelect = { onToggle(item.id) }
            )
        }
    }
}

@Composable
fun AcompanamientoCard(
    acompanamiento: Acompanamiento,
    isSelected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean
) {
    Card(
        onClick = onSelect,
        enabled = enabled, // respeta disponibilidad y límite de 2
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2A1D17) else Color(0xFF1E1E1E)
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFFF2722C) else Color(0xFF333333)
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
                // CAMBIO PRECIOS: sin "+$..." en la descripción: van incluidos
                Text(
                    text = acompanamiento.emoji,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = acompanamiento.nombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = acompanamiento.descripcion,
                        color = Color(0xFFC9BDB3),
                        fontSize = 12.sp
                    )
                }
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() },
                enabled = enabled,
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
fun BotonContinuarAcompanamientos(habilitado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
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
            text = if (habilitado) "Continuar" else "Elige al menos 1 acompañamiento",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
