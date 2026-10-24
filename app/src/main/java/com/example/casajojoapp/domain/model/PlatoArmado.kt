package com.example.casajojoapp.domain.model

data class PlatoArmado(
    val proteina: Producto? = null,
    val acompanamientos: List<Producto> = emptyList(),
    val ensalada: Producto? = null,
    val extras: List<Producto> = emptyList(),
    val notas: String = "",
    // ANIMACIÓN: identidad estable del plato para las animaciones de
    // inserción/eliminación de la lista del carrito (key de LazyColumn).
    // .copy() conserva la clave; cada plato nuevo nace con UUID propio.
    val clave: String = java.util.UUID.randomUUID().toString()
) {
    companion object {
        // =========================================================
        // CAMBIO PRECIOS (decisión de diseño):
        // - El plato tiene precio FIJO de $4.800 = 1 proteína +
        //   2 acompañamientos + 1 ensalada (sin precio por ítem).
        // - Solo los extras (postres y bebidas) suman aparte.
        // Reemplazar cuando el precio venga de la API (Sprint 2/4).
        // =========================================================
        const val PRECIO_BASE = 4800.0
    }

    val precioTotal: Double
        get() = PRECIO_BASE + extras.sumOf { it.precio }
}
