package com.example.casajojoapp.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val categoria: CategoriaItem,
    val precio: Double,
    val disponible: Boolean = true,
    val imagenUrl: String? = null
)
enum class CategoriaItem {
    PROTEINA,
    ACOMPANAMIENTO,
    ENSALADA,
    POSTRE,
    BEBIDA
    // CAMBIO FLUJO: se ELIMINO el valor EXTRA. La pantalla de
    // "Agregados y Extras" (SeleccionAgregadosSC) fue reemplazada por
    // Postres y Bebidas, que usan las categorías POSTRE y BEBIDA.
}