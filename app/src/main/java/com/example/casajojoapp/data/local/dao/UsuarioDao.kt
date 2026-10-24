package com.example.casajojoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.casajojoapp.data.local.entity.UsuarioEntity

// CAMBIO ROLES: operaciones mínimas para el sistema local de usuarios.
@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE username = :username LIMIT 1")
    suspend fun buscarPorUsername(username: String): UsuarioEntity?

    // ABORT: un username repetido lanza SQLiteConstraintException que la
    // capa de auth traduce a "ese usuario ya existe".
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("UPDATE usuarios SET nombre = :nombre, telefono = :telefono WHERE id = :id")
    suspend fun actualizarContacto(id: Long, nombre: String, telefono: String)
}
