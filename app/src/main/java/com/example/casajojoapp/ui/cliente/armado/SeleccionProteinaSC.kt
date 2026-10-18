package com.example.casajojoapp.ui.cliente.armado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.ui.componentes.Proteina
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.ProteinaCard
import com.example.casajojoapp.ui.componentes.StepProgressBar

val burgerEmoji = "\uD83C\uDF54"
val salmonEmoji = "\uD83D\uDC1F"

// 1. PANTALLA PRINCIPAL (Contenedor estructurado en módulos)
// CAMBIO MVVM (sección 3): firma ampliada con viewModel: OrderViewModel.
@Composable
fun SeleccionProteinaSC(navController: NavController, viewModel: OrderViewModel) {
    val listaProteinas = listOf(
        Proteina("1", "Carne Mechada", "Vacuno cocción lenta 4 hrs", "🥩"),
        Proteina("2", "Pollo al Horno", "Muslo marinado al limón y romero", "🍗"),
        Proteina("3", "Salmon cocido", "Filete cocido con tomate y verduras", salmonEmoji),
        Proteina("4", "Hamburguesa", "Hamburguesa dorada en mantequilla", burgerEmoji, estaDisponible = false),
        Proteina("5", "Hamburguesa", "Hamburguesa dorada en mantequilla", burgerEmoji, estaDisponible = false),
        Proteina("6", "Pescado Frito", "Reineta fresca del día", "🐟", estaDisponible = false)
    )

    // CAMBIO MVVM: se REEMPLAZÓ el "var selectedId by remember { mutableStateOf("1") }"
    // por estado compartido. selectedId ahora se DERIVA del OrderViewModel, así al
    // volver atrás la selección sigue siendo la que el usuario eligió (antes se
    // reseteaba a "1" en cada recomposición de la ruta). null = sin elegir aún.
    val uiState by viewModel.uiState.collectAsState()
    val selectedId: String? = uiState.platoEnArmadoActual.proteina?.id?.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        EncabezadoSeleccion()

        SeccionProgreso()

        ListaProteinas(
            listaProteinas = listaProteinas,
            selectedId = selectedId,
            onProteinaSeleccionada = { id ->
                // CAMBIO MVVM: la selección se persiste en el estado global
                // (mapeando el modelo visual Proteina al modelo de dominio Producto).
                listaProteinas.firstOrNull { it.id == id }
                    ?.let { viewModel.seleccionarProteina(it.toProducto()) }
            },
            modifier = Modifier.weight(1f)
        )

        BotonContinuar(
            // CAMBIO FLUJO: la proteína es obligatoria (1 por plato),
            // sin ella no se puede avanzar al paso de acompañamientos.
            habilitado = uiState.platoEnArmadoActual.proteina != null,
            onClick = {
                // CAMBIO FLUJO: el paso 2 ahora es Acompañamientos (antes Agregados)
                navController.navigate("seleccionAcompanamientosSC")
            }
        )
    }
}

// 2. SECCIÓN: Títulos principales de la pantalla
@Composable
fun EncabezadoSeleccion() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Elige tu proteína",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Un almuerzo preparado con cariño",
            color = Color(0xFFC9BDB3),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// 3. SECCIÓN: Barra de progreso e instrucciones del paso
@Composable
fun SeccionProgreso() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepProgressBar(totalSteps = 5, currentStep = 1)

        Text(
            // CAMBIO PRECIOS: el plato cuesta $4.800 e incluye
            // 2 acompañamientos + ensalada (sin precio por ítem)
            text = "Elige 1 proteína. Tu plato cuesta $4.800 e incluye 2 acompañamientos y ensalada.",
            color = Color(0xFFFFF4EC),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// 4. SECCIÓN: Lista dinámicas de tarjetas de proteína
@Composable
fun ListaProteinas(
    listaProteinas: List<Proteina>,
    selectedId: String?, // CAMBIO MVVM: ahora es nullable porque deriva del OrderViewModel
    onProteinaSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listaProteinas.forEach { proteina ->
            ProteinaCard(
                proteina = proteina,
                isSelected = (proteina.id == selectedId),
                onSelect = { onProteinaSeleccionada(proteina.id) }
            )
        }
    }
}

// 5. SECCIÓN: Botón principal de acción
// CAMBIO FLUJO: gana "habilitado" para bloquear el avance sin proteína
@Composable
fun BotonContinuar(habilitado: Boolean, onClick: () -> Unit) {
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
            text = if (habilitado) "Continuar" else "Elige una proteína",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}