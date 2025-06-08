package com.example.organizadoreventos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.organizadoreventos.data.entities.Evento
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    @Insert
    suspend fun insertarEvento(evento: Evento)

    @Query("SELECT * FROM eventos ORDER BY fecha DESC")
    fun obtenerTodosLosEventos(): Flow<List<Evento>>
}
