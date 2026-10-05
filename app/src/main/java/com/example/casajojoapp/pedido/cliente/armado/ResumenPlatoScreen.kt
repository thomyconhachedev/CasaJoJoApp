package com.example.casajojoapp.pedido.cliente.armado

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.StepProgressBar

// Modelo para el resumen completo del pedido
data class ResumenPedido(
    val proteina: String = "Carne Mechada 🥩",
    val acompanamientos: List<String> = listOf("Puré de Papas 🥔", "Arroz Casero 🍚"),
    val ensalada: String = "Ensalada Chilena 🥗",
    val extras: List<Pair<String, Int>> = listOf("Pebre Extra 🌶️" to 500),
    val precioBaseCombo: Int = 6500
)

// 1. PANTALLA PRINCIPAL
@Composable
fun ResumenPlatoScreen(
    navController: NavController,
    pedido: ResumenPedido = ResumenPedido() // Puedes recibirlo desde el ViewModel o NavArgs
) {
    val totalExtras = pedido.extras.sumOf { it.second }
    val precioTotal = pedido.precioBaseCombo + totalExtras

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        EncabezadoResumen()

        SeccionProgresoResumen()

        // Tarjeta con el detalle completo del plato armado
        TarjetaDetallePedido(
            pedido = pedido,
            precioTotal = precioTotal,
            modifier = Modifier.weight(1f)
        )

        BotonConfirmarPedido(
            precioTotal = precioTotal,
            onClick = {
                // Navegar a la pantalla de confirmación o enviar a la API
                navController.navigate("confirmacion_pedido")
            }
        )
    }
}

// 2. ENCABEZADO DE PANTALLA
@Composable
fun EncabezadoResumen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Resumen de tu Almuerzo",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Revisa tu selección antes de enviar a cocina",
            color = Color(0xFFC9BDB3),
            fontSize = 14.sp
        )
    }
}

// 3. BARRA DE PROGRESO (Paso 4 de 4)
@Composable
fun SeccionProgresoResumen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepProgressBar(totalSteps = 4, currentStep = 4)

        Text(
            text = "¡Paso final! Confirma tu plato personalizado.",
            color = Color(0xFFFFF4EC),
            fontSize = 14.sp
        )
    }
}

// 4. TARJETA DETALLE DEL PEDIDO
@Composable
fun TarjetaDetallePedido(
    pedido: ResumenPedido,
    precioTotal: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF333333)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "TU COMBO PERSONALIZADO",
                color = Color(0xFFFFAA75),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Proteína elegida
            ItemResumen(titulo = "Proteína", detalle = pedido.proteina)

            // Acompañamientos
            ItemResumen(
                titulo = "Acompañamientos",
                detalle = pedido.acompanamientos.joinToString("\n• ", prefix = "• ")
            )

            // Ensalada
            ItemResumen(titulo = "Ensalada", detalle = pedido.ensalada)

            // Extras / Agregados (si existen)
            if (pedido.extras.isNotEmpty()) {
                ItemResumen(
                    titulo = "Agregados",
                    detalle = pedido.extras.joinToString("\n") { "• ${it.first} (+$${it.second})" }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Divider(color = Color(0xFF333333), thickness = 1.dp)

            // Desglose del total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total a pagar",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${precioTotal}",
                    color = Color(0xFFF2722C),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Sub-componente para cada renglón del detalle
@Composable
fun ItemResumen(titulo: String, detalle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = titulo,
            color = Color(0xFFC9BDB3),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = detalle,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// 5. BOTÓN DE CONFIRMACIÓN FINAL
@Composable
fun BotonConfirmarPedido(precioTotal: Int, onClick: () -> Unit) {
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
            text = "Confirmar y pedir ($${precioTotal})",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}