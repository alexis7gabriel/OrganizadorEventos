package com.example.organizadoreventos.ui.fragmentos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.organizadoreventos.databinding.FragmentInicioBinding
import com.example.organizadoreventos.ui.adapters.EventosAdapter
import com.example.organizadoreventos.viewmodel.EventoViewModel
import kotlinx.coroutines.flow.collectLatest // No se usa en este fragmento, se puede quitar si no se necesita
import kotlinx.coroutines.launch // No se usa en este fragmento, se puede quitar si no se necesita
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InicioFragment : Fragment() {

    private val eventoViewModel: EventoViewModel by viewModels()
    private lateinit var binding: FragmentInicioBinding
    private lateinit var eventosAdapter: EventosAdapter // Declara el adaptador aquí

    // Formato de fecha para comparar (debe coincidir con el formato de tu Evento.fecha)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicialización del RecyclerView
        eventosAdapter = EventosAdapter(emptyList()) // Inicializa el adaptador
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventos.adapter = eventosAdapter

        // Observar los eventos desde la base de datos
        eventoViewModel.todosLosEventos.observe(viewLifecycleOwner) { todosLosEventos ->
            // Obtener la fecha actual en el mismo formato que los eventos
            val currentDate = Calendar.getInstance()
            val todayDateString = dateFormat.format(currentDate.time)

            // Filtrar los eventos para mostrar solo los de hoy
            val eventosDelDia = todosLosEventos.filter { evento ->
                try {
                    // Intenta parsear la fecha del evento para compararla robustamente
                    val eventDate = dateFormat.parse(evento.fecha)
                    val calEvent = Calendar.getInstance().apply { if (eventDate != null) time = eventDate }

                    // Compara año, mes y día
                    eventDate != null &&
                            calEvent.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                            calEvent.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                            calEvent.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)
                } catch (e: Exception) {
                    Log.e("InicioFragment", "Error al parsear fecha del evento: ${evento.fecha}, ${e.message}")
                    false // Si hay error de formato, no incluir el evento
                }
            }


            if (eventosDelDia.isEmpty()) {
                // Si no hay eventos para hoy, muestra el mensaje y oculta el RecyclerView
                binding.noEventosMessage.visibility = View.VISIBLE
                binding.rvEventos.visibility = View.GONE
            } else {
                // Si hay eventos para hoy, oculta el mensaje y muestra el RecyclerView
                binding.noEventosMessage.visibility = View.GONE
                binding.rvEventos.visibility = View.VISIBLE
                // Actualiza los datos del adaptador, no lo crees de nuevo
                eventosAdapter.updateEventos(eventosDelDia)
            }
        }
    }
}
