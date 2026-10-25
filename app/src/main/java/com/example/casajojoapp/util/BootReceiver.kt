package com.example.casajojoapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// =========================================================
// Tras un reinicio del teléfono las AlarmManager se pierden; este
// receiver vuelve a programar los recordatorios de retiro que siguen en
// el futuro desde las entradas persistidas en SharedPreferences.
// =========================================================
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(contexto: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            RecordatoriosRetiro(contexto).reprogramarTrasReinicio()
        }
    }
}
