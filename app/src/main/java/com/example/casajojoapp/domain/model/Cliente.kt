package com.example.casajojoapp.domain.model

// Perfil local del cliente (un solo perfil por dispositivo).
// No incluye PIN/credenciales: guardamos solo datos de contacto para
// agilizar los pedidos.
data class Cliente(
    val id: Long = 0,
    val nombre: String,
    val telefono: String
)
