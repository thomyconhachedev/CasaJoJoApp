package com.example.casajojoapp.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.R

// CAMBIO NAV: 
// - Flecha "atras" (←) que vuelve al paso anterior (navigateUp) en cada
//   vista del flujo. Se oculta en la home con mostrarAtras = false.
// - El logo + nombre ahora son clickeables y llevan a la vista principal
//   (home, la pantalla principal de la app). popUpTo(home) + launchSingleTop
//   evitan apilar copias repetidas de home/cuenta al tocar varias veces.
@Composable
fun HeaderPrincipal(
    navController: NavController,
    mostrarAtras: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (mostrarAtras) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.semantics { contentDescription = "Volver" }
            ) {
                Text(
                    text = "\u2190", // ←
                    color = Color.White,
                    fontSize = 26.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable {
                    navController.navigate("home") {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.lacasajojo),
                contentDescription = "Ir a la pantalla principal",
                modifier = Modifier.size(35.dp).padding(horizontal = 8.dp, vertical = 12.dp)
            )

            Text(
                text = "La Casa JoJo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = { navController.navigate("cuenta") { launchSingleTop = true } }
        ) {
            Text("Cuenta")
        }
    }
}
