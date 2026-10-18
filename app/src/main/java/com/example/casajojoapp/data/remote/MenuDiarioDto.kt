package com.example.casajojoapp.data.remote

import com.example.casajojoapp.data.remote.dto.ItemMenuDto
import com.google.gson.annotations.SerializedName

data class MenuDiarioDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("items") val items: List<ItemMenuDto> = emptyList()
)