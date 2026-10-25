package com.example.casajojoapp.data.mappers

import com.example.casajojoapp.data.remote.dto.ProductoDto
import com.example.casajojoapp.data.remote.dto.request.CrearPedidoRequestDto
import com.example.casajojoapp.data.remote.dto.response.PedidoResponseDto
import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.LineaDetallePedido
import com.example.casajojoapp.domain.model.Pedido
import com.example.casajojoapp.domain.model.Producto
import com.example.casajojoapp.data.remote.dto.request.ItemPedidoRequestDto

// =========================================================
// Mappers entre los DTOs remotos (backend Spring Boot) y los modelos de
// dominio. Los nombres de campo son los de los records del backend.
// =========================================================

// --- MENÚ ---

// ItemMenuResponse (GET /api/v1/menu) -> Producto (dominio).
// El emoji no viene del backend, queda null (la UI aplica un fallback).
fun ProductoDto.aDominio(): Producto = Producto(
    id = id,
    nombre = nombre,
    categoria = categoria.aCategoriaItem(),
    precio = precio,
    disponible = disponible,
    descripcion = descripcion
)

private fun String.aCategoriaItem(): CategoriaItem = when (this.trim().uppercase()) {
    "PROTEINA" -> CategoriaItem.PROTEINA
    "ACOMPANAMIENTO" -> CategoriaItem.ACOMPANAMIENTO
    "ENSALADA" -> CategoriaItem.ENSALADA
    "POSTRE" -> CategoriaItem.POSTRE
    "BEBIDA", "BEBIDA_POSTRE" -> CategoriaItem.BEBIDA
    else -> CategoriaItem.GENERAL
}

// --- PEDIDOS ---

// PedidoResponse (POST/GET /api/v1/pedidos) -> Pedido (dominio).
// El backend no agrupa en "platos": `detalles` queda plano y el TOTAL es el
// montoTotal calculado por el servidor (no se recalcula en la app).
fun PedidoResponseDto.aDominio(): Pedido = Pedido(
    id = id,
    clienteNombre = nombreCliente.orEmpty(),
    clienteTelefono = telefonoContacto.orEmpty(),
    horaRetiro = horaRetiro,
    platos = emptyList(),
    total = montoTotal,
    notasRestricciones = observaciones,
    estado = EstadoPedido.desde(estado),
    codigoSeguimiento = codigoSeguimiento,
    detalles = detalles.map {
        LineaDetallePedido(
            itemMenuId = it.itemMenuId,
            nombreItem = it.nombreItem,
            cantidad = it.cantidad,
            precioUnitario = it.precioUnitario,
            observacion = it.observacion
        )
    }
)

// Pedido (dominio) -> CrearPedidoRequest (body del POST /api/v1/pedidos).
// Aplana los platos en ítems con itemMenuId/cantidad; los precios NO viajan
// porque el backend los toma de su propio menú.
// CAMBIO ROLES: usuarioId se envía solo cuando hay sesión (null = invitado).
fun Pedido.aRequestDto(): CrearPedidoRequestDto = CrearPedidoRequestDto(
    usuarioId = usuarioId,
    nombreCliente = clienteNombre,
    telefonoContacto = clienteTelefono.ifBlank { null },
    horaRetiroSolicitada = horaRetiro,
    observaciones = notasRestricciones,
    items = platos.flatMap { plato ->
        val obs = plato.notas.trim().ifBlank { null }
        listOfNotNull(plato.proteina) + plato.acompanamientos +
            listOfNotNull(plato.ensalada) + plato.extras
    }.mapIndexed { idx, producto ->
        // Asignar observación del plato al que pertenece
        val platoIndex = obtenerPlatoIndexParaItem(idx)
        val obsPlato = if (platoIndex >= 0 && platoIndex < platos.size) {
            platos[platoIndex].notas.trim().ifBlank { null }
        } else null
        com.example.casajojoapp.data.remote.dto.request.ItemPedidoRequestDto(itemMenuId = producto.id, cantidad = 1, observacion = obsPlato)
    }
)

private fun Pedido.obtenerPlatoIndexParaItem(itemIndex: Int): Int {
    var acumulado = 0
    for (i in platos.indices) {
        val plato = platos[i]
        val count = (if (plato.proteina != null) 1 else 0) + plato.acompanamientos.size + (if (plato.ensalada != null) 1 else 0) + plato.extras.size
        if (itemIndex >= acumulado && itemIndex < acumulado + count) {
            return i
        }
        acumulado += count
    }
    return -1
}
