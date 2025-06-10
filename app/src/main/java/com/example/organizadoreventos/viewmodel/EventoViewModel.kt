package com.example.organizadoreventos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.organizadoreventos.data.AppDatabase
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.data.repository.EventoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = EventoRepository(db.eventoDao())

    val todosLosEventos: LiveData<List<Evento>> = repository.obtenerTodosLosEventos().asLiveData()

    fun insertarEvento(evento: Evento) {
        viewModelScope.launch {
            repository.insertarEvento(evento)
        }
    }

    fun actualizarEvento(evento: Evento) = viewModelScope.launch(Dispatchers.IO) {
        repository.actualizar(evento)
    }

    fun eliminarEvento(evento: Evento) = viewModelScope.launch(Dispatchers.IO) {
        repository.eliminar(evento)
    }

    fun eliminarTodosLosEventos() = viewModelScope.launch(Dispatchers.IO) {
        repository.eliminarTodos()
    }

    fun insertarTodosLosEventos(eventos: List<Evento>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertarTodos(eventos)
    }
}
