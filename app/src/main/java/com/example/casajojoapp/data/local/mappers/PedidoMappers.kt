package com.example.casajojoapp.data.local.mappers

import com.example.casajojoapp.data.local.entity.PedidoEntity
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.LineaDetallePedido
import com.example.casajojoapp.domain.model.Pedido
import com.example.casajojoapp.domain.model.PlatoArmado
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// CAMBIO HISTORIAL: Pedido de dominio <-> PedidoEntity. Las listas anidadas
// (platos y detalles) viajan como JSON con Gson: los modelos de dominio son
// puros (sin Android), así que se serializan sin problemas.
private val gson = Gson()

fun Pedido.aEntity(origenActual: String): PedidoEntity = PedidoEntity(
    clave = "${origenRegistro ?: origenActual}-$id",
    id = id,
    origen = origenRegistro ?: origenActual,
    clienteNombre = clienteNombre,
    clienteTelefono = clienteTelefono,
    horaRetiro = horaRetiro,
    total = total,
    notasRestricciones = notasRestricciones,
    estado = estado.name,
    codigoSeguimiento = codigoSeguimiento,
    usuarioId = usuarioId,
    platosJson = gson.toJson(platos),
    detallesJson = gson.toJson(detalles),
    fechaRegistro = System.currentTimeMillis()
)

fun PedidoEntity.aDominio(): Pedido = Pedido(
    id = id,
    clienteNombre = clienteNombre,
    clienteTelefono = clienteTelefono,
    horaRetiro = horaRetiro,
    platos = parsearLista(platosJson),
    total = total,
    notasRestricciones = notasRestricciones,
    estado = EstadoPedido.desde(estado),
    codigoSeguimiento = codigoSeguimiento,
    detalles = parsearLista(detallesJson),
    usuarioId = usuarioId,
    origenRegistro = origen
)

private inline fun <reified T> parsearLista(json: String): List<T> =
    if (json.isBlank()) emptyList()
    else gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
