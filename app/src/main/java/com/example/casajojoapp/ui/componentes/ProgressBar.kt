package com.example.casajojoapp.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casajojoapp.ui.theme.*

// Colores basados en la imagen
val OrangeAccent = JoJoNaranjoVivo // Segmento activo
val DarkSegment = JoJoDeshabilitado  // Segmento inactivo
val TextOrange = JoJoNaranjoTexto    // Texto del encabezado

@Composable
fun StepProgressBar(
    totalSteps: Int = 5, // CAMBIO FLUJO: el armado ahora tiene 5 pasos
    currentStep: Int = 1,
    title: String = "ARMA TU ALMUERZO",
    barHeight: Dp = 6.dp,
    spaceBetween: Dp = 8.dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(JoJoFondoApp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Texto Superior: "ARMA TU ALMUERZO · PASO 1 DE 4"
        Text(
            text = "$title · PASO $currentStep DE $totalSteps",
            color = TextOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 2. Barra Segmentada
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
            for (step in 1..totalSteps) {
                val isActive = step <= currentStep
                Box(
                    modifier = Modifier
                        .weight(1f) // Hace que todos los segmentos midan lo mismo
                        .height(barHeight)
                        .clip(RoundedCornerShape(50)) // Puntas redondeadas
                        .background(if (isActive) OrangeAccent else DarkSegment)
                )
            }
        }
    }
}