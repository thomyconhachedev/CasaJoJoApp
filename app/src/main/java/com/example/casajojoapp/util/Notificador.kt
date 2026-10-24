package com.example.casajojoapp.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.casajojoapp.R

// =========================================================
// NOTIFICACIONES LOCALES (recurso nativo, rubrica IE 2.4.1):
// avisa en el dispositivo cuando entra una comanda y cuando cambia su
// estado de preparación (p. ej. "Listo para retiro").
// - Canal obligatorio en Android 8+ (API 26; minSdk 28).
// - Permiso POST_NOTIFICATIONS se pide en runtime desde MainActivity
//   (Android 13+/API 33); sin él, notificar() se omite sin romper nada.
// =========================================================
class Notificador(private val contexto: Context) {

    fun notificar(id: Int, titulo: String, texto: String) {
        asegurarCanal()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                contexto, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // permiso aún no concedido: no se emite, no se crashea
        }

        val notificacion = NotificationCompat.Builder(contexto, CANAL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(contexto).notify(id, notificacion)
    }

    private fun asegurarCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CANAL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CANAL_ID,
                        "Pedidos Casa JoJo",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }

    companion object {
        private const val CANAL_ID = "casa_jojo_pedidos"
    }
}
