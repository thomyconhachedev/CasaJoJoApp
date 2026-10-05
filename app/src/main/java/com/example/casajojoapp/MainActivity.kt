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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.casajojoapp.pedido.cliente.armado.ResumenPlatoScreen
import com.example.casajojoapp.pedido.cliente.armado.SeleccionAgregadosSC
import com.example.casajojoapp.pedido.cliente.armado.SeleccionEnsaladaSC
import com.example.casajojoapp.pedido.cliente.armado.SeleccionProteinaSC
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
                        composable("seleccionProteinaSC") {
                            SeleccionProteinaSC(navController)
                        }
                        composable("seleccionAgregadosSC") {
                            SeleccionAgregadosSC(navController)
                        }
                        composable("seleccionEnsaladasSC") {
                            SeleccionEnsaladaSC(navController)
                        }
                        composable("resumen_plato") {
                            ResumenPlatoScreen(navController = navController)
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
