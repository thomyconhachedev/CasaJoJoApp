package com.example.casajojoapp.data.local.mappers

import com.example.casajojoapp.data.local.entity.ClienteEntity
import com.example.casajojoapp.domain.model.Cliente

// CAMBIO CUENTAS: mapeo entidad Room -> modelo de dominio.
fun ClienteEntity.aDominio(): Cliente = Cliente(
    id = id,
    nombre = nombre,
    telefono = telefono
)
