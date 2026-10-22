package com.example.casajojoapp.data.repository

import com.example.casajojoapp.data.local.dao.ClienteDao
import com.example.casajojoapp.data.local.entity.ClienteEntity
import com.example.casajojoapp.data.local.mappers.aDominio
import com.example.casajojoapp.domain.model.Cliente

// CAMBIO CUENTAS: implementación local (Room) del perfil de cliente.
// Un solo perfil por dispositivo: siempre se escribe en el id fijo 1.
class ClienteRepositoryImpl(
    private val dao: ClienteDao
) : ClienteRepository {

    override suspend fun obtenerCliente(): Cliente? = dao.obtenerUnico()?.aDominio()

    override suspend fun guardar(nombre: String, telefono: String) {
        dao.insertar(
            ClienteEntity(id = ID_PERFIL, nombre = nombre.trim(), telefono = telefono.trim())
        )
    }

    override suspend fun actualizar(cliente: Cliente) {
        val existente = dao.obtenerUnico() ?: return
        dao.insertar(existente.copy(nombre = cliente.nombre.trim(), telefono = cliente.telefono.trim()))
    }

    override suspend fun eliminar() {
        dao.borrarTodos()
    }

    private companion object {
        const val ID_PERFIL = 1L
    }
}
