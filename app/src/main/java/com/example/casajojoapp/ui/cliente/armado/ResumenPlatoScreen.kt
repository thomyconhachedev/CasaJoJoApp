package com.example.casajojoapp.ui.cliente.armado

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.StepProgressBar
import com.example.casajojoapp.ui.theme.*

// =========================================================
// CAMBIO MVVM (prompt sección 4.A):
// - Se ELIMINÓ la data class "ResumenPedido" con valores hardcodeados
//   (proteína/acompañamientos/extras ficticios que nunca cambiaban).
// - La pantalla ahora recibe "viewModel: OrderViewModel", colecta su
//   StateFlow con collectAsState() y muestra uiState.platoEnArmadoActual.
// - El precio total se calcula en vivo con platoEnArmadoActual.precioTotal
//   (getter de PlatoArmado) en vez de un total fijo.
//
// CAMBIO PRECIOS (paso 5 de 5):
// - Sin precios individuales por ítem: proteína, acompañamientos y
//   ensalada se listan solo con su nombre (incluidos en el plato de $4.800).
// - Se muestra el desglose: Plato $4.800 + Extras (+$X) = Total.
//
// CAMBIO FLUJO (al terminar de armar el plato):
// - Dos acciones en vez de un único botón:
//   "Pedir otro plato"  -> guarda el plato actual en el carrito y
//                          reinicia el armado desde el paso 1.
//   "Ir al carrito"     -> guarda el plato actual y va al carrito.
// - Ambas solo se habilitan con plato completo (1 proteína + al menos 1 acompañamiento).
//
// CAMBIO RESTRICCIONES:
// - Las restricciones ahora son POR PLATO y se escriben AQUÍ, antes de
//   terminar el plato (OutlinedTextField -> OrderViewModel.actualizarNotasPlato
//   -> PlatoArmado.notas). El carrito ya no tiene un campo de notas global.
//
// CAMBIO HORARIO:
// - "Ir al carrito" ahora pasa primero por SeleccionarHoraRetiroSC
//   (slots cada 10 min entre 12:00 y 16:00) y termina en el carrito.
// =========================================================

// 1. PANTALLA PRINCIPAL
@Composable
fun ResumenPlatoScreen(
    navController: NavController,
    viewModel: OrderViewModel // CAMBIO MVVM: antes tenía un ResumenPedido default
) {
    // CAMBIO MVVM: estado compartido colectado desde el OrderViewModel
    val uiState by viewModel.uiState.collectAsState()
    val plato = uiState.platoEnArmadoActual
    val precioTotal = plato.precioTotal // CAMBIO MVVM: $4.800 + extras (dinámico)

    // CAMBIO FLUJO: regla de negocio del plato (1 proteína + MÍNIMO 1 acompañamiento)
    val platoListo = plato.proteina != null && plato.acompanamientos.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        EncabezadoResumen()

        SeccionProgresoResumen()

        // Tarjeta con el detalle completo del plato armado
        TarjetaDetallePedido(
            plato = plato,
            precioTotal = precioTotal,
            modifier = Modifier.weight(1f)
        )

        if (!platoListo) {
            // Aviso en vez de botones muertos: indica qué falta para confirmar
            Text(
                text = "Faltan datos: elige 1 proteína y al menos 1 acompañamiento.",
                color = JoJoAcento,
                fontSize = 13.sp
            )
        }

        // CAMBIO RESTRICCIONES: antes de terminar el plato se escriben sus
        // restricciones; se guardan en PlatoArmado.notas (por plato, no globales)
        OutlinedTextField(
            value = plato.notas,
            onValueChange = { viewModel.actualizarNotasPlato(it) },
            label = { Text("Restricciones de este plato (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = JoJoNaranjo,
                focusedBorderColor = JoJoNaranjo,
                unfocusedBorderColor = JoJoBorde,
                focusedLabelColor = JoJoNaranjo,
                unfocusedLabelColor = JoJoTextoSecundario
            )
        )

        // CAMBIO FLUJO: al terminar de pedir las cosas del plato se abren
        // las DOS opciones: pedir otro plato o ir al carrito.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    // Guarda el plato actual y vuelve al paso 1.
                    // popUpTo(inclusive) limpia el stack del flujo (paso 1..5)
                    // para que "atrás" desde la nueva proteína no regrese al
                    // resumen del plato anterior; launchSingleTop evita duplicar
                    // la ruta si ya estaba en el stack.
                    if (platoListo) {
                        viewModel.agregarPlatoAlCarrito()
                        navController.navigate("seleccionProteinaSC") {
                            popUpTo("seleccionProteinaSC") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                enabled = platoListo,
                border = BorderStroke(1.dp, JoJoNaranjo),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = JoJoNaranjo,
                    disabledContentColor = JoJoGrisMedio
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Pedir otro plato",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Button(
                onClick = {
                    // Guarda el plato actual, pasa por el selector de hora de
                    // retiro y termina en el carrito. popUpTo borra el flujo de
                    // armado para que "atrás" desde la hora no regrese al resumen.
                    if (platoListo) {
                        viewModel.agregarPlatoAlCarrito()
                        navController.navigate("horaRetiroSC") {
                            popUpTo("seleccionProteinaSC") { inclusive = true }
                        }
                    }
                },
                enabled = platoListo,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoJoNaranjo,
                    contentColor = Color.Black,
                    disabledContainerColor = JoJoDeshabilitado,
                    disabledContentColor = JoJoGrisMedio
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Ir al carrito",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// 2. ENCABEZADO DE PANTALLA
@Composable
fun EncabezadoResumen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Resumen de tu Almuerzo",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Revisa tu selección antes de enviar a cocina",
            color = JoJoTextoSecundario,
            fontSize = 14.sp
        )
    }
}

// 3. BARRA DE PROGRESO (Paso 5 de 5)
@Composable
fun SeccionProgresoResumen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepProgressBar(totalSteps = 5, currentStep = 5)

        Text(
            text = "¡Paso final! Confirma tu plato personalizado.",
            color = JoJoTextoClaro,
            fontSize = 14.sp
        )
    }
}

