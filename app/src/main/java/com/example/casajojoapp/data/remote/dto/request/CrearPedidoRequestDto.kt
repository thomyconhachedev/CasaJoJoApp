package com.example.casajojoapp.data.remote.dto.request

import com.google.gson.annotations.SerializedName

// Cuerpo de POST /api/v1/pedidos (CrearPedidoRequest del backend).
// El servidor exige nombreCliente, horaRetiroSolicitada (HH:mm) e items;
// los precios NO viajan: el backend los toma de item_menu.
data class CrearPedidoRequestDto(
    @SerializedName("usuarioId") val usuarioId: Long? = null,
    @SerializedName("nombreCliente") val nombreCliente: String,
    @SerializedName("telefonoContacto") val telefonoContacto: String? = null,
    @SerializedName("horaRetiroSolicitada") val horaRetiroSolicitada: String,
    @SerializedName("observaciones") val observaciones: String? = null,
    @SerializedName("items") val items: List<ItemPedidoRequestDto>
)

data class ItemPedidoRequestDto(
    @SerializedName("itemMenuId") val itemMenuId: Long,
    @SerializedName("cantidad") val cantidad: Int = 1,
    @SerializedName("observacion") val observacion: String? = null,
    // CAMBIO PLATO ID: índice del plato al que pertenece (1..N dentro del
    // pedido). El backend debe persistirlo y devolverlo en detalle.platoId
    // para que la app pueda reagrupar las líneas por plato.
    @SerializedName("platoId") val platoId: Int? = null
)
