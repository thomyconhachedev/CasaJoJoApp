package com.example.casajojoapp

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.casajojoapp.domain.model.Rol
import com.example.casajojoapp.ui.admin.AccesoRestringidoAdmin
import com.example.casajojoapp.ui.admin.PantallaAdmin
import com.example.casajojoapp.ui.auth.PantallaLogin
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
                        // La MISMA instancia de OrderViewModel creada arriba se
                        // inyecta en cada ruta del flujo de armado para que
                        // compartan un único estado global.
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
                        // Selector de hora de retiro (12:00–22:00 TEMPORAL de
                        // pruebas; ver util/HorarioRetiro) entre Resumen y Carrito.
                        composable("horaRetiroSC") {
                            SeleccionarHoraRetiroSC(navController = navController, viewModel = orderViewModel)
                        }
                        composable("carritoSC") {
                            carritoSC(navController = navController, viewModel = orderViewModel)
                        }
                        // CAMBIO CUENTAS: perfil según sesión (cuenta o invitado).
                        composable("cuenta") {
                            PantallaCuenta(navController = navController)
                        }
                        // CAMBIO ROLES: login/registro con "origenAdmin" opcional
                        // (el panel exige cuenta de administrador).
                        composable(
                            route = "login?origenAdmin={origenAdmin}",
                            arguments = listOf(
                                navArgument("origenAdmin") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { entrada ->
                            val origenAdmin = entrada.arguments?.getBoolean("origenAdmin") ?: false
                            PantallaLogin(navController = navController, origenAdmin = origenAdmin)
                        }
                        // CAMBIO ROLES: cola de pedidos SOLO para sesión ADMIN;
                        // sin ella se muestra el gate de acceso.
                        composable("admin") {
                            val sesion by orderViewModel.sesion.collectAsState()
                            if (sesion.rol == Rol.ADMIN) {
                                PantallaAdmin(
                                    navController = navController,
                                    viewModel = orderViewModel
                                )
                            } else {
                                AccesoRestringidoAdmin(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}
