package com.example.organizadoreventos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.organizadoreventos.data.AppDatabase
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.data.repository.EventoRepository
import kotlinx.coroutines.launch

class EventoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = EventoRepository(db.eventoDao())

    val todosLosEventos: LiveData<List<Evento>> = repo.obtenerTodosLosEventos().asLiveData()

    fun insertarEvento(evento: Evento) {
        viewModelScope.launch {
            repo.insertarEvento(evento)
        }
    }
}
