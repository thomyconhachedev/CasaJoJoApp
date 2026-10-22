package com.example.casajojoapp.data.repository

import com.example.casajojoapp.data.mappers.aDominio
import com.example.casajojoapp.data.mappers.aRequestDto
import com.example.casajojoapp.data.remote.ApiService
import com.example.casajojoapp.data.remote.dto.request.CambiarEstadoRequest
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.Pedido

// =========================================================
// Repositorio de PEDIDOS con doble implementación:
// - Local: cola en memoria (se pierde al reiniciar; default).
// - Api: consume el backend (POST/GET/PATCH de API.md §4).
// El selector (Repositorios) elige según el ModoDatos.
// =========================================================
interface PedidoRepository {
    suspend fun listarPedidos(): List<Pedido>
    suspend fun crearPedido(pedido: Pedido): Pedido
    suspend fun cambiarEstado(id: Long, nuevoEstado: EstadoPedido)
}

// Cola en memoria (modo LOCAL). No persiste entre reinicios.
class PedidoRepositoryLocal : PedidoRepository {

    private val pedidos = mutableListOf<Pedido>()

    override suspend fun listarPedidos(): List<Pedido> = pedidos.toList()

    override suspend fun crearPedido(pedido: Pedido): Pedido {
        pedidos.add(pedido)
        return pedido
    }

    override suspend fun cambiarEstado(id: Long, nuevoEstado: EstadoPedido) {
        val indice = pedidos.indexOfFirst { it.id == id }
        if (indice >= 0) {
            pedidos[indice] = pedidos[indice].copy(estado = nuevoEstado)
        }
    }
}

// Contra el backend (modo API).
class PedidoRepositoryApi(private val api: ApiService) : PedidoRepository {

    override suspend fun listarPedidos(): List<Pedido> =
        api.listarPedidos().map { it.aDominio() }

    override suspend fun crearPedido(pedido: Pedido): Pedido =
        api.crearPedido(pedido.aRequestDto()).aDominio()

    override suspend fun cambiarEstado(id: Long, nuevoEstado: EstadoPedido) {
        api.cambiarEstadoPedido(id, CambiarEstadoRequest(nuevoEstado.name))
    }
}
