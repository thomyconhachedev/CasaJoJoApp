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

// Colores basados en la imagen
val OrangeAccent = Color(0xFFFF6B2B) // Segmento activo
val DarkSegment = Color(0xFF382C24)  // Segmento inactivo
val TextOrange = Color(0xFFFF9E66)    // Texto del encabezado

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
            .background(Color(0xFF121212))
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