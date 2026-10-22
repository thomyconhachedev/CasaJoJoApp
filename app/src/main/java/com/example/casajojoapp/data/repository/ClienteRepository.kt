package com.example.casajojoapp.data.repository

import com.example.casajojoapp.domain.model.Cliente

// CAMBIO CUENTAS: abstracción del origen de datos del perfil. Hoy se
// implementa con Room (local); cuando exista el backend se agrega una
// implementación remota sin tocar la UI.
interface ClienteRepository {
    suspend fun obtenerCliente(): Cliente?
    suspend fun guardar(nombre: String, telefono: String)
    suspend fun actualizar(cliente: Cliente)
    suspend fun eliminar()
}
