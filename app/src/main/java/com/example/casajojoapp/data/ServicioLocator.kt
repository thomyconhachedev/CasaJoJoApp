package com.example.casajojoapp.data

import android.content.Context
import com.example.casajojoapp.data.local.AppDatabase
import com.example.casajojoapp.data.modo.ModoDatosRepository
import com.example.casajojoapp.data.repository.AuthRepository
import com.example.casajojoapp.data.repository.AuthRepositoryLocal
import com.example.casajojoapp.data.repository.ClienteRepository
import com.example.casajojoapp.data.repository.ClienteRepositoryImpl
import com.example.casajojoapp.data.repository.PedidoRepositoryLocal
import com.example.casajojoapp.util.Notificador

// =========================================================
// Contenedor de dependencias (Service Locator, sin Hilt):
// instancias ÚNICAS compartidas por todos los ViewModels. Sin esto, cada
// VM creaba su propia copia de ModoDatosRepository/AuthRepository y la
// sesión o el modo de datos no se veían entre pantallas.
// inicializar() se llama desde CasaJoJoApplication.onCreate().
// =========================================================
object ServicioLocator {

    private lateinit var contexto: Context

    fun inicializar(context: Context) {
        contexto = context.applicationContext
    }

    val baseDeDatos: AppDatabase by lazy { AppDatabase.obtener(contexto) }
    val modoDatos: ModoDatosRepository by lazy { ModoDatosRepository(contexto) }

    // Recurso nativo: notificaciones locales de cambio de estado.
    val notificador: Notificador by lazy { Notificador(contexto) }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryLocal(baseDeDatos.usuarioDao(), contexto)
    }

    val clienteRepository: ClienteRepository by lazy {
        ClienteRepositoryImpl(baseDeDatos.clienteDao())
    }

    // Cola LOCAL compartida: antes cada OrderViewModel tenía la suya propia.
    val pedidoRepositoryLocal: PedidoRepositoryLocal by lazy { PedidoRepositoryLocal() }
}
