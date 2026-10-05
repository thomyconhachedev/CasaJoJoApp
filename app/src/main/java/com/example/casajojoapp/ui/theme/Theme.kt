package com.example.casajojoapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// RNF05: soporte obligatorio de modo claro y modo oscuro.
// Primario: negro · Secundario: naranjo · Terciario: verde
//
// Nota sobre "on": el naranjo y el verde de marca no alcanzan 4.5:1 con texto
// blanco (2.91 y 3.06), asi que ambos usan JoJoNegro como color de contenido.
// Es la alternativa a oscurecer los tonos de marca, que se decidió preservar.

private val LightColors = lightColorScheme(
    primary = JoJoNegro,
    onPrimary = JoJoBlanco,
    primaryContainer = JoJoGrisClaro,
    onPrimaryContainer = JoJoNegro,
    inversePrimary = JoJoGrisClaro,

    secondary = JoJoNaranjo,
    onSecondary = JoJoNegro,
    secondaryContainer = JoJoNaranjoClaro,
    onSecondaryContainer = JoJoNegro,

    tertiary = JoJoVerde,
    onTertiary = JoJoNegro,
    tertiaryContainer = JoJoVerdeClaro,
    onTertiaryContainer = JoJoNegro,

    background = JoJoBlanco,
    onBackground = JoJoNegro,
    surface = JoJoGrisClaro,
    onSurface = JoJoNegro,
    surfaceVariant = JoJoContenedorAlto,
    onSurfaceVariant = JoJoGrisOscuro,
    inverseSurface = JoJoGrisOscuro,
    inverseOnSurface = JoJoGrisClaro,

    surfaceContainerLowest = JoJoContenedorMasBajo,
    surfaceContainerLow = JoJoContenedorBajo,
    surfaceContainer = JoJoContenedor,
    surfaceContainerHigh = JoJoContenedorAlto,
    surfaceContainerHighest = JoJoContenedorMasAlto,

    outline = JoJoOutline,
    outlineVariant = JoJoOutlineClaro,
    scrim = JoJoScrim,

    error = JoJoRojo,
    onError = JoJoBlanco,
    errorContainer = JoJoRojoClaro,
    onErrorContainer = JoJoNegro,
)

private val DarkColors = darkColorScheme(
    // El negro puro no sirve como "primary" sobre fondo oscuro; se aclara.
    primary = JoJoGrisMedio,
    onPrimary = JoJoNegro,
    primaryContainer = JoJoGrisOscuro,
    onPrimaryContainer = JoJoGrisClaro,
    inversePrimary = JoJoNegro,

    secondary = JoJoNaranjo,
    onSecondary = JoJoNegro,
    secondaryContainer = JoJoNaranjoOscuro,
    onSecondaryContainer = JoJoNegro,

    tertiary = JoJoVerdeClaro,
    onTertiary = JoJoNegro,
    tertiaryContainer = JoJoVerdeOscuro,
    onTertiaryContainer = JoJoBlanco,

    background = JoJoNegro,
    onBackground = JoJoBlanco,
    surface = JoJoContenedorOscuro,
    onSurface = JoJoBlanco,
    surfaceVariant = JoJoContenedorOscuroAlto,
    onSurfaceVariant = JoJoGrisMedio,
    inverseSurface = JoJoGrisClaro,
    inverseOnSurface = JoJoGrisOscuro,

    surfaceContainerLowest = JoJoContenedorOscuroMasBajo,
    surfaceContainerLow = JoJoContenedorOscuroBajo,
    surfaceContainer = JoJoContenedorOscuro,
    surfaceContainerHigh = JoJoContenedorOscuroAlto,
    surfaceContainerHighest = JoJoContenedorOscuroMasAlto,

    outline = JoJoOutline,
    outlineVariant = JoJoOutlineClaro,
    scrim = JoJoScrim,

    error = JoJoRojoClaro,
    onError = JoJoNegro,
    errorContainer = JoJoRojo,
    onErrorContainer = JoJoBlanco,
)

@Composable
fun CasaJoJoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Color dinamico (Android 12+) apagado a proposito: la identidad de
    // marca (negro/naranjo/verde, RNF04) es obligatoria y no debe variar
    // segun el wallpaper del usuario.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
