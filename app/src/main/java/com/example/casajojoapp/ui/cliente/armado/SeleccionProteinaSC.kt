package com.example.casajojoapp.ui.cliente.armado

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.domain.model.Producto
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.componentes.ProteinaCard
import com.example.casajojoapp.ui.componentes.StepProgressBar
import com.example.casajojoapp.ui.theme.*

// 1. PANTALLA PRINCIPAL (Contenedor estructurado en módulos)
// CAMBIO MVVM (sección 3): firma ampliada con viewModel: OrderViewModel.
// CAMBIO API: la lista de proteínas ya NO está hardcodeada; viene del
// OrderViewModel (menú LOCAL o API según el interruptor del panel Admin).
@Composable
fun SeleccionProteinaSC(navController: NavController, viewModel: OrderViewModel) {
    // CAMBIO MVVM: se REEMPLAZÓ el "var selectedId by remember { mutableStateOf("1") }"
    // por estado compartido. selectedId ahora se DERIVA del OrderViewModel, así al
    // volver atrás la selección sigue siendo la que el usuario eligió. null = sin elegir aún.
    val uiState by viewModel.uiState.collectAsState()
    val proteinas: List<Producto> = uiState.proteinasDisponibles
    val selectedId: String? = uiState.platoEnArmadoActual.proteina?.id?.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController, mostrarBotonCuenta = false)

        EncabezadoSeleccion()

        SeccionProgreso()

        // ANIMACIÓN (IE 2.2.2): crossfade entre cargando / error / lista,
        // la caja ocupa el espacio restante de la columna.
        val estadoMenu = when {
            uiState.isLoadingMenu -> "cargando"
            uiState.menuError != null -> "error"
            else -> "lista"
        }
        Crossfade(
            targetState = estadoMenu,
            animationSpec = tween(300),
            label = "estadoMenu",
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { estado ->
            when (estado) {
                "cargando" -> Text(
                    text = "Cargando menú…",
                    color = JoJoTextoSecundario,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                "error" -> Text(
                    text = uiState.menuError ?: "",
                    color = JoJoRojoClaro,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                else -> ListaProteinas(
                    proteinas = proteinas,
                    selectedId = selectedId,
                    // CAMBIO MVVM: la selección se persiste en el estado global
                    // pasando el Producto de dominio DIRECTO (sin re-mapear).
                    onProteinaSeleccionada = { producto -> viewModel.seleccionarProteina(producto) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        BotonContinuar(
            // CAMBIO FLUJO: la proteína es obligatoria (1 por plato),
            // sin ella no se puede avanzar al paso de acompañamientos.
            habilitado = uiState.platoEnArmadoActual.proteina != null,
            onClick = {
                // CAMBIO FLUJO: el paso 2 ahora es Acompañamientos (antes Agregados)
                navController.navigate("seleccionAcompanamientosSC")
            }
        )
    }
}

// 2. SECCIÓN: Títulos principales de la pantalla
@Composable
fun EncabezadoSeleccion() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Elige tu proteína",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Un almuerzo preparado con cariño",
            color = JoJoTextoSecundario,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// 3. SECCIÓN: Barra de progreso e instrucciones del paso
@Composable
fun SeccionProgreso() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepProgressBar(totalSteps = 5, currentStep = 1)

        Text(
            // CAMBIO PRECIOS: el plato cuesta $4.800 e incluye
            // 2 acompañamientos + ensalada (sin precio por ítem)
            text = "Elige 1 proteína. Tu plato cuesta $4.800 e incluye 2 acompañamientos y ensalada.",
            color = JoJoTextoClaro,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// 4. SECCIÓN: Lista dinámica de tarjetas de proteína
@Composable
fun ListaProteinas(
    proteinas: List<Producto>,
    selectedId: String?, // CAMBIO MVVM: ahora es nullable porque deriva del OrderViewModel
    onProteinaSeleccionada: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        proteinas.forEach { producto ->
            ProteinaCard(
                proteina = producto.aProteinaVisual(),
                isSelected = (producto.id.toString() == selectedId),
                onSelect = { onProteinaSeleccionada(producto) }
            )
        }
    }
}

// 5. SECCIÓN: Botón principal de acción
// CAMBIO FLUJO: gana "habilitado" para bloquear el avance sin proteína
@Composable
fun BotonContinuar(habilitado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        colors = ButtonDefaults.buttonColors(
            containerColor = JoJoNaranjo,
            contentColor = Color.Black,
            disabledContainerColor = JoJoDeshabilitado,
            disabledContentColor = JoJoGrisMedio
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (habilitado) "Continuar" else "Elige una proteína",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}