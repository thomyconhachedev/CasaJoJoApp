package com.example.casajojoapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale



@Composable
fun PantallaDos(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {

        HeaderPrincipal(navController)

        SeccionInformativa()

        TarjetaPromocion()
    }
}

// 3. SECCIÓN: Subtítulos del día (Cambiado a Column para que no se amontonen de lado)
@Composable
fun SeccionInformativa() {

    val hoy = LocalDate.now()
    val formateador = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))
    val fechaFormateada = hoy.format(formateador).split(" ")
        .joinToString(" ") { palabra -> palabra.replaceFirstChar { it.uppercase() } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp) // Espacio pequeño entre renglones
    ) {
        Text(
            text = "Hoy se come en casa",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "$fechaFormateada • Retiro de 12:30 a 15:00",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

// 4. SECCIÓN: Tu tarjeta con la redondez que querías
@Composable
fun TarjetaPromocion() {
    Card(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(2.dp, Color(0xFFF2722C)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF513020)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Aquí adentro puedes empezar a meter el corazón, los títulos del plato, etc.
            Text(
                text = "EL FAVORITO DE LA CASA",
                color = Color(0xFFFFAA75),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp, start = 10.dp)
            )
            Text(
                // CAMBIO PRECIOS: el plato cuesta $4.800 (antes "Combo completo - $6.500")
                text = "Plato completo - $4.800",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Combo de hoy: pollo al horno + arroz casero + ensalada chilena.",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Text(
                text = "Tambien puedes elegir 1 proteina + 2 acompañamientos. Postres y bebidas por separado.",
                color = Color(0xFFC9BDB3),
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                // Los colores se pasan aquí como un parámetro del botón:
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF2722C),
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Text("Elegir combo de hoy")
            }
            Button (onClick = {}, modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                border = BorderStroke(1.dp,Color(0xFFF2722C)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF252321),
                    contentColor = Color(0xFFFFAA75)
                ),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Text("Personalizar mi combo")
            }
        }
    }
}