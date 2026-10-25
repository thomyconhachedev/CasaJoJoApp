package com.example.casajojoapp.ui.cliente.armado

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import com.example.casajojoapp.domain.model.Producto
import com.example.casajojoapp.ui.componentes.BannerContadorSeleccion
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.StepProgressBar
import com.example.casajojoapp.ui.theme.*

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
    // CAMBIO API: la lista de acompañamientos ya NO está hardcodeada; viene del
    // OrderViewModel (menú LOCAL o API según el interruptor del panel Admin).
    val uiState by viewModel.uiState.collectAsState()
    val acompanamientos: List<Producto> = uiState.acompanamientosDisponibles
    // Derivado del estado global: llega intacto al Resumen y al Carrito
    val idsSeleccionados: Set<Long> =
        uiState.platoEnArmadoActual.acompanamientos.map { it.id }.toSet()
    val MAX_SELECCIONES = 2 // Regla de negocio: SIEMPRE 2 acompañamientos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController, mostrarBotonCuenta = false)

        EncabezadoAcompanamientos()

        SeccionProgresoAcompanamientos()

        BannerContadorSeleccion(
            totalOpciones = acompanamientos.size,
            maxSelecciones = MAX_SELECCIONES,
            actualesSeleccionados = idsSeleccionados.size
        )

        // ANIMACIÓN (IE 2.2.2): crossfade entre cargando / error / lista.
        val estadoMenu = when {
            uiState.isLoadingMenu -> "cargando"
            uiState.menuError != null -> "error"
            else -> "lista"
        }
        Crossfade(
            targetState = estadoMenu,
            animationSpec = tween(300),
            label = "estadoMenu",
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { estado ->
            when (estado) {
                "cargando" -> Text(
                    text = "Cargando menú…",
                    color = JoJoTextoSecundario,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                "error" -> Text(
                    text = uiState.menuError ?: "",
                    color = JoJoRojoClaro,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                else -> ListaAcompanamientos(
                    lista = acompanamientos,
                    idsSeleccionados = idsSeleccionados,
                    maxSelecciones = MAX_SELECCIONES,
                    // CAMBIO MVVM: alterna en el estado global pasando el Producto
                    // de dominio DIRECTO; el VM respeta el límite de 2.
                    onToggle = { producto -> viewModel.alternarAcompanamiento(producto) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

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
            color = JoJoTextoSecundario,
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
            color = JoJoTextoClaro,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ListaAcompanamientos(
    lista: List<Producto>,
    idsSeleccionados: Set<Long>,
    maxSelecciones: Int,
    onToggle: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    val limiteAlcanzado = idsSeleccionados.size >= maxSelecciones

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lista.forEach { producto ->
            val estaSeleccionado = producto.id in idsSeleccionados

            // Al llegar a 2, los no seleccionados quedan deshabilitados
            val puedeSeleccionar = estaSeleccionado || !limiteAlcanzado

            AcompanamientoCard(
                acompanamiento = producto.aAcompanamientoVisual(),
                isSelected = estaSeleccionado,
                enabled = producto.disponible && puedeSeleccionar,
                onSelect = { onToggle(producto) }
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
            containerColor = if (isSelected) JoJoSeleccion else JoJoSuperficie
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) JoJoNaranjo else JoJoBorde
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
                        color = JoJoTextoSecundario,
                        fontSize = 12.sp
                    )
                }
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() },
                enabled = enabled,
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
fun BotonContinuarAcompanamientos(habilitado: Boolean, onClick: () -> Unit) {
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
            text = if (habilitado) "Continuar" else "Elige al menos 1 acompañamiento",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
