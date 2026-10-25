package com.example.casajojoapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * Generación y validación de la hora de retiro con reloj fijo.
 * Todas las expectativas se derivan de HORA_INICIO/HORA_FIN para que los
 * tests sigan en verde cuando HORAS_VENTANA pase de 14 a 4 el día de la demo.
 */
class HorarioRetiroTest {

    // Lunes 26-oct-2026 a las 15:00 (dentro de la ventana de servicio).
    private val tarde = LocalDateTime.of(2026, 10, 26, 15, 0)

    @Test
    fun generaSlotsDesdeLaAnticipacionHastaElCierre() {
        val slots = HorarioRetiro.generar(tarde)

        assertTrue(slots.isNotEmpty())
        // Primer slot: 15:00 + 20 min de anticipación, alineado a la grilla.
        assertEquals("15:20", slots.first())
        // Último slot: la hora de cierre de la ventana.
        assertEquals(HorarioRetiro.HORA_FIN.toString(), slots.last())
        // Todos alineados a la grilla de 10 minutos.
        assertTrue(slots.all { it.endsWith(":00") || it.endsWith(":10") || it.endsWith(":20") ||
            it.endsWith(":30") || it.endsWith(":40") || it.endsWith(":50") })
    }

    @Test
    fun antesDeLaAperturaOfreceDesdeElMediodia() {
        // 03:00 de la madrugada: la ventana del día aún no empieza.
        val madrugada = LocalDateTime.of(2026, 10, 26, 3, 0)
        val slots = HorarioRetiro.generar(madrugada)

        assertTrue(slots.isNotEmpty())
        assertEquals(HorarioRetiro.HORA_INICIO.toString(), slots.first())
    }

    @Test
    fun enLaColaDeLaMedianocheSigueOfreciendoHastaElCierre() {
        // 01:00: pertenece a la ventana de ANTERIOR al mediodía (cruce).
        val unoAM = LocalDateTime.of(2026, 10, 26, 1, 0)
        val slots = HorarioRetiro.generar(unoAM)

        assertTrue(slots.isNotEmpty())
        assertEquals("01:20", slots.first())
        assertEquals(HorarioRetiro.HORA_FIN.toString(), slots.last())
    }

    @Test
    fun sinTiempoSuficienteLaListaQuedaVacia() {
        // 5 minutos antes del cierre: ninguna hora cumple la anticipación.
        val fecha = tarde.toLocalDate()
        val cierre = if (HorarioRetiro.HORA_FIN.isAfter(HorarioRetiro.HORA_INICIO)) {
            LocalDateTime.of(fecha, HorarioRetiro.HORA_FIN)
        } else {
            LocalDateTime.of(fecha, HorarioRetiro.HORA_FIN).plusDays(1)
        }
        val casiCierre = cierre.minusMinutes(5)

        assertTrue(HorarioRetiro.generar(casiCierre).isEmpty())
    }

    @Test
    fun validacionDeHoraPersonalizadaAceptaDentroDeLaVentana() {
        // Una hora DESPUÉS de la hora actual (15:00 + 60 min = 16:00),
        // dentro de la ventana y con más de 20 minutos de anticipación.
        val valida = tarde.toLocalTime().plusMinutes(60)
        assertNull(HorarioRetiro.validarHoraPersonalizada(valida.toString(), tarde))
    }

    @Test
    fun validacionDeHoraPersonalizadaRechazaFueraDeLaVentana() {
        // 09:00 nunca pertenece a la ventana (12:00 -> 02:00/16:00).
        val error = HorarioRetiro.validarHoraPersonalizada("09:00", tarde)
        assertNotNull(error)
    }

    @Test
    fun validacionDeHoraPersonalizadaExigeAnticipacion() {
        // Exactamente la hora de apertura SIN los 20 minutos de anticipación.
        val error = HorarioRetiro.validarHoraPersonalizada(
            HorarioRetiro.HORA_INICIO.toString(),
            LocalDateTime.of(2026, 10, 26, 12, 0)
        )
        assertNotNull(error)
    }

    @Test
    fun recordatorioEsCincoMinutosAntesDelRetiro() {
        val ahora = LocalDateTime.of(2026, 10, 26, 13, 0)
        val esperado = LocalDateTime.of(2026, 10, 26, 13, 55)

        val obtenido = HorarioRetiro.momentoRecordatorio("14:00", ahora)
        val esperadoMillis = esperado.atZone(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        assertEquals(esperadoMillis, obtenido)
    }

    @Test
    fun recordatorioResuelveAlDiaSiguienteSiLaHoraYaPaso() {
        val ahora = LocalDateTime.of(2026, 10, 26, 14, 3)
        val esperado = LocalDateTime.of(2026, 10, 27, 13, 55)

        val obtenido = HorarioRetiro.momentoRecordatorio("14:00", ahora)
        val esperadoMillis = esperado.atZone(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        assertEquals(esperadoMillis, obtenido)
    }

    @Test
    fun sinRecordatorioSiFaltanMenosDeCincoMinutosUHoraInvalida() {
        val ahora = LocalDateTime.of(2026, 10, 26, 13, 58)
        assertNull(HorarioRetiro.momentoRecordatorio("14:00", ahora))
        assertNull(HorarioRetiro.momentoRecordatorio("no-es-hora", ahora))
    }
}
