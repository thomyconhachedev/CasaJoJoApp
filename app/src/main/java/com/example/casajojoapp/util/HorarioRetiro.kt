package com.example.casajojoapp.util

import java.time.LocalDateTime
import java.time.LocalTime

// =========================================================
// HORARIO DE RETIRO: única fuente de verdad del rango de slots.
// Todas las pantallas y textos derivan de estas constantes para que
// nunca queden rangos contradictorios en la UI.
//
// La ventana de servicio CRUZA LA MEDIANOCHE: empieza a las 12:00 y
// termina a las 02:00 del día siguiente (pruebas hasta las 2 am).
//
// ⚠️ DÍA DE LA DEMO/ENTREGA: cambiar HORAS_VENTANA de 14 a 4 para que
// el rango sea exactamente 12:00–16:00 (4 horas). Es el único cambio.
// =========================================================
object HorarioRetiro {

    /** Hora de apertura del retiro (12:00). */
    val HORA_INICIO: LocalTime = LocalTime.of(12, 0)

    /**
     * Duración de la ventana de servicio en horas:
     * - 14 => 12:00 → 02:00 del día siguiente (PRUEBAS, hasta las 2 am).
     * -  4 => 12:00 → 16:00 (DÍA REAL / DEMO).
     */
    const val HORAS_VENTANA = 14

    /** El backend exige al menos esta anticipación para el retiro. */
    const val ANTICIPACION_MINIMA_MINUTOS = 20L

    /** Hora de cierre (02:00 del día siguiente con HORAS_VENTANA = 14). */
    val HORA_FIN: LocalTime = HORA_INICIO.plusHours(HORAS_VENTANA.toLong())

    /** Texto para UI: "12:00 y 02:00". */
    fun rangoVisible(): String = "${HORA_INICIO} y ${HORA_FIN}"

    /**
     * Ventana de servicio vigente. Si la hora actual cae en la cola de la
     * ventana de AYER (00:00–02:00), se usa esa; si no, la de hoy
     * (12:00 → 02:00 del día siguiente).
     */
    private fun ventanaActual(ahora: LocalDateTime): ClosedRange<LocalDateTime> {
        val inicioHoy = LocalDateTime.of(ahora.toLocalDate(), HORA_INICIO)
        val finHoy = inicioHoy.plusHours(HORAS_VENTANA.toLong())
        val inicioAyer = inicioHoy.minusDays(1)
        val finAyer = inicioAyer.plusHours(HORAS_VENTANA.toLong()) // hoy a las 02:00
        return if (ahora.isBefore(finAyer) && ahora.isAfter(inicioAyer)) {
            inicioAyer..finAyer
        } else {
            inicioHoy..finHoy
        }
    }

    /**
     * Genera los slots de 10 minutos de la ventana vigente, descartando
     * los que el backend rechazaría (< anticipación mínima). Compara por
     * FECHA Y HORA (LocalDateTime), así que funciona cruzando la
     * medianoche: a la 01:30 ofrece hasta las 02:00, nunca horas pasadas.
     */
    fun generar(ahora: LocalDateTime = LocalDateTime.now()): List<String> {
        val ventana = ventanaActual(ahora)
        val desde = ahora.plusMinutes(ANTICIPACION_MINIMA_MINUTOS)
        if (!desde.isBefore(ventana.endInclusive)) return emptyList()

        return buildList {
            var slot = ventana.start
            while (!slot.isAfter(ventana.endInclusive)) {
                if (slot >= desde) {
                    add(slot.toLocalTime().toString()) // "HH:mm"
                }
                slot = slot.plusMinutes(10)
            }
        }
    }

    /**
     * Valida una hora elegida con el TimePicker NATIVO (fuera de la grilla
     * de 10 minutos). Devuelve el mensaje de error o null si es válida:
     * la hora debe caer dentro de la ventana de servicio (resolviendo si
     * pertenece a hoy o a mañana, por el cruce de medianoche) y respetar
     * la anticipación mínima exigida por el backend.
     */
    fun validarHoraPersonalizada(
        hora: String,
        ahora: LocalDateTime = LocalDateTime.now()
    ): String? {
        val elegida = try {
            LocalTime.parse(hora)
        } catch (_: java.time.format.DateTimeParseException) {
            return "Hora inválida."
        }

        val ventana = ventanaActual(ahora)
        val desde = ahora.plusMinutes(ANTICIPACION_MINIMA_MINUTOS)

        // Candidatos: hoy a esa hora, o mañana (para 00:00–02:00 elegidos
        // durante la tarde, que pertenecen al día siguiente).
        val candidatos = listOf(
            LocalDateTime.of(ahora.toLocalDate(), elegida),
            LocalDateTime.of(ahora.toLocalDate(), elegida).plusDays(1)
        )

        val dentroDeVentana = candidatos.filter { it in ventana }
        if (dentroDeVentana.isEmpty()) {
            return "El retiro funciona entre ${HORA_INICIO} y ${HORA_FIN}."
        }
        if (dentroDeVentana.none { it >= desde }) {
            return "Elige al menos ${ANTICIPACION_MINIMA_MINUTOS} minutos de anticipación."
        }
        return null
    }
}
