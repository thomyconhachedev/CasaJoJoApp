package com.example.casajojoapp.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BannerContadorSeleccion(
    maxSelecciones: Int = 2,
    totalOpciones: Int = 5,
    actualesSeleccionados: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E)) // Fondo oscuro del contenedor
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Texto Izquierda: "Elige 2 de 5"
        Text(
            text = "Elige $maxSelecciones de $totalOpciones",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        // Badge/Píldora Derecha: "2 / 2 seleccionados"
        Row(
            modifier = Modifier
                .background(
                    color = Color(0xFF513020), // Tono café/naranja oscuro redondeado
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$actualesSeleccionados / $maxSelecciones seleccionados",
                color = Color(0xFFFFAA75), // Color del texto naranja/salmón
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}