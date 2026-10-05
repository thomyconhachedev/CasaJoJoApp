package com.example.casajojoapp.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.casajojoapp.R

@Composable
fun HeaderPrincipal(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.lacasajojo),
            contentDescription = "Logo La Casa JoJo",
            modifier = Modifier.size(35.dp).padding(horizontal = 8.dp, vertical = 12.dp)
        )

        Text(
            text = "La Casa JoJo",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Button(onClick = { navController.navigate("pantalla_uno") }) {
            Text("Cuenta")
        }
    }
}