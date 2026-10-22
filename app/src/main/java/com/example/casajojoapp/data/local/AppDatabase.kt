package com.example.casajojoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.casajojoapp.data.local.dao.ClienteDao
import com.example.casajojoapp.data.local.entity.ClienteEntity

// CAMBIO CUENTAS: base de datos Room local (aún sin backend). Por ahora
// solo guarda el perfil del cliente; crece con futuras entidades.
@Database(entities = [ClienteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtener(contexto: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: construir(contexto.applicationContext).also { INSTANCIA = it }
            }

        private fun construir(contexto: Context): AppDatabase =
            Room.databaseBuilder(
                contexto,
                AppDatabase::class.java,
                "casa_jojo.db"
            ).build()
    }
}
