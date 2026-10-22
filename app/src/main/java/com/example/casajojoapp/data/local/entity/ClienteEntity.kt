package com.example.casajojoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// CAMBIO CUENTAS: entidad Room que representa el perfil local del cliente.
// Se usa un id FIJO (1) porque solo se permite UN perfil por dispositivo;
// OnConflictStrategy.REPLACE sobrescribe el único registro existente.
@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val id: Long = 1,
    val nombre: String,
    val telefono: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)
