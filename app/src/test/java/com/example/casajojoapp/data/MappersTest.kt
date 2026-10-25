package com.example.casajojoapp.data

import com.example.casajojoapp.data.local.mappers.aDominio
import com.example.casajojoapp.data.local.mappers.aEntity
import com.example.casajojoapp.data.mappers.aRequestDto
import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.LineaDetallePedido
import com.example.casajojoapp.domain.model.Pedido
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.domain.model.Producto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mappers: el pedido aplanado conserva el plato de origen (platoId) y la
 * observación por plato, y el paso por Room (entity) es fiel.
 */
class MappersTest {

    private fun producto(id: Long, cat: CategoriaItem, precio: Double = 0.0) =
        Producto(id = id, nombre = "Item $id", categoria = cat, precio = precio)

    private fun pedidoDeEjemplo() = Pedido(
        id = 0L,
        clienteNombre = "Ana Pérez",
        clienteTelefono = "+56912345678",
        horaRetiro = "13:00",
        platos = listOf(
            PlatoArmado(
                proteina = producto(1, CategoriaItem.PROTEINA),
                acompanamientos = listOf(
                    producto(2, CategoriaItem.ACOMPANAMIENTO),
                    producto(6, CategoriaItem.ACOMPANAMIENTO)
                ),
                ensalada = producto(7, CategoriaItem.ENSALADA),
                extras = listOf(producto(10, CategoriaItem.POSTRE, 1000.0)),
                notas = "sin sal"
            ),
            PlatoArmado(
                proteina = producto(1, CategoriaItem.PROTEINA),
                acompanamientos = listOf(producto(5, CategoriaItem.ACOMPANAMIENTO)),
                ensalada = null,
                notas = ""
            )
        ),
        total = 10600.0,
        notasRestricciones = "sin sal",
        estado = EstadoPedido.LISTO_PARA_RETIRO,
        codigoSeguimiento = "JOJO-0007",
        usuarioId = 42L
    )

    @Test
    fun cadaItemDelRequestLlevaElPlatoAlQuePertenece() {
        val dto = pedidoDeEjemplo().aRequestDto()

        // Plato 1: proteína + 2 acompañamientos + ensalada + 1 extra = 5.
        // Plato 2: proteína + 1 acompañamiento + sin ensalada = 2.
        assertEquals(7, dto.items.size)
        assertEquals(listOf(1, 1, 1, 1, 1, 2, 2), dto.items.map { it.platoId })

        // La observación del plato viaja en SUS líneas y no contamina al otro.
        assertEquals(listOf("sin sal", "sin sal", "sin sal", "sin sal", "sin sal", null, null),
            dto.items.map { it.observacion })

        assertEquals(42L, dto.usuarioId)
        assertEquals("13:00", dto.horaRetiroSolicitada)
        assertEquals("Ana Pérez", dto.nombreCliente)
    }

    @Test
    fun elPasajePorRoomEsFielAlPedidoOriginal() {
        val original = pedidoDeEjemplo()

        val entity = original.aEntity(origenActual = "LOCAL")
        assertEquals("LOCAL-0", entity.clave)
        assertEquals("LOCAL", entity.origen)

        val vuelta = entity.aDominio()

        assertEquals(original.id, vuelta.id)
        assertEquals(original.clienteNombre, vuelta.clienteNombre)
        assertEquals(original.horaRetiro, vuelta.horaRetiro)
        assertEquals(original.total, vuelta.total, 0.0)
        assertEquals(EstadoPedido.LISTO_PARA_RETIRO, vuelta.estado)
        assertEquals("JOJO-0007", vuelta.codigoSeguimiento)
        assertEquals(42L, vuelta.usuarioId)
        assertEquals("LOCAL", vuelta.origenRegistro)

        // Platos completos: combinaciones, precios y notas por plato.
        assertEquals(2, vuelta.platos.size)
        assertEquals("sin sal", vuelta.platos[0].notas)
        assertEquals("", vuelta.platos[1].notas)
        assertEquals(1, vuelta.platos[0].extras.size)
        assertEquals(1000.0, vuelta.platos[0].extras[0].precio, 0.0)
        assertNull(vuelta.platos[1].ensalada)
        assertEquals(2, vuelta.platos[0].acompanamientos.size)
    }

    @Test
    fun lasLineasDelBackendSeConservanEnElPasajePorRoom() {
        // Pedido estilo API: sin platos armados, solo líneas planas del backend.
        val conDetalles = pedidoDeEjemplo().copy(
            platos = emptyList(),
            detalles = listOf(
                LineaDetallePedido(1L, "Carne Mechada", 1, 500.0, "sin sal", platoId = 1),
                LineaDetallePedido(1L, "Carne Mechada", 1, 500.0, null, platoId = 2)
            )
        )

        val vuelta = conDetalles.aEntity("API").aDominio()

        assertEquals(2, vuelta.detalles.size)
        assertEquals(1, vuelta.detalles[0].platoId)
        assertEquals(2, vuelta.detalles[1].platoId)
        assertEquals("sin sal", vuelta.detalles[0].observacion)
        assertNull(vuelta.detalles[1].observacion)
        assertTrue(vuelta.platos.isEmpty())
    }
}
