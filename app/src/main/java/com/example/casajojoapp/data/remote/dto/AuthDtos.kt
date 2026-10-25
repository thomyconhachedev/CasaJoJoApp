package com.example.casajojoapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// DTOs del módulo de autenticación del backend (AuthController):
//   POST /api/v1/auth/login    -> AuthResponseDto
//   POST /api/v1/auth/registro -> AuthResponseDto (201)
//   GET  /api/v1/auth/me       -> AuthResponseDto
// El token se guarda en TokenHolder y viaja en todas las peticiones.

data class LoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegistroRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("telefono") val telefono: String
)

data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("telefono") val telefono: String? = null
)
