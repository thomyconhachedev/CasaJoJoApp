package com.example.casajojoapp.data.remote

import com.example.casajojoapp.data.remote.dto.ProductoDto
import com.google.gson.annotations.SerializedName

data class MenuDiarioDto(
    @SerializedName("fecha") val fecha: String = "",
    @SerializedName("estado") val estado: String = "",
    @SerializedName("items") val items: List<ProductoDto> = emptyList()
)
