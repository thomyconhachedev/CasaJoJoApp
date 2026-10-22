package com.example.casajojoapp.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================
// Paleta de marca CasaJoJoApp
// Primario: negro · Secundario: naranjo · Terciario: verde
//
// Este archivo es la unica fuente de verdad de los hex. Theme.kt solo cablea
// estos tokens a los roles del ColorScheme; los dos ficheros comparten valores,
// no literales sueltos.
//
// Todos los pares "X / onX" están verificados contra WCAG AA (4.5:1 para texto,
// 3:1 para elementos de interfaz) en los dos modos.
// =========================================================

// --- Primario (negro) ---
val JoJoNegro = Color(0xFF1A1A1A)

// --- Secundario (naranjo) ---
val JoJoNaranjo = Color(0xFFF2722C)
val JoJoNaranjoClaro = Color(0xFFFFA366)
// #C75A1E daba 4.28:1 con blanco y 4.07:1 con negro, insuficiente en ambos
// casos. Este tono es el naranja oscuro mas claro que si alcanza 4.5:1 (con negro).
val JoJoNaranjoOscuro = Color(0xFFD4651F)

// --- Terciario (verde) ---
val JoJoVerde = Color(0xFF3FA66A)
val JoJoVerdeClaro = Color(0xFF7FD19B)
val JoJoVerdeOscuro = Color(0xFF2C7A4B)

// --- Semanticos de error ---
val JoJoRojo = Color(0xFFC62828)
val JoJoRojoClaro = Color(0xFFE88B8A)
val JoJoAmbar = Color(0xFFE2A33D)

// --- Neutros de soporte (fondo, superficie, texto) ---
// Todos comparten la temperatura calida de JoJoGrisMedio para que la escala de
// grises sea homogenea: antes gris medio era calido y los demas neutros puros.
val JoJoBlanco = Color(0xFFFFFFFF)
val JoJoGrisClaro = Color(0xFFF4F2F0)
val JoJoGrisMedio = Color(0xFF9C9A92)
val JoJoGrisOscuro = Color(0xFF2B2A28)

// --- Bordes y divisorios ---
val JoJoOutline = Color(0xFF767469)
val JoJoOutlineClaro = Color(0xFFCFCBC3)
val JoJoScrim = Color(0xFF000000)

// --- Superficies y acentos de las pantallas ---
// Antes estaban hardcodeados como Color(0xFF...) repartidos por las ~12
// pantallas; se centralizan aqui para que cambiar un token actualice toda la app.
val JoJoFondoApp = Color(0xFF121212)          // fondo de pantalla de la app
val JoJoSuperficie = Color(0xFF1E1E1E)        // tarjetas / superficies elevadas
val JoJoSeleccion = Color(0xFF2A1D17)         // tarjeta seleccionada
val JoJoBorde = Color(0xFF333333)             // bordes y divisores
val JoJoTextoSecundario = Color(0xFFC9BDB3)   // texto secundario calido
val JoJoTextoClaro = Color(0xFFFFF4EC)        // texto claro (barra de progreso)
val JoJoTextoRestriccion = Color(0xFFE8D9CF)  // texto de restricciones
val JoJoAcento = Color(0xFFFFAA75)            // acento naranja claro (encabezados/chips)
val JoJoNaranjoVivo = Color(0xFFFF6B2B)       // naranja vivo (borde tarjeta seleccionada)
val JoJoNaranjoTexto = Color(0xFFFF9E66)      // texto naranja claro (encabezado barra de progreso)
val JoJoCafeOscuro = Color(0xFF513020)        // badge contador / tarjeta promocion
val JoJoBotonOscuro = Color(0xFF252321)       // boton secundario oscuro (contenedor)
val JoJoDeshabilitado = Color(0xFF382C24)     // contenedor deshabilitado y segmento inactivo
val JoJoRojoFondo = Color(0xFF5A1A1A)         // fondo del chip "AGOTADO"
val JoJoRojoTexto = Color(0xFFFF6B6B)         // texto del chip "AGOTADO"

// --- Superficies Material 3 ---
// Sin definir estos roles, Material los calcula a partir de "primary" y les
// aplica su tinte: en claro salian violeta (morado de la paleta baseline) y en
// oscuro un oliva que no cuadraba con los neutros de marca.
val JoJoContenedorMasBajo = Color(0xFFFFFFFF)
val JoJoContenedorBajo = Color(0xFFFAF9F6)
val JoJoContenedor = Color(0xFFF4F2F0)
val JoJoContenedorAlto = Color(0xFFEFEDE9)
val JoJoContenedorMasAlto = Color(0xFFE9E7E2)

val JoJoContenedorOscuroMasBajo = Color(0xFF131312)
val JoJoContenedorOscuroBajo = Color(0xFF1A1A1A)
val JoJoContenedorOscuro = Color(0xFF232322)
val JoJoContenedorOscuroAlto = Color(0xFF2E2D2B)
val JoJoContenedorOscuroMasAlto = Color(0xFF383633)

// --- Colores de estado (maquina de 5 estados del pedido, RF13) ---
// El fondo del chip es fijo en ambos modos: son colores de marca y de estado,
// no roles del tema, para que un pedido se lea igual en la cola sin importar el
// tema activo. Por eso cada estado lleva su "On..." emparejado, en vez de
// depender de onSurface, que si cambia entre modos.
val EstadoRecibido = JoJoGrisMedio
val OnEstadoRecibido = JoJoNegro          // 6.18:1

val EstadoEnPreparacion = JoJoAmbar
val OnEstadoEnPreparacion = JoJoNegro     // 7.91:1

val EstadoListo = JoJoNaranjo              // naranjo: atencion, accion requerida
val OnEstadoListo = JoJoNegro              // 5.98:1

val EstadoEntregado = JoJoVerde             // verde: completado con exito
val OnEstadoEntregado = JoJoNegro          // 5.70:1

val EstadoCancelado = JoJoRojo             // rojo: terminado negativamente
val OnEstadoCancelado = JoJoBlanco         // 5.62:1
