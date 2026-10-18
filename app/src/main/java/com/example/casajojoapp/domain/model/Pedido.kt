package com.example.casajojoapp.domain.model

data class Pedido(
    val id: Long = 0,
    val clienteNombre: String,
    val clienteTelefono: String,
    val platos: List<PlatoArmado>,
    val horaRetiro: String,
    val notasRestricciones: String? = null,
    val estadoPedido: EstadoPedido = EstadoPedido.RECIBIDO,
    val estadoPago: EstadoPago = EstadoPago.NO_PAGADO,
    val total: Double = platos.sumOf { it.precioTotal }
)

enum class EstadoPedido {
    RECIBIDO,
    EN_PREPARACION,
    LISTO_PARA_RETIRO,
    ENTREGADO,
    CANCELADO
}

enum class EstadoPago {
    NO_PAGADO,
    PAGO_PENDIENTE,
    PAGADO
}