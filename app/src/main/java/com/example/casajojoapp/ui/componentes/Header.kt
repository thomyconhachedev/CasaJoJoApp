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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

// CAMBIO NAV / CAMBIO ROLES:
// - Flecha "atras" (←) que vuelve al paso anterior (navigateUp) en cada
//   vista del flujo. Se oculta en la home con mostrarAtras = false.
// - El logo + nombre llevan al home con popUpTo(home)+launchSingleTop para
//   no apilar copias repetidas.
// - Botón derecho según sesión: "Iniciar sesión" (invitado) o "Cuenta"
//   (USUARIO/ADMIN). La sesión vive en ServicioLocator.authRepository.
// - mostrarBotonCuenta = false: las pantallas de SELECCIÓN (proteína,
//   acompañamientos, ensalada, extras y hora de retiro) muestran SOLO la
//   flecha + "La Casa JoJo" alineados a la izquierda (la navegación de
//   esas pantallas vive en la barra inferior, que tampoco se muestra ahí).
@Composable
fun HeaderPrincipal(
    navController: NavController,
    mostrarAtras: Boolean = true,
    mostrarBotonCuenta: Boolean = true
) {
    val sesion by com.example.casajojoapp.data.ServicioLocator.authRepository.sesion.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = if (mostrarBotonCuenta) {
            Arrangement.SpaceBetween
        } else {
            Arrangement.Start // todo el contenido agrupado a la izquierda
        },
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

        if (mostrarBotonCuenta) {
            Button(
                onClick = {
                    if (sesion.rol == com.example.casajojoapp.domain.model.Rol.INVITADO) {
                        navController.navigate("login") { launchSingleTop = true }
                    } else {
                        navController.navigate("cuenta") { launchSingleTop = true }
                    }
                }
            ) {
                Text(if (sesion.rol == com.example.casajojoapp.domain.model.Rol.INVITADO) "Ingresar" else "Cuenta")
            }
        }
    }
}
