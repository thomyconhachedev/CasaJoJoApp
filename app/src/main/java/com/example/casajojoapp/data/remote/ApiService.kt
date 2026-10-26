package com.example.casajojoapp.data.remote

import com.example.casajojoapp.data.remote.dto.AuthResponseDto
import com.example.casajojoapp.data.remote.dto.LoginRequestDto
import com.example.casajojoapp.data.remote.dto.ProductoDto
import com.example.casajojoapp.data.remote.dto.RegistroRequestDto
import com.example.casajojoapp.data.remote.dto.request.CambiarEstadoRequest
import com.example.casajojoapp.data.remote.dto.request.CrearPedidoRequestDto
import com.example.casajojoapp.data.remote.dto.response.PedidoResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
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

    // Pedidos de la cuenta autenticada (usuario normal; el token lo adjunta
    // AuthInterceptor). La cola completa de arriba es solo para admin.
    @GET("api/v1/pedidos/mios")
    suspend fun listarMisPedidos(): List<PedidoResponseDto>

    // Buscador por código de seguimiento (endpoint público: también lo
    // usan invitados desde "Mis Pedidos").
    @GET("api/v1/pedidos/seguimiento/{codigo}")
    suspend fun buscarPorCodigo(@Path("codigo") codigo: String): PedidoResponseDto

    // Búsqueda por id numérico (solo rol ADMIN en modo API).
    @GET("api/v1/pedidos/{id}")
    suspend fun buscarPorId(@Path("id") id: Long): PedidoResponseDto

    @PATCH("api/v1/pedidos/{id}/estado")
    suspend fun cambiarEstadoPedido(
        @Path("id") id: Long,
        @Body body: CambiarEstadoRequest
    ): PedidoResponseDto

    // --- AUTENTICACIÓN (backend con roles/JWT) ---
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("api/v1/auth/registro")
    suspend fun registro(@Body body: RegistroRequestDto): AuthResponseDto

    @GET("api/v1/auth/me")
    suspend fun perfil(@Header("Authorization") bearer: String): AuthResponseDto
}
