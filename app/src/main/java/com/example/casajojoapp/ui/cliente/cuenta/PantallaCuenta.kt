package com.example.casajojoapp.ui.cliente.cuenta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.casajojoapp.R
import com.example.casajojoapp.ui.componentes.HeaderPrincipal
import com.example.casajojoapp.ui.theme.*

// =========================================================
// CAMBIO ROLES: pantalla de cuenta consciente de la sesiÃ³n.
// - SesiÃ³n USUARIO/ADMIN: muestra usuario+rol, edita sus datos y cierra
//   sesiÃ³n. La tabla "clientes" NO se toca (es el perfil de invitado).
// - Invitado: perfil local de siempre (nombre/telÃ©fono en este dispositivo)
//   + invitaciÃ³n a crear cuenta/iniciar sesiÃ³n.
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
                text = when {
                    uiState.sesionIniciada && uiState.rol == "ADMIN" ->
                        "SesiÃ³n de administrador (@${uiState.username})."
                    uiState.sesionIniciada ->
                        "SesiÃ³n iniciada como @${uiState.username}. Tus datos se autocompletan al ordenar."
                    uiState.perfilExiste ->
                        "Tus datos se autocompletan al ordenar (perfil de invitado, en este dispositivo)."
                    else ->
                        "Guarda tus datos para ordenar mÃ¡s rÃ¡pido. TambiÃ©n puedes crear una cuenta."
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

        // Errores inline por campo (calculados en CuentaViewModel.guardar).
        val errorNombreCampo = uiState.errorNombre
        val errorTelefonoCampo = uiState.errorTelefono

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                viewModel.limpiarErrorNombre()
            },
            label = { Text("Nombre") },
            singleLine = true,
            enabled = !uiState.isLoading,
            isError = errorNombreCampo != null,
            supportingText = if (errorNombreCampo != null) {
                { Text(text = errorNombreCampo, fontSize = 12.sp, color = JoJoRojoClaro) }
            } else null,
            trailingIcon = if (errorNombreCampo != null) {
                { Icon(painter = painterResource(id = R.drawable.ic_error), contentDescription = null, modifier = Modifier.size(18.dp), tint = JoJoRojoClaro) }
            } else null,
            colors = camposOscuros(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = {
                telefono = it
                viewModel.limpiarErrorTelefono()
            },
            label = { Text("TelÃ©fono") },
            singleLine = true,
            enabled = !uiState.isLoading,
            isError = errorTelefonoCampo != null,
            supportingText = if (errorTelefonoCampo != null) {
                { Text(text = errorTelefonoCampo, fontSize = 12.sp, color = JoJoRojoClaro) }
            } else null,
            trailingIcon = if (errorTelefonoCampo != null) {
                { Icon(painter = painterResource(id = R.drawable.ic_error), contentDescription = null, modifier = Modifier.size(18.dp), tint = JoJoRojoClaro) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = camposOscuros(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.guardar(nombre, telefono) },
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
                    uiState.sesionIniciada -> "Guardar cambios"
                    uiState.perfilExiste -> "Guardar cambios"
                    else -> "Guardar en este dispositivo"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (uiState.sesionIniciada) {
            OutlinedButton(
                onClick = {
                    viewModel.cerrarSesion()
                    navController.navigate("home") {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                },
                enabled = !uiState.isLoading,
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cerrar sesiÃ³n", color = JoJoRojoClaro, fontSize = 15.sp)
            }
        } else {
            TextButton(
                onClick = { navController.navigate("login") { launchSingleTop = true } },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Â¿Quieres guardar tu cuenta en cualquier dispositivo? Inicia sesiÃ³n o crÃ©ala",
                    color = JoJoNaranjo,
                    fontSize = 13.sp
                )
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
