package com.example.casajojoapp.data.repository

import com.example.casajojoapp.data.local.MenuLocalDataSource
import com.example.casajojoapp.data.mappers.aDominio
import com.example.casajojoapp.data.remote.ApiService
import com.example.casajojoapp.domain.model.CategoriaItem
import com.example.casajojoapp.domain.model.Producto

// =========================================================
// Repositorio de MENÚ con doble implementación:
// - Local: fuente hardcodeada (MenuLocalDataSource), sin backend.
// - Api: consume GET /api/v1/menu (menú de hoy) y mapea a dominio.
// El selector (OrderViewModel) elige según el ModoDatos vigente.
// =========================================================
interface MenuRepository {
    suspend fun listarProductos(): List<Producto>

    // "Refrescar menú" del panel admin. Por defecto = listar; la
    // implementación remota además sincroniza (POST /sincronizar baja el
    // Google Sheet a la BD del backend) y luego vuelve a leer el menú, para
    // que el cliente actualice su menú desde la app sin usar Postman.
    suspend fun refrescar(): List<Producto> = listarProductos()
}

// Menú hardcodeado (modo LOCAL). No depende de red.
class MenuRepositoryLocal : MenuRepository {
    override suspend fun listarProductos(): List<Producto> =
        MenuLocalDataSource.proteinas +
            MenuLocalDataSource.acompanamientos +
            MenuLocalDataSource.ensaladas +
            MenuLocalDataSource.extras
}

// Contra el backend (modo API): GET /api/v1/menu devuelve el menú de hoy
// envuelto en {fecha, estado, items[]}; al repo solo le interesan los items.
class MenuRepositoryApi(private val api: ApiService) : MenuRepository {
    override suspend fun listarProductos(): List<Producto> =
        api.listarMenu().items.map { it.aDominio() }

    override suspend fun refrescar(): List<Producto> {
        api.sincronizarMenu() // POST: lee el Google Sheet y hace upsert en la BD
        return listarProductos() // GET: devuelve el menú recién sincronizado
    }
}

// Helpers de filtrado por categoría usados por el ViewModel.
fun List<Producto>.deCategoria(categoria: CategoriaItem): List<Producto> =
    filter { it.categoria == categoria }

// Extras = postres + bebidas. En modo API el backend agrupa ambos en
// BEBIDA_POSTRE, que el mapper traduce a BEBIDA; por eso se aceptan los dos.
fun List<Producto>.extras(): List<Producto> =
    filter { it.categoria == CategoriaItem.POSTRE || it.categoria == CategoriaItem.BEBIDA }
