package com.example.casajojoapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// =========================================================
// Dispara el recordatorio "faltan 5 minutos para tu retiro". Lo invoca
// AlarmManager con la app potencialmente cerrada. Al llegar, emite la
// notificación y olvida la entrada persistida.
// =========================================================
class AvisoRetiroReceiver : BroadcastReceiver() {

    override fun onReceive(contexto: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        if (id == -1L) return

        val titulo = intent.getStringExtra(EXTRA_TITULO)
            ?: "Tu retiro es en 5 minutos"
        val texto = intent.getStringExtra(EXTRA_TEXTO)
            ?: "Pasa por tu pedido en La Casa JoJo."

        // Primero olvida la entrada, luego notifica (si la notificación
        // se deniega, no queda un recordatorio zombi reprogramado).
        RecordatoriosRetiro(contexto).quitar(id)
        Notificador(contexto).notificar(
            // ID distinto al de los avisos de estado, para no pisarlos.
            id = idNotificacion(id),
            titulo = titulo,
            texto = texto
        )
    }

    companion object {
        const val EXTRA_ID = "id_pedido"
        const val EXTRA_TITULO = "titulo_aviso"
        const val EXTRA_TEXTO = "texto_aviso"

        // Tramo de ids reservado para recordatorios de retiro.
        fun idNotificacion(idPedido: Long): Int = idPedido.toInt() + 1_000_000
    }
}
