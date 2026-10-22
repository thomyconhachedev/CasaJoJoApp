package com.example.casajojoapp.data.local

import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.Producto

// =========================================================
// MENU LOCAL (modo LOCAL): fuente hardcodeada de productos, equivalente a
// la que antes vivia hardcodeada en las 4 pantallas de seleccion. Sirve de
// respaldo cuando el interruptor de datos esta en LOCAL (sin backend).
// Los IDs se conservan iguales a los de antes (por categoria) para no
// cambiar el comportamiento local.
// NOTA: "Sin Ensalada" NO es un Producto de dominio; lo maneja la pantalla
// de ensaladas como una opcion especial (id "0").
// =========================================================
object MenuLocalDataSource {

    val proteinas: List<Producto> = listOf(
        Producto(1L, "Carne Mechada", CategoriaItem.PROTEINA, 0.0, true, null, "Vacuno cocción lenta 4 hrs", "🥩"),
        Producto(2L, "Pollo al Horno", CategoriaItem.PROTEINA, 0.0, true, null, "Muslo marinado al limón y romero", "🍗"),
        Producto(3L, "Salmon cocido", CategoriaItem.PROTEINA, 0.0, true, null, "Filete cocido con tomate y verduras", "🐟"),
        Producto(4L, "Hamburguesa", CategoriaItem.PROTEINA, 0.0, false, null, "Hamburguesa dorada en mantequilla", "🍔"),
        Producto(5L, "Hamburguesa", CategoriaItem.PROTEINA, 0.0, false, null, "Hamburguesa dorada en mantequilla", "🍔"),
        Producto(6L, "Pescado Frito", CategoriaItem.PROTEINA, 0.0, false, null, "Reineta fresca del día", "🐟")
    )

    val acompanamientos: List<Producto> = listOf(
        Producto(1L, "Papas Fritas", CategoriaItem.ACOMPANAMIENTO, 0.0, true, null, "Crujientes y doradas", "🍟"),
        Producto(2L, "Arroz Blanco", CategoriaItem.ACOMPANAMIENTO, 0.0, true, null, "Al estilo casero", "🍚"),
        Producto(3L, "Ensalada de Lechuga", CategoriaItem.ACOMPANAMIENTO, 0.0, true, null, "Lechuga fresca con limón", "🥬"),
        Producto(4L, "Puré de Papas", CategoriaItem.ACOMPANAMIENTO, 0.0, true, null, "Cremoso con mantequilla", "🥔"),
        Producto(5L, "Fideos al Ajillo", CategoriaItem.ACOMPANAMIENTO, 0.0, true, null, "Ajo y perejil", "🍝"),
        Producto(6L, "Quinoa", CategoriaItem.ACOMPANAMIENTO, 0.0, false, null, "Mezcla del día", "🌾")
    )

    val ensaladas: List<Producto> = listOf(
        Producto(1L, "Ensalada Chilena", CategoriaItem.ENSALADA, 0.0, true, null, "Tomate fresco, cebolla pluma y cilantro", "🥗"),
        Producto(2L, "Ensalada Mixta", CategoriaItem.ENSALADA, 0.0, true, null, "Lechuga, tomate y choclo", "🥬"),
        Producto(3L, "Ensalada de Apio", CategoriaItem.ENSALADA, 0.0, true, null, "Apio crujiente con palta", "🥑")
    )

    // Extras (postres y bebidas): los ÚNICOS con precio propio (suman al plato).
    val extras: List<Producto> = listOf(
        Producto(1L, "Leche Asada", CategoriaItem.POSTRE, 1200.0, true, null, "Postre casero de la casa", "🍮"),
        Producto(2L, "Kuchén", CategoriaItem.POSTRE, 1500.0, true, null, "Porción individual", "🍰"),
        Producto(3L, "Helado", CategoriaItem.POSTRE, 1300.0, true, null, "Dos bochas a elección", "🍨"),
        Producto(4L, "Agua Mineral", CategoriaItem.BEBIDA, 800.0, true, null, "Botella 500 ml", "💧"),
        Producto(5L, "Jugo Natural", CategoriaItem.BEBIDA, 1500.0, true, null, "Naranja o limón", "🧃"),
        Producto(6L, "Gaseosa", CategoriaItem.BEBIDA, 1200.0, true, null, "Lata 355 ml", "🥤"),
        Producto(7L, "Café", CategoriaItem.BEBIDA, 1000.0, true, null, "Americano o con leche", "☕")
    )
}
