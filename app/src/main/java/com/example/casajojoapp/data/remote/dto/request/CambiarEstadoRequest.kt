package com.example.casajojoapp.data.remote.dto.request

import com.google.gson.annotations.SerializedName

// Body del PATCH /api/v1/pedidos/{id}/estado.
// "estado" usa el enum del backend: RECIBIDO, EN_PREPARACION,
// LISTO_PARA_RETIRO, ENTREGADO (solo se admite avanzar de a uno).
data class CambiarEstadoRequest(
    @SerializedName("estado") val estado: String
)
