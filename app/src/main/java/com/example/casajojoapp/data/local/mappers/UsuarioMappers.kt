package com.example.casajojoapp.data.local.mappers

import com.example.casajojoapp.data.local.entity.UsuarioEntity
import com.example.casajojoapp.domain.model.Rol
import com.example.casajojoapp.domain.model.Sesion
import com.example.casajojoapp.domain.model.Usuario

// CAMBIO ROLES: mapeo entidad Room -> dominio. El rol se persiste como
// string y cualquier valor desconocido degrada a USUARIO (nunca a ADMIN,
// por seguridad).
fun UsuarioEntity.aDominio(): Usuario = Usuario(
    id = id,
    username = username,
    rol = aRol(),
    nombre = nombre,
    telefono = telefono
)

fun UsuarioEntity.aSesion(): Sesion = Sesion(
    usuarioId = id,
    username = username,
    rol = aRol(),
    nombre = nombre,
    telefono = telefono
)

private fun UsuarioEntity.aRol(): Rol =
    Rol.entries.firstOrNull { it.name == rol && it != Rol.INVITADO } ?: Rol.USUARIO
