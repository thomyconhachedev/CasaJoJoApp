package com.example.casajojoapp.pedido.cliente.armado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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

// Modelo para Agregados / Extras
data class Agregado(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val emoji: String,
    val estaDisponible: Boolean = true
)

@Composable
fun SeleccionAgregadosSC(navController: NavController) {
    val listaAgregados = listOf(
        Agregado("1", "Papas Fritas Extra", "Porción de papas crujientes", 1500, "🍟"),
        Agregado("2", "Huevo Frito", "Huevo de campo yema blanda", 800, "🍳"),
        Agregado("3", "Pebre Casero", "Porción de pebre chileno", 500, "🌶️"),
        Agregado("4", "Palta Extra", "Molida fresca del día", 1200, "🥑", estaDisponible = false)
    )

    // 1. CAMBIO CLAVE: Mapa para guardar la cantidad de cada agregado seleccionado (ID -> Cantidad)
    val seleccionados = remember { mutableStateMapOf<String, Int>() }
    val MAX_SELECCIONES = 2
    var actualesSeleccionados = 0
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        EncabezadoAgregados()

        // Paso 3 de 4 en la barra de progreso
        SeccionProgresoAgregados()

        BannerContadorSeleccion(totalOpciones = listaAgregados.size,
            maxSelecciones = 2, actualesSeleccionados = seleccionados.size)

        // Lista de agregados con multi-selección
        ListaAgregados(
            lista = listaAgregados,
            seleccionados = seleccionados,
            maxSelecciones = MAX_SELECCIONES,
            onToggleAgregado = { id ->
                if (seleccionados.containsKey(id)) {
                    // Si ya está seleccionado, lo quitamos siempre
                    seleccionados.remove(id)
                } else {
                    // Solo dejamos agregar si aún no ha alcanzado el límite de 2
                    if (seleccionados.size < MAX_SELECCIONES) {
                        seleccionados[id] = 1
                    }
                }
            },
            modifier = Modifier.weight(1f)
        )

        // Calcula el total sumado de los agregados
        val totalAgregados = listaAgregados
            .filter { seleccionados.containsKey(it.id) }
            .sumOf { it.precio * (seleccionados[it.id] ?: 1) }

        BotonContinuarAgregados(
            totalExtra = totalAgregados,
            onClick = {
                // Le dices a Jetpack Compose que navegue a la ruta de agregados
                navController.navigate("seleccionEnsaladasSC")
            }
        )
    }
}

// 2. CAMBIO DE TEXTOS
@Composable
fun EncabezadoAgregados() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Agregados y Extras",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "¿Quieres añadir algo más a tu plato?",
            color = Color(0xFFC9BDB3),
            fontSize = 14.sp
        )
    }
}

@Composable
fun SeccionProgresoAgregados() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepProgressBar(totalSteps = 4, currentStep = 2)

        Text(
            text = "Opcional. Elige los adicionales que desees sumar a tu pedido.",
            color = Color(0xFFFFF4EC),
            fontSize = 14.sp
        )
    }
}

// 3. LISTA Y TARJETA CON CHECKBOX
@Composable
fun ListaAgregados(
    lista: List<Agregado>,
    seleccionados: Map<String, Int>,
    maxSelecciones: Int,
    onToggleAgregado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val limiteAlcanzado = seleccionados.size >= maxSelecciones

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lista.forEach { item ->
            val estaSeleccionado = seleccionados.containsKey(item.id)

            // Si se alcanzó el límite y este ítem NO está seleccionado, se deshabilita
            val puedeSeleccionar = estaSeleccionado || !limiteAlcanzado

            AgregadoCard(
                agregado = item,
                isSelected = estaSeleccionado,
                enabled = item.estaDisponible && puedeSeleccionar, // <--- Nueva condición
                onSelect = { onToggleAgregado(item.id) }
            )
        }
    }
}

@Composable
fun AgregadoCard(
    agregado: Agregado,
    isSelected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean
) {
    androidx.compose.material3.Card(
        onClick = onSelect,
        enabled = agregado.estaDisponible,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2A1D17) else Color(0xFF1E1E1E)
        ),
        border = androidx.compose.foundation.BorderStroke(
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
                Text(text = agregado.emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                Column {
                    Text(text = agregado.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "+$${agregado.precio} • ${agregado.descripcion}", color = Color(0xFFC9BDB3), fontSize = 12.sp)
                }
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFF2722C),
                    checkmarkColor = Color.Black,
                    uncheckedColor = Color.Gray
                )
            )
        }
    }
}

// 4. BOTÓN QUE MUESTRA EL EXTRA SUMADO
@Composable
fun BotonContinuarAgregados(totalExtra: Int, onClick: () -> Unit) {
    val textoBoton = if (totalExtra > 0) "Continuar (+$${totalExtra})" else "Omitir / Continuar"

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