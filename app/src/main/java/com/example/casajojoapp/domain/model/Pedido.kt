package com.example.casajojoapp.domain.model

// =========================================================
// Snapshot de un pedido, ya sea creado en local o devuelto por el backend.
// En modo API el backend no agrupa los detalles en "platos": vienen planos
// (DetallePedidoResponse), por eso `platos` queda vacío y `detalles` trae
// las líneas reales que guardó el servidor.
// =========================================================
data class Pedido(
    val id: Long,
    val clienteNombre: String,
    val clienteTelefono: String,
    val horaRetiro: String,
    val platos: List<PlatoArmado>,
    val total: Double,
    val notasRestricciones: String?,
    val estado: EstadoPedido = EstadoPedido.RECIBIDO,
    val codigoSeguimiento: String? = null,
    // Líneas planas devueltas por el backend (vacío en modo LOCAL).
    val detalles: List<LineaDetallePedido> = emptyList()
) {
    /** Unidades a preparar: platos armados en local, suma de cantidades en API. */
    val unidades: Int
        get() = if (detalles.isNotEmpty()) detalles.sumOf { it.cantidad } else platos.size
}

// Línea de un pedido tal como la persiste el backend (precio congelado al comprar).
data class LineaDetallePedido(
    val itemMenuId: Long?,
    val nombreItem: String,
    val cantidad: Int,
    val precioUnitario: Double
)
