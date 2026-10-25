package com.example.casajojoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.casajojoapp.data.local.entity.PedidoEntity

// CAMBIO HISTORIAL: operaciones mínimas sobre el historial local de pedidos.
@Dao
interface PedidoDao {

    // REPLACE por clave: re-sent/estado actualizado sobrescribe el registro.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pedido: PedidoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTodos(pedidos: List<PedidoEntity>)

    @Query("SELECT * FROM pedidos ORDER BY fechaRegistro DESC")
    suspend fun listar(): List<PedidoEntity>

    // Solo los creados en modo LOCAL (para restaurar la cola en memoria).
    @Query("SELECT * FROM pedidos WHERE origen = :origen ORDER BY fechaRegistro DESC")
    suspend fun listarPorOrigen(origen: String): List<PedidoEntity>
}
