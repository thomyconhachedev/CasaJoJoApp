package com.example.casajojoapp.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val categoria: CategoriaItem,
    val precio: Double,
    val disponible: Boolean = true,
    val imagenUrl: String? = null,
    // CAMBIO API: la descripcion viene del backend (ProductoDTO) y se muestra
    // en las tarjetas; el emoji NO lo envia el backend, asi que queda null en
    // modo API y la UI usa un fallback por categoria (ver PresentacionMappers).
    val descripcion: String? = null,
    val emoji: String? = null
)
enum class CategoriaItem {
    PROTEINA,
    ACOMPANAMIENTO,
    ENSALADA,
    POSTRE,
    BEBIDA,
    // GENERAL: categoría del backend (API.md) sin equivalente en el flujo
    // de armado; los productos así clasificados simplemente no se muestran
    // en las pantallas de selección (que filtran por categoría concreta).
    GENERAL
    // CAMBIO FLUJO: se ELIMINO el valor EXTRA. La pantalla de
    // "Agregados y Extras" (SeleccionAgregadosSC) fue reemplazada por
    // Postres y Bebidas, que usan las categorías POSTRE y BEBIDA.
}