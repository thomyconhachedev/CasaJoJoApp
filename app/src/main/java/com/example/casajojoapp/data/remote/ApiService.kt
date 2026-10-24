package com.example.casajojoapp.data.remote

import com.example.casajojoapp.data.remote.dto.ProductoDto
import com.example.casajojoapp.data.remote.dto.request.CambiarEstadoRequest
import com.example.casajojoapp.data.remote.dto.request.CrearPedidoRequestDto
import com.example.casajojoapp.data.remote.dto.response.PedidoResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// =========================================================
// Cliente HTTP (Retrofit) contra el backend de CasaJoJo.
// Rutas relativas a la BASE_URL (debe terminar en "/").
// Contrato: MenuController.java y PedidoController.java del backend.
// =========================================================
interface ApiService {

    // --- MENÚ ---
    @GET("api/v1/menu")
    suspend fun listarMenu(): MenuDiarioDto

    @POST("api/v1/menu/sincronizar")
    suspend fun sincronizarMenu(): MenuDiarioDto

    // --- PEDIDOS ---
    @POST("api/v1/pedidos")
    suspend fun crearPedido(@Body body: CrearPedidoRequestDto): PedidoResponseDto

    @GET("api/v1/pedidos")
    suspend fun listarPedidos(): List<PedidoResponseDto>

    @PATCH("api/v1/pedidos/{id}/estado")
    suspend fun cambiarEstadoPedido(
        @Path("id") id: Long,
        @Body body: CambiarEstadoRequest
    ): PedidoResponseDto
}
