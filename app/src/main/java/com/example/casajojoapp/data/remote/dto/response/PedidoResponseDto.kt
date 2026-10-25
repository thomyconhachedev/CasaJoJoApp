package com.example.casajojoapp.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// Respuesta de POST/GET /api/v1/pedidos (PedidoResponse del backend).
// El total es montoTotal (lo calcula el servidor); los detalles vienen planos.
data class PedidoResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("codigoSeguimiento") val codigoSeguimiento: String? = null,
    @SerializedName("estado") val estado: String,
    @SerializedName("horaRetiro") val horaRetiro: String,
    @SerializedName("nombreCliente") val nombreCliente: String? = null,
    @SerializedName("telefonoContacto") val telefonoContacto: String? = null,
    @SerializedName("observaciones") val observaciones: String? = null,
    @SerializedName("montoTotal") val montoTotal: Double = 0.0,
    @SerializedName("fechaCreacion") val fechaCreacion: String? = null,
    @SerializedName("detalles") val detalles: List<DetallePedidoResponseDto> = emptyList()
)

data class DetallePedidoResponseDto(
    @SerializedName("itemMenuId") val itemMenuId: Long? = null,
    @SerializedName("nombreItem") val nombreItem: String,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precioUnitario") val precioUnitario: Double,
    @SerializedName("observacion") val observacion: String? = null,
    // CAMBIO PLATO ID: devuelto por el backend para reagrupar por plato
    // (null con backends viejos => la app muestra las líneas planas).
    @SerializedName("platoId") val platoId: Int? = null
)
