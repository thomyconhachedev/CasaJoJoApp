package com.example.casajojoapp.ui.cliente.armado

import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.Producto
import com.example.casajojoapp.ui.componentes.Proteina

// =========================================================
// Mappers de PRESENTACIÓN: Producto (dominio) -> modelos visuales de las
// tarjetas (Proteina, Acompanamiento, Ensalada, PostreBebida).
// El backend NO envía emoji, así que si el Producto viene sin él (modo API)
// se aplica un fallback por categoría para no degradar la UI.
// La selección en las pantallas pasa el Producto de dominio DIRECTO al
// ViewModel (sin volver a mapear), preservando id/precio/descripción.
// =========================================================

private fun emojiDe(categoria: CategoriaItem): String = when (categoria) {
    CategoriaItem.PROTEINA -> "🥩"
    CategoriaItem.ACOMPANAMIENTO -> "🍚"
    CategoriaItem.ENSALADA -> "🥗"
    CategoriaItem.POSTRE -> "🍮"
    CategoriaItem.BEBIDA -> "🥤"
    CategoriaItem.GENERAL -> "🍽️"
}

fun Producto.aProteinaVisual(): Proteina = Proteina(
    id = id.toString(),
    nombre = nombre,
    descripcion = descripcion.orEmpty(),
    emoji = emoji ?: emojiDe(categoria),
    estaDisponible = disponible
)

fun Producto.aAcompanamientoVisual(): Acompanamiento = Acompanamiento(
    id = id.toString(),
    nombre = nombre,
    descripcion = descripcion.orEmpty(),
    emoji = emoji ?: emojiDe(categoria),
    estaDisponible = disponible
)

fun Producto.aEnsaladaVisual(): Ensalada = Ensalada(
    id = id.toString(),
    nombre = nombre,
    descripcion = descripcion.orEmpty(),
    emoji = emoji ?: emojiDe(categoria),
    estaDisponible = disponible
)

fun Producto.aPostreBebidaVisual(): PostreBebida = PostreBebida(
    id = id.toString(),
    nombre = nombre,
    descripcion = descripcion.orEmpty(),
    precio = precio.toInt(),
    emoji = emoji ?: emojiDe(categoria),
    categoria = categoria,
    estaDisponible = disponible
)
