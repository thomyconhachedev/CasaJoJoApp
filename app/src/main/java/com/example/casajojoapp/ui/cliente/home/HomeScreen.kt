package com.example.casajojoapp.ui.cliente.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.casajojoapp.R
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.domain.model.Producto
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// =========================================================
// HOME / PANTALLA PRINCIPAL:
// - Encabezado con marca, ubicación y fecha del día.
// - Tarjeta "Menú de Hoy" con los accesos al flujo de armado.
// - Carrusel de proteínas del menú vigente (LOCAL/API) con estado AGOTADO.
// - Acceso discreto al panel admin al final de la pantalla.
// Los colores vienen SIEMPRE de MaterialTheme.colorScheme (tokens del tema).
// =========================================================
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: OrderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EncabezadoHome()

            Spacer(modifier = Modifier.height(16.dp))

            TarjetaMenuDiario(
                onArmarPedido = { navController.navigate("seleccionProteinaSC") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contacto para pedidos grandes / eventos (WhatsApp).
            TarjetaPedidoGrande()

            Spacer(modifier = Modifier.height(24.dp))

            SeccionProteinasDestacadas(
                proteinas = uiState.proteinasDisponibles,
                isLoading = uiState.isLoadingMenu,
                error = uiState.menuError
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Acceso admin discreto: SOLO visible con sesión de administrador
            // (CAMBIO ROLES). Para clientes no existe el botón.
            val sesion by viewModel.sesion.collectAsState()
            if (sesion.rol == com.example.casajojoapp.domain.model.Rol.ADMIN) {
                OutlinedButton(
                    onClick = { navController.navigate("admin") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_admin_panel),
                        contentDescription = "Panel de Administración",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acceso Panel Administrador",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// 1. ENCABEZADO: título, subtítulo, fecha actual y ubicación.
@Composable
private fun EncabezadoHome() {
    val hoy = LocalDate.now()
    val formateador = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))
    val fechaFormateada = hoy.format(formateador).split(" ")
        .joinToString(" ") { palabra -> palabra.replaceFirstChar { it.uppercase() } }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "La Casa JoJo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Comida casera en Viña del Mar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = "Ubicación",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = fechaFormateada,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 2. TARJETA HERO: menú del día + acción principal al flujo de armado.
// A3: se elimina el segundo botón ("Elegir Combo del Día") que apuntaba a
// la MISMA acción que el principal (rúbrica IE 2.1.1: botones sin acción).
@Composable
private fun TarjetaMenuDiario(onArmarPedido: () -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Menú de Hoy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Arma tu plato paso a paso: proteína, acompañamientos, ensalada y los extras que quieras.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onArmarPedido,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = "Armar mi Pedido Paso a Paso",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// TARJETA DE CONTACTO: pedidos grandes / eventos por WhatsApp
// (rubrica US-012). Mismo lenguaje visual que la tarjeta hero: borde
// naranjo, fondo surface, esquinas de 18dp.
@Composable
private fun TarjetaPedidoGrande() {
    val contexto = LocalContext.current
    val mensajeWhatsApp =
        "¡Hola, La Casa JoJo! 👋 Quiero coordinar un pedido grande " +
            "(trabajo, celebración o grupo). ¿Nos ayudan a coordinar cantidades y horario?"

    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "¿UN PEDIDO GRANDE?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "¿Almuerzo para el equipo, una celebración o un grupo de amigos? " +
                    "Escríbenos por WhatsApp y coordinamos todo: cantidades, menú y horario de retiro, " +
                    "a la medida de tu evento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    // Abre WhatsApp con el mensaje precargado. Si se define el
                    // número (formato 569XXXXXXXX) va directo al chat; si no,
                    // abre WhatsApp para elegir el contacto.
                    val uri = if (WHATSAPP_NUMERO.isBlank()) {
                        Uri.parse("whatsapp://send?text=${Uri.encode(mensajeWhatsApp)}")
                    } else {
                        Uri.parse("https://wa.me/$WHATSAPP_NUMERO?text=${Uri.encode(mensajeWhatsApp)}")
                    }
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        contexto.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            contexto,
                            "No se encontró WhatsApp en este dispositivo.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(text = "Contáctanos por WhatsApp", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Número del negocio en formato internacional sin '+' (ej. 56912345678).
// Vacío => se abre WhatsApp para elegir el contacto.
private const val WHATSAPP_NUMERO = ""

// 3. CARRUSEL de proteínas del menú vigente, con estado AGOTADO.
@Composable
private fun SeccionProteinasDestacadas(
    proteinas: List<Producto>,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Proteínas Destacadas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading && proteinas.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            proteinas.isEmpty() && error != null -> {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            proteinas.isEmpty() -> {
                Text(
                    text = "El menú de hoy estará disponible pronto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(proteinas) { producto ->
                        TarjetaProteina(producto)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaProteina(producto: Producto) {
    val agotado = !producto.disponible
    // Las proteínas no tienen precio propio: el plato cuesta el base ($4.800).
    val precioTexto = if (producto.precio > 0) {
        "$${producto.precio.toLong()}"
    } else {
        "$${PlatoArmado.PRECIO_BASE.toLong()}"
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(150.dp)
            .alpha(if (agotado) 0.5f else 1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = producto.emoji ?: "🥩",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = precioTexto,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            if (agotado) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = "AGOTADO",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
