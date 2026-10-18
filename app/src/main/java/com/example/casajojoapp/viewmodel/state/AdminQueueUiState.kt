package com.example.casajojoapp.viewmodel.state

import com.example.casajojoapp.domain.model.Pedido

data class AdminQueueUiState(
    val pedidosCola: List<Pedido> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pedidoSeleccionadoId: Long? = null
)