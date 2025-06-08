package com.example.organizadoreventos.data.repository

import com.example.organizadoreventos.data.dao.EventoDao
import com.example.organizadoreventos.data.entities.Evento
import kotlinx.coroutines.flow.Flow

class EventoRepository(private val eventoDao: EventoDao) {

    suspend fun insertarEvento(evento: Evento) {
        eventoDao.insertarEvento(evento)
    }

    fun obtenerTodosLosEventos(): Flow<List<Evento>> {
        return eventoDao.obtenerTodosLosEventos()
    }

    suspend fun actualizar(evento: Evento) {
        eventoDao.actualizarEvento(evento)
    }

    suspend fun eliminar(evento: Evento) {
        eventoDao.eliminarEvento(evento)
    }
}
