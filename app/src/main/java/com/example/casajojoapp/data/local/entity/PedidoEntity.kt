package com.example.casajojoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// CAMBIO HISTORIAL: pedidos persistidos localmente (rubrica A9/D1/D2).
// Un solo registro por (origen-id) para que un pedido LOCAL y uno de la API
// con el mismo id numérico no colisionen. Los platos y las líneas de detalle
// se guardan serializados en JSON (Gson): son listas anidadas de modelos de
// dominio puros, no hace falta modelarlas relationalmente en una MVP.
@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val clave: String, // "${origen}-${id}"
    val id: Long,
    val origen: String, // ModoDatos.name donde se registró (LOCAL/API)
    val clienteNombre: String,
    val clienteTelefono: String,
    val horaRetiro: String,
    val total: Double,
    val notasRestricciones: String?,
    val estado: String, // EstadoPedido.name
    val codigoSeguimiento: String?,
    val usuarioId: Long?,
    val platosJson: String, // List<PlatoArmado>
    val detallesJson: String, // List<LineaDetallePedido>
    val fechaRegistro: Long = System.currentTimeMillis()
)
