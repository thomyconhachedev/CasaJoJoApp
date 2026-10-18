package com.example.casajojoapp.pedido.cliente.armado

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.StepProgressBar

// Modelo para Ensaladas
data class Ensalada(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val emoji: String,
    val estaDisponible: Boolean = true
)

// 1. PANTALLA PRINCIPAL
@Composable
fun SeleccionEnsaladaSC(navController: NavController) {
    val listaEnsaladas = listOf(
        Ensalada("1", "Ensalada Chilena", "Tomate fresco, cebolla pluma y cilantro", "🥗"),
        Ensalada("2", "Ensalada Mixta", "Lechuga, tomate y choclo", "🥬"),
        Ensalada("3", "Ensalada de Apio", "Apio crujiente con palta", "🥑"),
        Ensalada("0", "Sin Ensalada", "Prefiero no agregar ensalada a mi pedido", "🚫")
    )

    // Por defecto seleccionamos la primera opción (Ensalada Chilena)
    var selectedId by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        EncabezadoEnsaladas()

        SeccionProgresoEnsaladas()

        ListaEnsaladas(
            lista = listaEnsaladas,
            selectedId = selectedId,
            onEnsaladaSeleccionada = { selectedId = it },
            modifier = Modifier.weight(1f)
        )

        BotonContinuarEnsalada(
            sinEnsalada = (selectedId == "0"),
            onClick = {
                // Paso 4: Navegar a Bebidas/Postres o Resumen del Pedido
                navController.navigate("resumen_plato")
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
            color = Color(0xFFC9BDB3),
            fontSize = 14.sp
        )
    }
}

// 3. BARRA DE PROGRESO (Paso 3 de 4)
@Composable
fun SeccionProgresoEnsaladas() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepProgressBar(totalSteps = 4, currentStep = 3)

        Text(
            text = "Selecciona 1 opción de ensalada incluida en tu combo.",
            color = Color(0xFFFFF4EC),
            fontSize = 14.sp
        )
    }
}

// 4. LISTA DE CARDS
@Composable
fun ListaEnsaladas(
    lista: List<Ensalada>,
    selectedId: String,
    onEnsaladaSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lista.forEach { ensalada ->
            EnsaladaCard(
                ensalada = ensalada,
                isSelected = (ensalada.id == selectedId),
                onSelect = { onEnsaladaSeleccionada(ensalada.id) }
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
                        color = Color(0xFFC9BDB3),
                        fontSize = 12.sp
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFFF2722C),
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