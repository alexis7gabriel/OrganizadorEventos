package com.example.organizadoreventos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.organizadoreventos.data.entities.Evento
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    @Insert
    suspend fun insertarEvento(evento: Evento)

    @Query("SELECT * FROM eventos ORDER BY fecha DESC")
    fun obtenerTodosLosEventos(): Flow<List<Evento>>

    @Update
    suspend fun actualizarEvento(evento: Evento)

    @Delete
    suspend fun eliminarEvento(evento: Evento)
}
