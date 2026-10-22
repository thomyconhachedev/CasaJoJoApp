package com.example.casajojoapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// Una línea del menú tal como la devuelve GET /api/v1/menu (ItemMenuResponse
// del backend): precio BigDecimal -> Double, stock del día y disponibilidad.
data class ProductoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("precio") val precio: Double = 0.0,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("disponible") val disponible: Boolean = true
)
