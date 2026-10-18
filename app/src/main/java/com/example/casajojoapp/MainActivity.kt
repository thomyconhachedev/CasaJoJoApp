package com.example.casajojoapp

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.cliente.armado.ResumenPlatoScreen
import com.example.casajojoapp.ui.cliente.armado.SeleccionAcompanamientosSC
import com.example.casajojoapp.ui.cliente.armado.SeleccionEnsaladaSC
import com.example.casajojoapp.ui.cliente.armado.SeleccionPostresBebidasSC
import com.example.casajojoapp.ui.cliente.armado.SeleccionProteinaSC
import com.example.casajojoapp.ui.cliente.carrito.SeleccionarHoraRetiroSC
import com.example.casajojoapp.ui.cliente.carrito.carritoSC
import com.example.casajojoapp.ui.theme.CasaJoJoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // SystemBarStyle.auto detecta el modo oscuro y elige los iconos de la barra
        // sin parametros, por eso no hace falta un themes.xml aparte para -night.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT),
        )
        setContent {
            val orderViewModel: OrderViewModel = viewModel()
            CasaJoJoAppTheme {
                val navController = rememberNavController()

                // Sin Scaffold el rol "background" del ColorScheme nunca se pinta y
                // el Text resolveria contra LocalContentColor (negro fijo), no contra
                // onSurface. El inner padding tambien aplica los insets del sistema.
                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "pantalla_uno",
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable("pantalla_uno") {
                            PantallaUno(navController)
                        }
                        composable("pantalla_dos") {
                            PantallaDos(navController)
                        }
                        // CAMBIO MVVM (sección 3): la MISMA instancia de OrderViewModel
                        // creada arriba con viewModel() se inyecta en cada ruta del
                        // flujo de armado para que compartan un único estado global.
                        // CAMBIO FLUJO (5 pasos): Proteína -> Acompañamientos(2)
                        // -> Ensalada -> Postres/Bebidas (opcionales) -> Resumen
                        composable("seleccionProteinaSC") {
                            SeleccionProteinaSC(navController, viewModel = orderViewModel)
                        }
                        composable("seleccionAcompanamientosSC") {
                            SeleccionAcompanamientosSC(navController, viewModel = orderViewModel)
                        }
                        composable("seleccionEnsaladasSC") {
                            SeleccionEnsaladaSC(navController, viewModel = orderViewModel)
                        }
                        composable("postresBebidasSC") {
                            SeleccionPostresBebidasSC(navController, viewModel = orderViewModel)
                        }
                        composable("resumen_plato") {
                            ResumenPlatoScreen(navController = navController, viewModel = orderViewModel)
                        }
                        // CAMBIO HORARIO: selector de hora de retiro (12:00–16:00
                        // cada 10 min) entre el Resumen y el Carrito
                        composable("horaRetiroSC") {
                            SeleccionarHoraRetiroSC(navController = navController, viewModel = orderViewModel)
                        }
                        composable("carritoSC") {
                            carritoSC(navController = navController, viewModel = orderViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaUno(navController: NavController) {
    Column {
        Text(text = "Bienvenido a CasaJoJoApp")
        Button(onClick = { navController.navigate("pantalla_dos") }) {
            Text("Ir a pantalla dos")
        }
        Button (onClick = { navController.navigate("seleccionProteinaSC")} , modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)){
            Text("SELECCIONA LA PROTEINA")
        }
    }
}
