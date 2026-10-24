package com.example.casajojoapp

import android.app.Application
import com.example.casajojoapp.data.ServicioLocator

// Inicializa el contenedor de dependencias ANTES de que cualquier
// ViewModel toque ServicioLocator (registrada en AndroidManifest).
class CasaJoJoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServicioLocator.inicializar(this)
    }
}
