package com.example.casajojoapp.pedido.cliente.armado

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.Proteina
import com.example.casajojoapp.ui.componentes.ProteinaCard
import com.example.casajojoapp.ui.componentes.StepProgressBar

val burgerEmoji = "\uD83C\uDF54"
val salmonEmoji = "\uD83D\uDC1F"

// 1. PANTALLA PRINCIPAL (Contenedor estructurado en módulos)
@Composable
fun SeleccionProteinaSC(navController: NavController) {
    val listaProteinas = listOf(
        Proteina("1", "Carne Mechada", "Vacuno cocción lenta 4 hrs", "🥩"),
        Proteina("2", "Pollo al Horno", "Muslo marinado al limón y romero", "🍗"),
        Proteina("3", "Salmon cocido", "Filete cocido con tomate y verduras", salmonEmoji),
        Proteina("4", "Hamburguesa", "Hamburguesa dorada en mantequilla", burgerEmoji, estaDisponible = false),
        Proteina("5", "Hamburguesa", "Hamburguesa dorada en mantequilla", burgerEmoji, estaDisponible = false),
        Proteina("6", "Pescado Frito", "Reineta fresca del día", "🐟", estaDisponible = false)
    )
    var selectedId by remember { mutableStateOf("1") }

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
            onProteinaSeleccionada = { selectedId = it },
            modifier = Modifier.weight(1f)
        )

        BotonContinuar(
            onClick = {
                // Le dices a Jetpack Compose que navegue a la ruta de agregados
                navController.navigate("seleccionAgregadosSC")
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
        StepProgressBar(totalSteps = 4, currentStep = 1)

        Text(
            text = "Elige 1 proteína. El precio del combo incluye dos acompañamientos.",
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
    selectedId: String,
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
@Composable
fun BotonContinuar(onClick: () -> Unit) {
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
            text = "Continuar",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}