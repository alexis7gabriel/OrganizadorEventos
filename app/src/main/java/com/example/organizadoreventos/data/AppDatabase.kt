package com.example.organizadoreventos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.organizadoreventos.data.dao.EventoDao
import com.example.organizadoreventos.data.entities.Evento

@Database(entities = [Evento::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventoDao(): EventoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "organizador_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
