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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.casajojoapp.ui.cliente.cuenta.PantallaCuenta
import com.example.casajojoapp.ui.cliente.home.HomeScreen
import com.example.casajojoapp.ui.theme.CasaJoJoAppTheme
import com.example.casajojoapp.ui.admin.PantallaAdmin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La app fuerza modo oscuro (ver Theme.kt), así que las system bars usan
        // estilo fijo oscuro (iconos claros) para que sigan legibles aunque el
        // sistema esté en modo claro. Scrim TRANSPARENT: el fondo lo pinta cada pantalla.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(TRANSPARENT),
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
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable("home") {
                            HomeScreen(navController = navController, viewModel = orderViewModel)
                        }
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
                        // CAMBIO CUENTAS: pantalla de perfil local del cliente
                        // (registro / editar / eliminar). Se accede desde el Header.
                        composable("cuenta") {
                            PantallaCuenta(navController = navController)
                        }
                        // ADMIN: cola de pedidos enviados, ordenados por hora de retiro.
                        composable("admin") {
                            PantallaAdmin(
                                navController = navController,
                                viewModel = orderViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

// Pantalla de inicio (restaurada desde HEAD): bienvenida + acceso al flujo de
// armado y al panel de administración de la cola de pedidos.
@Composable
fun PantallaUno(navController: NavController) {
    Column {
        Text(text = "Bienvenido a CasaJoJoApp")
        Button(onClick = { navController.navigate("pantalla_dos") }) {
            Text("Ir a pantalla dos")
        }
        Button(
            onClick = { navController.navigate("seleccionProteinaSC") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
        ) {
            Text("SELECCIONA LA PROTEINA")
        }
        Button(
            onClick = { navController.navigate("admin") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
        ) {
            Text("PANEL ADMIN")
        }
    }
}

