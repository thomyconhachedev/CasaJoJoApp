package com.example.casajojoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.casajojoapp.data.local.dao.ClienteDao
import com.example.casajojoapp.data.local.dao.UsuarioDao
import com.example.casajojoapp.data.local.entity.ClienteEntity
import com.example.casajojoapp.data.local.entity.UsuarioEntity

// Base de datos local:
// - v1: perfil del cliente (nombre/teléfono).
// - v2: sistema de usuarios con rol (CAMBIO ROLES); la migración conserva
//   el perfil existente (no se usa fallback destructivo).
@Database(
    entities = [ClienteEntity::class, UsuarioEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        // v1 -> v2: crea la tabla usuarios con índice único de username.
        // Debe coincidir exactamente con el esquema de UsuarioEntity.
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `usuarios` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`username` TEXT NOT NULL, " +
                        "`passwordHash` TEXT NOT NULL, " +
                        "`salt` TEXT NOT NULL, " +
                        "`rol` TEXT NOT NULL, " +
                        "`nombre` TEXT NOT NULL, " +
                        "`telefono` TEXT NOT NULL, " +
                        "`fechaCreacion` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_usuarios_username` " +
                        "ON `usuarios` (`username`)"
                )
            }
        }

        fun obtener(contexto: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: construir(contexto.applicationContext).also { INSTANCIA = it }
            }

        private fun construir(contexto: Context): AppDatabase =
            Room.databaseBuilder(
                contexto,
                AppDatabase::class.java,
                "casa_jojo.db"
            ).addMigrations(MIGRACION_1_2).build()
    }
}
