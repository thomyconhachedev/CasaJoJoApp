package com.example.casajojoapp.data.remote

// Holder en memoria del token JWT del backend. Lo escribe AuthRepositoryApi
// (login/registro/restauración) y lo lee AuthInterceptor para cada petición.
// Sin Context: el persistido vive en las prefs del repositorio de auth.
object TokenHolder {
    @Volatile
    var token: String? = null
}
