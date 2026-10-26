package com.example.casajojoapp.data.repository

import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.EstadoPedido
import com.example.casajojoapp.domain.model.Pedido
import com.example.casajojoapp.domain.model.PlatoArmado
import com.example.casajojoapp.domain.model.Producto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cola local (modo LOCAL): ids únicos, código de seguimiento, cambios de
 * estado que tocan SOLO al pedido correcto y restauración desde Room.
 */
class PedidoRepositoryLocalTest {

    private fun pedido(nombre: String = "Cliente") = Pedido(
        id = 0L,
        clienteNombre = nombre,
        clienteTelefono = "+56911111111",
        horaRetiro = "13:00",
        platos = listOf(
            PlatoArmado(
                proteina = Producto(1L, "Carne", CategoriaItem.PROTEINA, 0.0),
                acompanamientos = listOf(
                    Producto(2L, "Arroz", CategoriaItem.ACOMPANAMIENTO, 0.0),
                    Producto(3L, "Papas", CategoriaItem.ACOMPANAMIENTO, 0.0)
                )
            )
        ),
        total = 4800.0,
        notasRestricciones = null
    )

    @Test
    fun cadaPedidoNuevoRecibeIdUnicoYCodigoJojo() = runBlocking {
        val repo = PedidoRepositoryLocal()

        val primero = repo.crearPedido(pedido("Uno"))
        val segundo = repo.crearPedido(pedido("Dos"))
        val tercero = repo.crearPedido(pedido("Tres"))

        assertEquals(1L, primero.id)
        assertEquals(2L, segundo.id)
        assertEquals(3L, tercero.id)
        assertEquals("JOJO-0001", primero.codigoSeguimiento)
        assertEquals("JOJO-0003", tercero.codigoSeguimiento)
        assertEquals(3, repo.listarPedidos().size)
    }

    @Test
    fun cambiarEstadoSoloAfectaAlPedidoIndicado() = runBlocking {
        val repo = PedidoRepositoryLocal()
        val primero = repo.crearPedido(pedido("Uno"))
        val segundo = repo.crearPedido(pedido("Dos"))

        repo.cambiarEstado(segundo.id, EstadoPedido.LISTO_PARA_RETIRO)

        val lista = repo.listarPedidos()
        assertEquals(EstadoPedido.RECIBIDO, lista.first { it.id == primero.id }.estado)
        assertEquals(EstadoPedido.LISTO_PARA_RETIRO, lista.first { it.id == segundo.id }.estado)
    }

    @Test
    fun sembrarRestauraLaColaYAvanzaLaSecuencia() = runBlocking {
        val repo = PedidoRepositoryLocal()
        val restaurados = listOf(
            pedido("Viejo A").copy(id = 3L, codigoSeguimiento = "JOJO-0003"),
            pedido("Viejo B").copy(id = 5L, codigoSeguimiento = "JOJO-0005")
        )

        repo.sembrar(restaurados)

        val lista = repo.listarPedidos()
        assertEquals(listOf(3L, 5L), lista.map { it.id })
        // El siguiente pedido nuevo no pisa los restaurados.
        val nuevo = repo.crearPedido(pedido("Nuevo"))
        assertEquals(6L, nuevo.id)
    }

    @Test
    fun sembrarNoDuplicaSiLaColaYaTieneDatos() {
        val repo = PedidoRepositoryLocal()
        runBlocking { repo.crearPedido(pedido("Existente")) }

        repo.sembrar(listOf(pedido("Intruso").copy(id = 9L)))

        assertEquals(1, runBlocking { repo.listarPedidos().size })
        assertTrue(runBlocking { repo.listarPedidos() }.none { it.id == 9L })
    }

    @Test
    fun buscarPorCodigoEncuentraIgnorandoMayusculas() = runBlocking {
        val repo = PedidoRepositoryLocal()
        val creado = repo.crearPedido(pedido("Ana"))

        assertEquals(creado.id, repo.buscarPorCodigo(creado.codigoSeguimiento!!)?.id)
        // Case-insensitive: el usuario puede escribirlo en minúsculas.
        assertEquals(creado.id, repo.buscarPorCodigo(creado.codigoSeguimiento!!.lowercase())?.id)
        // Inexistente => null (la pantalla muestra "no encontrado").
        assertNull(repo.buscarPorCodigo("CJ-NOEXISTE"))
    }

    @Test
    fun buscarPorIdEncuentraSoloPedidosExistentes() = runBlocking {
        val repo = PedidoRepositoryLocal()
        val primero = repo.crearPedido(pedido("Uno"))
        repo.crearPedido(pedido("Dos"))

        assertEquals(primero.id, repo.buscarPorId(primero.id)?.id)
        assertNull(repo.buscarPorId(999L))
    }
}
