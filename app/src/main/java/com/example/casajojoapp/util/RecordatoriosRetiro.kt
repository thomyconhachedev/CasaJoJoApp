package com.example.casajojoapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.edit

// =========================================================
// RECORDATORIO DE RETIRO: programa una notificación local 5 MINUTOS
// ANTES de la hora de retiro con AlarmManager (funciona con la app
// cerrada; ver AvisoRetiroReceiver).
// - Se programa en el dispositivo del CLIENTE al enviar el pedido (su
//   teléfono conoce la horaRetiro, no hace falta backend).
// - Cada entrada se persiste en SharedPreferences para poder
//   REPROGRAMAR las alarmas tras un reinicio del teléfono (BootReceiver).
// - Se cancela si el pedido se cancela o queda listo para retiro.
// Precisión: setExactAndAllowWhileIdle; si el sistema niega el permiso
// de alarma exacta (Android 13+), cae a una alarma inexacta sin romper.
// =========================================================
class RecordatoriosRetiro(private val contexto: Context) {

    private val prefs =
        contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun programar(
        idPedido: Long,
        momento: Long,
        titulo: String,
        texto: String
    ) {
        if (momento <= System.currentTimeMillis()) return

        // Se persiste ANTES de la alarma: si el teléfono se reinicia,
        // BootReceiver la vuelve a programar desde estas entradas.
        prefs.edit {
            putString(
                clave(idPedido),
                "$momento|$titulo|$texto"
            )
        }

        val alarma = contexto.getSystemService(AlarmManager::class.java)
        try {
            alarma.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, momento, pendienteDe(idPedido, titulo, texto)
            )
        } catch (_: SecurityException) {
            // Sin permiso de alarma exacta (Android 13+ lo otorga el
            // usuario): alarma inexacta, suficiente para el aviso.
            alarma.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, momento, pendienteDe(idPedido, titulo, texto)
            )
        }
    }

    fun cancelar(idPedido: Long) {
        val alarma = contexto.getSystemService(AlarmManager::class.java)
        alarma.cancel(pendienteCancelacion(idPedido))
        prefs.edit { remove(clave(idPedido)) }
    }

    /** Solo olvida la entrada (ya disparada). */
    fun quitar(idPedido: Long) {
        prefs.edit { remove(clave(idPedido)) }
    }

    /** Tras un reinicio: reprograma las alarmas que siguen en el futuro. */
    fun reprogramarTrasReinicio() {
        val ahora = System.currentTimeMillis()
        prefs.all.forEach { (claveEntrada, valor) ->
            val datos = (valor as? String)?.split("|", limit = 3)
            val id = claveEntrada.removePrefix("pedido_").toLongOrNull()
            val momento = datos?.getOrNull(0)?.toLongOrNull()
            if (id == null || datos == null || momento == null) {
                prefs.edit { remove(claveEntrada) }
            } else if (momento > ahora) {
                programar(id, momento, datos[1], datos[2])
            } else {
                prefs.edit { remove(claveEntrada) }
            }
        }
    }

    // El receiver recibe id/título/texto como extras del PendingIntent.
    private fun pendienteDe(
        idPedido: Long,
        titulo: String,
        texto: String
    ): PendingIntent {
        val intent = Intent(contexto, AvisoRetiroReceiver::class.java).apply {
            putExtra(AvisoRetiroReceiver.EXTRA_ID, idPedido)
            putExtra(AvisoRetiroReceiver.EXTRA_TITULO, titulo)
            putExtra(AvisoRetiroReceiver.EXTRA_TEXTO, texto)
        }
        return PendingIntent.getBroadcast(
            contexto,
            codigoSolicitud(idPedido),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Para cancelar basta con coincidir componente + código de solicitud
    // (los extras no participan en la igualdad de los Intents).
    private fun pendienteCancelacion(idPedido: Long): PendingIntent =
        PendingIntent.getBroadcast(
            contexto,
            codigoSolicitud(idPedido),
            Intent(contexto, AvisoRetiroReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun clave(idPedido: Long) = "pedido_$idPedido"

    private fun codigoSolicitud(idPedido: Long): Int = idPedido.toInt()

    companion object {
        private const val PREFS = "recordatorios_retiro"
    }
}
