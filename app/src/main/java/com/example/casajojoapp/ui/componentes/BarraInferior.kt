package com.example.casajojoapp.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.R
import com.example.casajojoapp.data.ServicioLocator
import com.example.casajojoapp.domain.model.Rol
import com.example.casajojoapp.ui.theme.JoJoNaranjo
import com.example.casajojoapp.ui.theme.JoJoSeleccion
import com.example.casajojoapp.ui.theme.JoJoSuperficie
import com.example.casajojoapp.ui.theme.JoJoTextoSecundario

// =========================================================
// BARRA INFERIOR ESTÁTICA (3 botones cuadrados) en las pantallas de
// nivel superior: Inicio · Pedidos · Perfil.
// - El botón "Pedidos" adapta su etiqueta y destino al rol:
//   ADMIN -> "Cola" (vista filtrable de pedidos), USUARIO -> "Mis pedidos",
//   INVITADO -> "Pedidos" (pide iniciar sesión).
// Se renderiza como bottomBar del Scaffold de MainActivity solo en las
// rutas top-level (home/pedidos/cuenta); el flujo de armado no la muestra.
// =========================================================
@Composable
fun BarraInferior(navController: NavController, rutaActual: String?) {
    val sesion by ServicioLocator.authRepository.sesion.collectAsState()

    val etiquetaPedidos = when (sesion.rol) {
        Rol.ADMIN -> "Cola"
        Rol.INVITADO -> "Pedidos"
        else -> "Mis pedidos"
    }

    // Navegación tipo pestañas: limpia el stack sobre home y evita duplicados.
    val irA: (String) -> Unit = { ruta ->
        if (rutaActual != ruta) {
            navController.navigate(ruta) {
                popUpTo("home") { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Surface(
        color = JoJoSuperficie,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ItemBarraInferior(
                icono = R.drawable.ic_home,
                etiqueta = "Inicio",
                seleccionado = rutaActual == "home",
                onClic = { irA("home") }
            )
            ItemBarraInferior(
                icono = R.drawable.ic_pedidos,
                etiqueta = etiquetaPedidos,
                seleccionado = rutaActual == "pedidos",
                onClic = { irA("pedidos") }
            )
            ItemBarraInferior(
                icono = R.drawable.ic_perfil,
                etiqueta = "Perfil",
                seleccionado = rutaActual == "cuenta",
                onClic = { irA("cuenta") }
            )
        }
    }
}

// Botón cuadrado: contenedor de 46dp con ícono + rótulo debajo.
@Composable
private fun ItemBarraInferior(
    icono: Int,
    etiqueta: String,
    seleccionado: Boolean,
    onClic: () -> Unit
) {
    val color = if (seleccionado) JoJoNaranjo else JoJoTextoSecundario
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClic)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (seleccionado) JoJoSeleccion else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = etiqueta,
                modifier = Modifier.size(26.dp),
                tint = color
            )
        }
        Text(
            text = etiqueta,
            fontSize = 11.sp,
            color = color,
            fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
