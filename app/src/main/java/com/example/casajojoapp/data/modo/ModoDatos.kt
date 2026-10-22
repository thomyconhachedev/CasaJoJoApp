package com.example.casajojoapp.data.modo

// =========================================================
// MODO DE DATOS de la app.
// - LOCAL (default): menú hardcodeado + cola de pedidos en memoria.
// - API: consume el backend (menú + pedidos) vía Retrofit.
// Se alterna en runtime desde el panel Admin y se persiste en SharedPreferences.
// =========================================================
enum class ModoDatos {
    LOCAL,
    API
}
