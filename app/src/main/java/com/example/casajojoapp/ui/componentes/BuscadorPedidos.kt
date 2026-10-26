package com.example.casajojoapp.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casajojoapp.ui.cliente.armado.OrderUiState
import com.example.casajojoapp.ui.cliente.armado.OrderViewModel
import com.example.casajojoapp.ui.theme.JoJoBorde
import com.example.casajojoapp.ui.theme.JoJoDeshabilitado
import com.example.casajojoapp.ui.theme.JoJoGrisMedio
import com.example.casajojoapp.ui.theme.JoJoNaranjo
import com.example.casajojoapp.ui.theme.JoJoRojoClaro
import com.example.casajojoapp.ui.theme.JoJoSuperficie

// =========================================================
// BUSCADOR DE PEDIDOS compartido (cola admin + Mis Pedidos):
// busca por código de seguimiento (CJ-XXXX / JOJO-xxxx) o por #id numérico
// y, al encontrarlo, la pantalla navega a su detalle completo.
// La lógica vive en OrderViewModel.buscarPedido (desacoplada de la UI).
// =========================================================
@Composable
fun BuscadorPedidos(
    uiState: OrderUiState,
    viewModel: OrderViewModel,
    modifier: Modifier = Modifier,
    pista: String = "CJ-XXXX o #número"
) {
    var texto by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Buscar pedido") },
                placeholder = { Text(pista, fontSize = 13.sp) },
                singleLine = true,
                enabled = !uiState.buscandoPedido,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JoJoNaranjo,
                    unfocusedBorderColor = JoJoBorde,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = JoJoNaranjo,
                    focusedLabelColor = JoJoNaranjo,
                    unfocusedLabelColor = JoJoGrisMedio,
                    focusedContainerColor = JoJoSuperficie,
                    unfocusedContainerColor = JoJoSuperficie
                ),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { viewModel.buscarPedido(texto) },
                enabled = !uiState.buscandoPedido,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoJoNaranjo,
                    contentColor = Color.Black,
                    disabledContainerColor = JoJoDeshabilitado,
                    disabledContentColor = JoJoGrisMedio
                ),
                border = BorderStroke(1.dp, JoJoNaranjo),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 14.dp
                )
            ) {
                if (uiState.buscandoPedido) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(18.dp),
                        color = JoJoNaranjo,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Buscar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        uiState.errorBusqueda?.let { mensaje ->
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(text = mensaje, color = JoJoRojoClaro, fontSize = 13.sp)
        }
    }
}
