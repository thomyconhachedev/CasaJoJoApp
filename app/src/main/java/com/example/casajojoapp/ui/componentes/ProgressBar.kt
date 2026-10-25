package com.example.casajojoapp.ui.componentes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casajojoapp.ui.theme.*

// Colores basados en la imagen
val OrangeAccent = JoJoNaranjoVivo // Segmento activo
val DarkSegment = JoJoDeshabilitado  // Segmento inactivo
val TextOrange = JoJoNaranjoTexto    // Texto del encabezado

// ANIMACIÓN (IE 2.2.2): al avanzar de paso, cada segmento transiciona su
// color con animateColorAsState en vez de cambiar de golpe, y el rótulo
// "PASO X DE 5" hace crossfade con el número nuevo.
@Composable
fun StepProgressBar(
    totalSteps: Int = 5,
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
        // 1. Texto Superior: "ARMA TU ALMUERZO · PASO X DE 5"
        val tituloPaso = "$title · PASO $currentStep DE $totalSteps"
        AnimatedContent(
            targetState = tituloPaso,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "tituloPaso"
        ) { texto ->
            Text(
                text = texto,
                color = TextOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 2. Barra segmentada con transición de color por segmento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
            for (step in 1..totalSteps) {
                val isActive = step <= currentStep
                // Avance 0→1 ANIMADO al entrar a la pantalla: cada pantalla
                // crea una instacia NUEVA de la barra, así que sin esto el
                // color se componía instantáneo y no se veía transición.
                val progreso = remember { androidx.compose.animation.core.Animatable(0f) }
                LaunchedEffect(isActive) {
                    progreso.animateTo(
                        targetValue = if (isActive) 1f else 0f,
                        animationSpec = tween(durationMillis = 500)
                    )
                }
                val colorSegmento = lerp(DarkSegment, OrangeAccent, progreso.value)
                Box(
                    modifier = Modifier
                        .weight(1f) // Hace que todos los segmentos midan lo mismo
                        .height(barHeight)
                        .clip(RoundedCornerShape(50)) // Puntas redondeadas
                        .background(colorSegmento)
                )
            }
        }
    }
}
