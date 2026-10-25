package com.example.casajojoapp.data.remote

import okhttp3.Interceptor
import okhttp3.Response

// Adjunta "Authorization: Bearer <token>" cuando hay sesión de backend.
// Sin token (invitado o sin login) la petición sale anónima: el backend
// trata el POST /pedidos como pedido de invitado.
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenHolder.token
        val peticion = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(peticion)
    }
}
