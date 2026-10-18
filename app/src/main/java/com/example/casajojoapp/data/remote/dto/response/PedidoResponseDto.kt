package com.example.casajojoapp.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PedidoResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("cliente_nombre") val clienteNombre: String,
    @SerializedName("cliente_telefono") val clienteTelefono: String,
    @SerializedName("hora_retiro") val horaRetiro: String,
    @SerializedName("notas_restricciones") val notasRestricciones: String?,
    @SerializedName("estado_pedido") val estadoPedido: String,
    @SerializedName("estado_pago") val estadoPago: String,
    @SerializedName("creado_en") val creadoEn: String,
    @SerializedName("detalles") val detalles: List<DetallePedidoResponseDto> = emptyList()
)

data class DetallePedidoResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("item_nombre") val itemNombre: String,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double
)