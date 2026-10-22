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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.StepProgressBar
import com.example.casajojoapp.ui.theme.*

// Modelo para Ensaladas
data class Ensalada(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val emoji: String,
    val estaDisponible: Boolean = true
)

// 1. PANTALLA PRINCIPAL
// CAMBIO MVVM (sección 3): firma ampliada con viewModel: OrderViewModel.
// CAMBIO API: las ensaladas reales vienen del OrderViewModel (LOCAL o API);
// "Sin Ensalada" es una opción especial de UI (id "0") que se guarda como null.
@Composable
fun SeleccionEnsaladaSC(navController: NavController, viewModel: OrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val ensaladasReales: List<Producto> = uiState.ensaladasDisponibles
    // CAMBIO MVVM: null equivale a no tener ensalada en el plato ("Sin Ensalada").
    val selectedId: String? = uiState.platoEnArmadoActual.ensalada?.id?.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        EncabezadoEnsaladas()

        SeccionProgresoEnsaladas()

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
            else -> ListaEnsaladas(
                ensaladasReales = ensaladasReales,
                selectedId = selectedId,
                // CAMBIO MVVM: persiste la ensalada (o null para "Sin Ensalada")
                // en el estado global, pasando el Producto de dominio DIRECTO.
                onEnsaladaSeleccionada = { producto -> viewModel.seleccionarEnsalada(producto) },
                modifier = Modifier.weight(1f)
            )
        }

        BotonContinuarEnsalada(
            // selectedId null == plato sin ensalada == opción "Sin Ensalada"
            sinEnsalada = (selectedId == null),
            onClick = {
                // CAMBIO FLUJO: el paso 4 ahora es Postres y Bebidas (opcional);
                // el Resumen queda como paso 5 final.
                navController.navigate("postresBebidasSC")
            }
        )
    }
}

// 2. ENCABEZADO DE PANTALLA
@Composable
fun EncabezadoEnsaladas() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Elige tu Ensalada",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Fresco y saludable para acompañar",
            color = JoJoTextoSecundario,
            fontSize = 14.sp
        )
    }
}

// 3. BARRA DE PROGRESO (Paso 3 de 5)
@Composable
fun SeccionProgresoEnsaladas() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepProgressBar(totalSteps = 5, currentStep = 3)

        Text(
            text = "Selecciona 1 opción de ensalada incluida en tu plato.",
            color = JoJoTextoClaro,
            fontSize = 14.sp
        )
    }
}

// 4. LISTA DE CARDS (ensaladas reales del menú + opción "Sin Ensalada")
@Composable
fun ListaEnsaladas(
    ensaladasReales: List<Producto>,
    selectedId: String?, // CAMBIO MVVM: nullable porque deriva del OrderViewModel (null = sin ensalada)
    onEnsaladaSeleccionada: (Producto?) -> Unit, // null = "Sin Ensalada"
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Opción especial "Sin Ensalada" (id "0"): se marca seleccionada cuando
        // el plato no tiene ensalada (selectedId == null).
        EnsaladaCard(
            ensalada = Ensalada(
                id = "0",
                nombre = "Sin Ensalada",
                descripcion = "Prefiero no agregar ensalada a mi pedido",
                emoji = "🚫"
            ),
            isSelected = (selectedId == null),
            onSelect = { onEnsaladaSeleccionada(null) }
        )

        ensaladasReales.forEach { producto ->
            EnsaladaCard(
                ensalada = producto.aEnsaladaVisual(),
                isSelected = (producto.id.toString() == selectedId),
                onSelect = { onEnsaladaSeleccionada(producto) }
            )
        }
    }
}

// 5. TARJETA INDIVIDUAL CON SELECCIÓN ÚNICA
@Composable
fun EnsaladaCard(
    ensalada: Ensalada,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        enabled = ensalada.estaDisponible,
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
                Text(
                    text = ensalada.emoji,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = ensalada.nombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = ensalada.descripcion,
                        color = JoJoTextoSecundario,
                        fontSize = 12.sp
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = JoJoNaranjo,
                    unselectedColor = Color.Gray
                )
            )
        }
    }
}

// 6. BOTÓN CONTINUAR
@Composable
fun BotonContinuarEnsalada(sinEnsalada: Boolean, onClick: () -> Unit) {
    val textoBoton = if (sinEnsalada) "Continuar sin ensalada" else "Continuar"

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