package com.example.casajojoapp.ui.cliente.armado

import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.Producto
import com.example.casajojoapp.ui.componentes.Proteina

// =========================================================
// CAMBIO MVVM: mapeo de los modelos visuales de las pantallas
// (Proteina, Acompanamiento, Ensalada, PostreBebida) al modelo de
// dominio Producto, que es lo que vive en OrderUiState / PlatoArmado.
//
// NOTA: id se parsea de String ("1") a Long (1L) porque Producto.id
// es Long (preparado para el id real que viene de la API).
//
// CAMBIO PRECIOS: proteína, acompañamiento y ensalada van con
// precio = 0.0 porque NO tienen precio propio: el plato cuesta
// $4.800 fijo (PlatoArmado.PRECIO_BASE). Solo los extras
// (postres y bebidas) traen precio real y suman aparte.
// =========================================================

fun Proteina.toProducto(): Producto = Producto(
    id = id.toLongOrNull() ?: 0L,
    nombre = nombre,
    categoria = CategoriaItem.PROTEINA,
    precio = 0.0, // CAMBIO PRECIOS: incluida en el plato base de $4.800
    disponible = estaDisponible,
    imagenUrl = null // sin foto todavía: la UI muestra emoji (Sprint 3)
)

fun Acompanamiento.toProducto(): Producto = Producto(
    id = id.toLongOrNull() ?: 0L,
    nombre = nombre,
    categoria = CategoriaItem.ACOMPANAMIENTO,
    precio = 0.0, // CAMBIO PRECIOS: los 2 acompañamientos están incluidos en el plato base
    disponible = estaDisponible,
    imagenUrl = null
)

fun Ensalada.toProducto(): Producto = Producto(
    id = id.toLongOrNull() ?: 0L,
    nombre = nombre,
    categoria = CategoriaItem.ENSALADA,
    precio = 0.0, // la ensalada está incluida en el plato base
    disponible = estaDisponible,
    imagenUrl = null
)

// CAMBIO FLUJO: postres y bebidas son los ÚNICOS extras del plato;
// conservan su precio real porque suman aparte al plato de $4.800.
fun PostreBebida.toProducto(): Producto = Producto(
    id = id.toLongOrNull() ?: 0L,
    nombre = nombre,
    categoria = categoria, // POSTRE o BEBIDA
    precio = precio.toDouble(),
    disponible = estaDisponible,
    imagenUrl = null
)
