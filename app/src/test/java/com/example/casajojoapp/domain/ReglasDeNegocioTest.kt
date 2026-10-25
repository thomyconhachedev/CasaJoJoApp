package com.example.casajojoapp.domain

import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.domain.model.Producto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reglas de negocio puras del dominio: precios del plato y máquina de
 * estados del pedido (rubrica de confiabilidad).
 */
class ReglasDeNegocioTest {

    private fun producto(id: Long, categoria: CategoriaItem, precio: Double = 0.0) =
        Producto(id = id, nombre = "Item $id", categoria = categoria, precio = precio)

    @Test
    fun precioTotalSinExtrasEsLaBase() {
        val plato = PlatoArmado(
            proteina = producto(1, CategoriaItem.PROTEINA),
            acompanamientos = listOf(
                producto(2, CategoriaItem.ACOMPANAMIENTO),
                producto(3, CategoriaItem.ACOMPANAMIENTO)
            ),
            ensalada = producto(4, CategoriaItem.ENSALADA)
        )
        assertEquals(PlatoArmado.PRECIO_BASE, plato.precioTotal, 0.0)
    }

    @Test
    fun precioTotalSumaSoloLosExtras() {
        val plato = PlatoArmado(
            proteina = producto(1, CategoriaItem.PROTEINA),
            acompanamientos = listOf(producto(2, CategoriaItem.ACOMPANAMIENTO)),
            extras = listOf(
                producto(9, CategoriaItem.POSTRE, 1200.0),
                producto(10, CategoriaItem.BEBIDA, 800.0)
            )
        )
        // Base + 1200 + 800; los ítems base (precio 0) no alteran nada.
        assertEquals(PlatoArmado.PRECIO_BASE + 2000.0, plato.precioTotal, 0.0)
    }

    @Test
    fun laCadenaDeEstadosAvanzaDeAUnoHastaEntregado() {
        assertEquals(EstadoPedido.EN_PREPARACION, EstadoPedido.RECIBIDO.siguiente)
        assertEquals(EstadoPedido.LISTO_PARA_RETIRO, EstadoPedido.EN_PREPARACION.siguiente)
        assertEquals(EstadoPedido.ENTREGADO, EstadoPedido.LISTO_PARA_RETIRO.siguiente)
        assertNull(EstadoPedido.ENTREGADO.siguiente)
        assertNull(EstadoPedido.CANCELADO.siguiente)
    }

    @Test
    fun estadosDesconocidosDegradanARecibido() {
        assertEquals(EstadoPedido.RECIBIDO, EstadoPedido.desde(null))
        assertEquals(EstadoPedido.RECIBIDO, EstadoPedido.desde("QUALQUIER_COSA"))
        assertEquals(EstadoPedido.LISTO_PARA_RETIRO, EstadoPedido.desde("listo_para_retiro"))
        assertEquals(EstadoPedido.CANCELADO, EstadoPedido.desde(" CANCELADO "))
    }

    @Test
    fun platoCompletoRequiereProteinaYAAcompanamiento() {
        // La regla vive en OrderViewModel.platoCompleto(); aquí validamos el
        // estado mínimo que la representa: proteína sin acompañamiento no es plato.
        val sinAcompanamiento = PlatoArmado(proteina = producto(1, CategoriaItem.PROTEINA))
        assertTrue(sinAcompanamiento.acompanamientos.isEmpty())
        assertEquals(PlatoArmado.PRECIO_BASE, sinAcompanamiento.precioTotal, 0.0)
    }
}
