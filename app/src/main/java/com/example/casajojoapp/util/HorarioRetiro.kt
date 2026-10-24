package com.example.casajojoapp.util

import java.time.LocalDateTime
import java.time.LocalTime

// =========================================================
// HORARIO DE RETIRO: única fuente de verdad del rango de slots.
// Todas las pantallas y textos derivan de estas constantes para que
// nunca queden rangos contradictorios en la UI (antes PantallaDos decia
// "12:30 a 15:00" y la UI mostraba hasta 22:00).
//
// IMPORTANTE (pendiente de entrega al cliente): TOTAL_HORAS = 10 deja slots
// de 12:00 a 22:00 SOLO PARA PRUEBAS. Antes de entregar, ajustar al rango
// real del negocio (p. ej. 4 horas = 12:00 a 16:00).
// =========================================================
object HorarioRetiro {

    /** Hora de apertura del retiro (12:00). */
    val HORA_INICIO: LocalTime = LocalTime.of(12, 0)

    // TEMPORAL PARA PRUEBAS: 10 horas (12:00 -> 22:00). Cambiar a 4 para 16:00.
    private const val TOTAL_HORAS_PRUEBA = 10

    /** El backend exige al menos esta anticipación para el retiro. */
    const val ANTICIPACION_MINIMA_MINUTOS = 20L

    /** Rango visible en los textos de la UI, derivado de las constantes. */
    val HORA_FIN: LocalTime = HORA_INICIO.plusHours(TOTAL_HORAS_PRUEBA.toLong())

    /** Texto para UI: "12:00 y 22:00". */
    fun rangoVisible(): String = "${HORA_INICIO} y ${HORA_FIN}"

    /**
     * Valida una hora elegida con el TimePicker NATIVO (fuera de la grilla
     * de 10 minutos). Devuelve el mensaje de error o null si es válida:
     * debe caer dentro de la ventana de servicio y respetar la
     * anticipación mínima exigida por el backend.
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
        if (elegida.isBefore(HORA_INICIO) || elegida.isAfter(HORA_FIN)) {
            return "El retiro funciona entre ${HORA_INICIO} y ${HORA_FIN}."
        }
        val elegidaHoy = LocalDateTime.of(ahora.toLocalDate(), elegida)
        if (elegidaHoy.isBefore(ahora.plusMinutes(ANTICIPACION_MINIMA_MINUTOS))) {
            return "Elige al menos ${ANTICIPACION_MINIMA_MINUTOS} minutos de anticipación."
        }
        return null
    }

    /**
     * Genera los slots de hoy cada 10 minutos, descartando los que el
     * backend rechazaria (< anticipación mínima). Compara por FECHA Y HORA
     * (LocalDateTime): asi, tras las 22:00 no se listan slots pasados del dia
     * (bug anterior: comparaba solo LocalTime y re-ofrecia todo el rango).
     */
    fun generar(ahora: LocalDateTime = LocalDateTime.now()): List<String> {
        val desde = ahora.toLocalTime().plusMinutes(ANTICIPACION_MINIMA_MINUTOS)
        val yaPaso = ahora.toLocalTime().isAfter(HORA_FIN)
        if (yaPaso) return emptyList()

        val totalMinutos = TOTAL_HORAS_PRUEBA * 60
        val fechaHoy = ahora.toLocalDate()
        return buildList {
            for (minuto in 0..totalMinutos step 10) {
                val horaDelSlot = HORA_INICIO.plusMinutes(minuto.toLong())
                val slotDateTime = LocalDateTime.of(fechaHoy, horaDelSlot)
                if (!slotDateTime.isBefore(ahora) && !horaDelSlot.isBefore(desde)) {
                    add(horaDelSlot.toString()) // "HH:mm"
                }
            }
        }
    }
}
