package com.example.casajojoapp.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CrearPedidoRequestDto(
    @SerializedName("cliente_nombre") val clienteNombre: String,
    @SerializedName("cliente_telefono") val clienteTelefono: String,
    @SerializedName("hora_retiro") val horaRetiro: String,
    @SerializedName("notas_restricciones") val notasRestricciones: String?,
    @SerializedName("detalles") val detalles: List<DetallePedidoRequestDto>
)

data class DetallePedidoRequestDto(
    @SerializedName("item_menu_id") val itemMenuId: Long,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: Double
)