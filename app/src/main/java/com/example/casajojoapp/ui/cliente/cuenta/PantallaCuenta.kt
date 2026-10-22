package com.example.casajojoapp.ui.cliente.cuenta

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.theme.*

// =========================================================
// CAMBIO CUENTAS: pantalla de cuenta del cliente (perfil local, sin PIN).
// Dos estados según CuentaUiState.perfilExiste:
//  - Sin perfil: formulario de registro (nombre + teléfono).
//  - Con perfil: ver/editar datos o eliminar la cuenta.
// =========================================================
@Composable
fun PantallaCuenta(
    navController: NavController,
    viewModel: CuentaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var nombre by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }

    // Sincroniza los campos cuando cambia el perfil cargado/guardado.
    LaunchedEffect(uiState.nombre, uiState.telefono) {
        nombre = uiState.nombre
        telefono = uiState.telefono
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderPrincipal(navController)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Mi Cuenta",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (uiState.perfilExiste) {
                    "Tus datos se autocompletan al ordenar."
                } else {
                    "Guarda tus datos para ordenar más rápido."
                },
                color = JoJoTextoSecundario,
                fontSize = 14.sp
            )
        }

        uiState.error?.let { mensaje ->
            Text(text = mensaje, color = JoJoRojoClaro, fontSize = 13.sp)
        }
        uiState.mensajeExito?.let { mensaje ->
            Text(text = mensaje, color = JoJoVerdeClaro, fontSize = 13.sp)
        }

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            enabled = !uiState.isLoading,
            colors = camposOscuros(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            singleLine = true,
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = camposOscuros(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (uiState.perfilExiste) {
                    viewModel.actualizarPerfil(nombre, telefono)
                } else {
                    viewModel.crearCuenta(nombre, telefono)
                }
            },
            enabled = !uiState.isLoading,
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
                text = when {
                    uiState.isLoading -> "Guardando..."
                    uiState.perfilExiste -> "Guardar cambios"
                    else -> "Guardar cuenta"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (uiState.perfilExiste) {
            TextButton(
                onClick = { viewModel.eliminarCuenta() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Eliminar cuenta de este dispositivo", color = JoJoRojoClaro)
            }
        }
    }
}

@Composable
private fun camposOscuros() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = JoJoNaranjo,
    unfocusedBorderColor = JoJoBorde,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = JoJoNaranjo,
    focusedLabelColor = JoJoNaranjo,
    unfocusedLabelColor = JoJoGrisMedio,
    focusedContainerColor = JoJoSuperficie,
    unfocusedContainerColor = JoJoSuperficie
)