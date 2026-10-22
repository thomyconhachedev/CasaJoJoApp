package com.example.casajojoapp.ui.componentes

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casajojoapp.ui.theme.*

data class Proteina(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val emoji: String,
    val estaDisponible: Boolean = true
)

// Colores del patrón La Casa JoJo
val DarkBackground = JoJoSuperficie
val PrimaryOrange = JoJoNaranjoVivo
val SelectedBackground = JoJoSeleccion
val BorderDark = JoJoBorde

@Composable
fun ProteinaCard(
    proteina: Proteina,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    // Si no está disponible, bajamos la opacidad y desactivamos el click
    val cardAlpha = if (proteina.estaDisponible) 1.0f else 0.4f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(cardAlpha)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryOrange else BorderDark,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = proteina.estaDisponible) { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SelectedBackground else DarkBackground
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Contenido de la Proteína (Emoji + Nombre + Descripción)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = proteina.emoji,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = proteina.nombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = proteina.descripcion,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Indicador de Estado (Radio Button / Check / Agotado)
            if (!proteina.estaDisponible) {
                Surface(
                    color = JoJoRojoFondo,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "AGOTADO",
                        color = JoJoRojoTexto,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = PrimaryOrange,
                        unselectedColor = Color.Gray
                    )
                )
            }
        }
    }
}