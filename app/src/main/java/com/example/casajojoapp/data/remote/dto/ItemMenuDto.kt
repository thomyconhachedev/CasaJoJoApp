package com.example.casajojoapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ItemMenuDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("disponible") val disponible: Boolean,
    @SerializedName("imagen_url") val imagenUrl: String? = null
)