package com.example.casajojoapp.data.remote

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Fábrica del ApiService. Se crea bajo demanda con la BASE_URL vigente
// (que puede cambiar en runtime desde el panel Admin en modo API).
object NetworkModule {

    fun crearApiService(baseUrl: String): ApiService {
        val gson = GsonBuilder().create()
        return Retrofit.Builder()
            .baseUrl(baseUrl) // debe terminar en "/" (lo garantiza ModoDatosRepository)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
