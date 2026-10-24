package com.example.casajojoapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// CAMBIO ROLES: usuarios de la app con rol (USUARIO/ADMIN). La contraseña
// NUNCA se guarda en claro: solo salt + hash PBKDF2 (util/PasswordHasher).
// INVITADO no es una fila: es la ausencia de sesión (Sesion.INVITADO).
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["username"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val rol: String, // Rol.name
    val nombre: String,
    val telefono: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)
