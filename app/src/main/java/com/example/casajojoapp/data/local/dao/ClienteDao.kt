package com.example.casajojoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.casajojoapp.data.local.entity.ClienteEntity

// CAMBIO CUENTAS: operaciones mínimas para un único perfil local.
@Dao
interface ClienteDao {

    @Query("SELECT * FROM clientes WHERE id = 1")
    suspend fun obtenerUnico(): ClienteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cliente: ClienteEntity)

    @Query("DELETE FROM clientes")
    suspend fun borrarTodos()
}