// 4. TARJETA DETALLE DEL PEDIDO
// CAMBIO MVVM: ahora recibe el PlatoArmado real del ViewModel en vez de
// un ResumenPedido con datos de ejemplo.
// CAMBIO PRECIOS: los ítems base (proteína/acomp/ensalada) van sin precio;
// solo se desglosan Plato $4.800 y Extras (+$X).
@Composable
fun TarjetaDetallePedido(
    plato: PlatoArmado,
    precioTotal: Double,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, JoJoBorde),
        colors = CardDefaults.cardColors(
            containerColor = JoJoSuperficie
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "TU COMBO PERSONALIZADO",
                color = JoJoAcento,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Proteína elegida (null = aún no ha elegido)
            ItemResumen(
                titulo = "Proteína",
                detalle = plato.proteina?.nombre ?: "Sin seleccionar"
            )

            // Acompañamientos (mínimo 1, máximo 2; próximamente pedidos especiales)
            ItemResumen(
                titulo = "Acompañamientos (${plato.acompanamientos.size})",
                detalle = if (plato.acompanamientos.isEmpty()) {
                    "Ninguno"
                } else {
                    plato.acompanamientos.joinToString("\n• ", prefix = "• ") { it.nombre }
                }
            )

            // Ensalada (null en el estado global = "Sin Ensalada")
            ItemResumen(
                titulo = "Ensalada",
                detalle = plato.ensalada?.nombre ?: "Sin ensalada"
            )

            // Extras / Postres y bebidas (los ÚNICOS con precio propio)
            if (plato.extras.isNotEmpty()) {
                ItemResumen(
                    titulo = "Extras (Postres y Bebidas)",
                    detalle = plato.extras.joinToString("\n") {
                        "• ${it.nombre} (+$${it.precio.toLong()})"
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = JoJoBorde, thickness = 1.dp)

            // CAMBIO PRECIOS: desglose fijo "Plato" + "Extras" = "Total"
            FilaPrecio(
                etiqueta = "Plato",
                detalle = "proteína + acompañamientos + ensalada",
                monto = "$${PlatoArmado.PRECIO_BASE.toLong()}",
                esTotal = false
            )
            if (plato.extras.isNotEmpty()) {
                FilaPrecio(
                    etiqueta = "Extras (${plato.extras.size})",
                    monto = "+$${plato.extras.sumOf { it.precio }.toLong()}",
                    esTotal = false
                )
            }
            FilaPrecio(
                etiqueta = "Total a pagar",
                monto = "$${precioTotal.toLong()}",
                esTotal = true
            )
        }
    }
}

// Sub-componente para cada renglón del detalle
@Composable
fun ItemResumen(titulo: String, detalle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = titulo,
            color = JoJoTextoSecundario,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = detalle,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Fila de desglose de precios (Plato / Extras / Total).
// La etiqueta y el monto van anclados a los extremos; "detalle" (opcional)
// es una segunda línea pequeña bajo la etiqueta para no partir el texto en
// varias filas cuando el nombre es largo.
@Composable
private fun FilaPrecio(
    etiqueta: String,
    monto: String,
    esTotal: Boolean,
    detalle: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (esTotal) Alignment.CenterVertically else Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = etiqueta,
                color = if (esTotal) Color.White else JoJoTextoSecundario,
                fontSize = if (esTotal) 18.sp else 13.sp,
                fontWeight = if (esTotal) FontWeight.Bold else FontWeight.Normal
            )
            if (detalle != null) {
                Text(
                    text = detalle,
                    color = JoJoGrisMedio,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        Text(
            text = monto,
            color = if (esTotal) JoJoNaranjo else Color.White,
            fontSize = if (esTotal) 22.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
