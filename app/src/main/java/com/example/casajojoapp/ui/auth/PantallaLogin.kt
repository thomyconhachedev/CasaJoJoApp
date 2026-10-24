package com.example.casajojoapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.casajojoapp.domain.model.Rol
import com.example.casajojoapp.ui.theme.JoJoBorde
import com.example.casajojoapp.ui.theme.JoJoDeshabilitado
import com.example.casajojoapp.ui.theme.JoJoFondoApp
import com.example.casajojoapp.ui.theme.JoJoGrisMedio
import com.example.casajojoapp.ui.theme.JoJoNaranjo
import com.example.casajojoapp.ui.theme.JoJoRojoClaro
import com.example.casajojoapp.ui.theme.JoJoSuperficie
import com.example.casajojoapp.ui.theme.JoJoTextoSecundario

// =========================================================
// PANTALLA DE ACCESO (CAMBIO ROLES):
// - Login (USUARIO/ADMIN) o registro de cuenta nueva.
// - "Continuar como invitado": compra sin cuenta, como siempre.
// - origenAdmin=true: llegamos del panel restringido; solo un login ADMIN
//   entra al panel (un USUARIO ve el aviso y sigue aquí).
// =========================================================
@Composable
fun PantallaLogin(
    navController: NavController,
    origenAdmin: Boolean = false,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.exitoRol) {
        val rol = uiState.exitoRol ?: return@LaunchedEffect
        when {
            origenAdmin && rol != Rol.ADMIN -> viewModel.advertirNoEsAdmin()
            origenAdmin -> navController.navigate("admin") {
                popUpTo("home")
                launchSingleTop = true
            }
            // Invitado desde la pantalla de cuenta: simplemente vuelve.
            rol == Rol.INVITADO -> navController.navigateUp()
            // Login normal: vuelve de donde vino (home/cuenta/etc.).
            else -> navController.navigateUp()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JoJoFondoApp)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (origenAdmin) "Acceso de administrador" else if (uiState.modo == ModoAuth.LOGIN) "Iniciar sesión" else "Crear cuenta",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when {
                    origenAdmin -> "El panel de cocina requiere una cuenta de administrador."
                    uiState.modo == ModoAuth.LOGIN -> "Tu cuenta guarda tus datos para ordenar más rápido."
                    else -> "Crea tu cuenta para ordenar más rápido."
                },
                color = JoJoTextoSecundario,
                fontSize = 14.sp
            )
        }

        uiState.error?.let { mensaje ->
            Text(text = mensaje, color = JoJoRojoClaro, fontSize = 13.sp)
        }

        OutlinedTextField(
            value = uiState.username,
            onValueChange = viewModel::actualizarUsername,
            label = { Text("Usuario") },
            singleLine = true,
            enabled = !uiState.isLoading,
            colors = camposOscuros(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::actualizarPassword,
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !uiState.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = camposOscuros(),
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.modo == ModoAuth.REGISTRO) {
            OutlinedTextField(
                value = uiState.passwordConfirmacion,
                onValueChange = viewModel::actualizarPasswordConfirmacion,
                label = { Text("Repite la contraseña") },
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = camposOscuros(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = viewModel::actualizarNombre,
                label = { Text("Nombre") },
                singleLine = true,
                enabled = !uiState.isLoading,
                colors = camposOscuros(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.telefono,
                onValueChange = viewModel::actualizarTelefono,
                label = { Text("Teléfono") },
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = camposOscuros(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = viewModel::enviar,
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
                    uiState.isLoading -> "Un momento..."
                    uiState.modo == ModoAuth.LOGIN -> "Iniciar sesión"
                    else -> "Crear cuenta"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (!origenAdmin) {
            // El registro solo tiene sentido fuera del gate admin.
            TextButton(
                onClick = {
                    viewModel.cambiarModo(
                        if (uiState.modo == ModoAuth.LOGIN) ModoAuth.REGISTRO else ModoAuth.LOGIN
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.modo == ModoAuth.LOGIN) "¿No tienes cuenta? Créala aquí" else "Ya tengo cuenta",
                    color = JoJoNaranjo
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = viewModel::continuarComoInvitado,
                enabled = !uiState.isLoading,
                border = androidx.compose.foundation.BorderStroke(1.dp, JoJoBorde),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JoJoTextoSecundario),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Continuar como invitado", fontSize = 15.sp)
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
