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
    usuarioId = usuarioId,
    detalles = detalles.map {
        LineaDetallePedido(
            itemMenuId = it.itemMenuId,
            nombreItem = it.nombreItem,
            cantidad = it.cantidad,
            precioUnitario = it.precioUnitario,
            observacion = it.observacion,
            platoId = it.platoId
        )
    }
)

// Pedido (dominio) -> CrearPedidoRequest (body del POST /api/v1/pedidos).
// Aplana los platos en ítems con itemMenuId/cantidad; los precios NO viajan
// porque el backend los toma de su propio menú.
// CAMBIO ROLES: usuarioId se envía solo cuando hay sesión (null = invitado).
// CAMBIO PLATO ID: cada ítem lleva `platoId` (1..N) para que el backend lo
// persista y lo devuelva en `detalle.platoId`: así la app puede reagrupar
// las líneas por plato (la restricción viaja además en `observacion`).
// Reemplaza al viejo mapIndexed + obtenerPlatoIndexParaItem.
fun Pedido.aRequestDto(): CrearPedidoRequestDto = CrearPedidoRequestDto(
    usuarioId = usuarioId,
    nombreCliente = clienteNombre,
    telefonoContacto = clienteTelefono.ifBlank { null },
    horaRetiroSolicitada = horaRetiro,
    observaciones = notasRestricciones,
    items = platos.flatMapIndexed { indicePlato, plato ->
        val obsPlato = plato.notas.trim().ifBlank { null }
        val productosDelPlato = listOfNotNull(plato.proteina) +
            plato.acompanamientos +
            listOfNotNull(plato.ensalada) +
            plato.extras
        productosDelPlato.map { producto ->
            ItemPedidoRequestDto(
                itemMenuId = producto.id,
                cantidad = 1,
                observacion = obsPlato,
                platoId = indicePlato + 1 // agrupación 1..N por pedido
            )
        }
    }
)
