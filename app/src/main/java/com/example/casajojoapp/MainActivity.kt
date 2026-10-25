package com.example.casajojoapp

import android.Manifest
import android.graphics.Color.TRANSPARENT
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.casajojoapp.domain.model.Rol
import com.example.casajojoapp.ui.admin.AccesoRestringidoAdmin
import com.example.casajojoapp.ui.admin.PantallaAdmin
import com.example.casajojoapp.ui.admin.PantallaDetallePedido
import com.example.casajojoapp.ui.admin.PantallaPedidosAdmin
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
import com.example.casajojoapp.ui.cliente.pedidos.PantallaMisPedidos
import com.example.casajojoapp.ui.componentes.BarraInferior
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

                // NOTIFICACIONES LOCALES (recurso nativo): pedir el permiso
                // POST_NOTIFICATIONS en runtime en Android 13+. En versiones
                // anteriores el permiso se otorga al instalar.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val contexto = LocalContext.current
                    val permisoNotificaciones = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { concedido ->
                        // Si se rechaza, la app sigue funcionando: solo no
                        // mostrará los avisos de estado de comanda.
                        if (!concedido) {
                            android.util.Log.i(
                                "Notificaciones",
                                "Permiso POST_NOTIFICATIONS denegado en ${contexto.packageName}"
                            )
                        }
                    }
                    LaunchedEffect(Unit) {
                        permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // BARRA INFERIOR ESTÁTICA (3 botones cuadrados): se ve en TODA
                // la app SALVO en las pantallas de selección del flujo de
                // armado (proteína, acompañamientos, ensalada, extras y hora
                // de retiro), donde manda la flecha de volver del header.
                val entrada by navController.currentBackStackEntryAsState()
                val rutaActual = entrada?.destination?.route
                val rutasSinBarra = setOf(
                    "seleccionProteinaSC",
                    "seleccionAcompanamientosSC",
                    "seleccionEnsaladasSC",
                    "postresBebidasSC",
                    "horaRetiroSC"
                )
                val mostrarBarra = rutaActual != null && rutaActual !in rutasSinBarra

                // Sin Scaffold el rol "background" del ColorScheme nunca se pinta y
                // el Text resolveria contra LocalContentColor (negro fijo), no contra
                // onSurface. El inner padding tambien aplica los insets del sistema.
                Scaffold(
                    bottomBar = {
                        if (mostrarBarra) {
                            BarraInferior(
                                navController = navController,
                                rutaActual = rutaActual
                            )
                        }
                    }
                ) { innerPadding ->
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
                        // Selector de hora de retiro (rango de pruebas
                        // 12:00–02:00; ver util/HorarioRetiro)
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
                        // VISTA DE PEDIDOS (barra inferior, botón del medio):
                        // - ADMIN -> cola filtrable por estado (filtro inicial
                        //   opcional vía ?filtro=ESTADO desde el panel).
                        // - USUARIO -> sus pedidos (linkeados a la cuenta).
                        // - INVITADO -> invitación a iniciar sesión.
                        composable(
                            route = "pedidos?filtro={filtro}",
                            arguments = listOf(
                                navArgument("filtro") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            )
                        ) { datosRuta ->
                            val filtroInicial =
                                datosRuta.arguments?.getString("filtro")?.ifBlank { null }
                            val sesionPedidos by orderViewModel.sesion.collectAsState()
                            if (sesionPedidos.rol == Rol.ADMIN) {
                                PantallaPedidosAdmin(
                                    navController = navController,
                                    viewModel = orderViewModel,
                                    filtroInicial = filtroInicial
                                )
                            } else {
                                PantallaMisPedidos(
                                    navController = navController,
                                    viewModel = orderViewModel
                                )
                            }
                        }
                        // DETALLE COMPLETO DE UN PEDIDO (admin): especificación
                        // de cada plato (LOCAL) o de cada línea (API).
                        composable(
                            route = "detallePedido/{id}",
                            arguments = listOf(
                                navArgument("id") { type = NavType.LongType }
                            )
                        ) { datosRuta ->
                            val pedidoId = datosRuta.arguments?.getLong("id") ?: -1L
                            PantallaDetallePedido(
                                navController = navController,
                                viewModel = orderViewModel,
                                pedidoId = pedidoId
                            )
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
