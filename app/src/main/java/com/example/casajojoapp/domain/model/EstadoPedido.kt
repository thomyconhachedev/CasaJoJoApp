package com.example.casajojoapp.domain.model

// =========================================================
// Estados del pedido, ALINEADOS al backend (EstadoPedido.java):
// RECIBIDO -> EN_PREPARACION -> LISTO_PARA_RETIRO -> ENTREGADO.
// También se permite CANCELADO desde RECIBIDO o EN_PREPARACION para métricas.
// El backend solo admite avanzar de a un estado (409 en cualquier otro
// salto), por eso `siguiente` es la UNICA transición permitida y la cola
// de admin solo ofrece esa opción.
// =========================================================
enum class EstadoPedido(val nombre: String) {
    RECIBIDO("Recibido"),
    EN_PREPARACION("En preparación"),
    LISTO_PARA_RETIRO("Listo para retiro"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    /** Único estado al que puede pasar desde este (null si ya es el último). */
    val siguiente: EstadoPedido?
        get() = when (this) {
            RECIBIDO -> EN_PREPARACION
            EN_PREPARACION -> LISTO_PARA_RETIRO
            LISTO_PARA_RETIRO -> ENTREGADO
            ENTREGADO -> null
            CANCELADO -> null
        }

    companion object {
        /** Traduce el nombre que llega del backend; desconocido -> RECIBIDO. */
        fun desde(valor: String?): EstadoPedido =
            entries.firstOrNull { it.name == valor?.trim()?.uppercase() } ?: RECIBIDO
    }
}
