package com.example.organizadoreventos.ui.fragmentos

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.organizadoreventos.R
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.databinding.FragmentInicioBinding
import com.example.organizadoreventos.ui.AnadirEventoFragment
import com.example.organizadoreventos.ui.adapters.EventosAdapter
import com.example.organizadoreventos.viewmodel.EventoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.time.LocalTime


class InicioFragment : Fragment(), EventosAdapter.OnItemClickListener {

    private val eventoViewModel: EventoViewModel by viewModels()
    private lateinit var binding: FragmentInicioBinding
    private lateinit var eventosAdapter: EventosAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicialización del RecyclerView
        eventosAdapter = EventosAdapter(emptyList())
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventos.adapter = eventosAdapter

        eventosAdapter.setOnItemClickListener(this)

        // Observar los eventos desde la base de datos
        eventoViewModel.todosLosEventos.observe(viewLifecycleOwner) { todosLosEventos ->
            // Obtener la fecha actual (solo día, mes, año)
            val today = LocalDate.now()

            // Calcular la fecha límite: hoy + 2 días (total 3 días incluyendo hoy)
            val threeDaysFromNow = today.plusDays(2)

            // Filtrar los eventos para mostrar solo los de hoy y los próximos dos días
            val eventosFiltrados = todosLosEventos.filter { evento ->
                try {
                    val eventDate = LocalDate.parse(evento.fecha, dateFormatter)
                    // Compara si la fecha del evento está dentro del rango
                    !eventDate.isBefore(today) && !eventDate.isAfter(threeDaysFromNow)
                } catch (e: DateTimeParseException) {
                    Log.e("InicioFragment", "Error al parsear fecha del evento '${evento.fecha}': Formato incorrecto. ${e.message}")
                    false
                } catch (e: Exception) {
                    Log.e("InicioFragment", "Error inesperado al filtrar evento '${evento.fecha}': ${e.message}")
                    false
                }
            }.sortedBy { evento ->
                try {
                    val date = LocalDate.parse(evento.fecha, dateFormatter)
                    val time = try {
                        val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                        LocalTime.parse(evento.hora, timeFormat)
                    } catch (e: Exception) {
                        LocalTime.MIN
                    }
                    date.toEpochDay() * 86400_000_000_000L + time.toNanoOfDay()
                } catch (e: Exception) {
                    Long.MIN_VALUE
                }
            }


            if (eventosFiltrados.isEmpty()) {
                binding.noEventosMessage.visibility = View.VISIBLE
                binding.rvEventos.visibility = View.GONE
                binding.noEventosMessage.text = "¡No hay eventos programados para los próximos 3 días!"
            } else {
                binding.noEventosMessage.visibility = View.GONE
                binding.rvEventos.visibility = View.VISIBLE
                eventosAdapter.updateEventos(eventosFiltrados)
            }
        }
    }

    override fun onItemClick(evento: Evento) {
        Log.d("InicioFragment", "Clic en evento: ${evento.descripcion}")
    }

    override fun onMapButtonClick(ubicacion: String) {
        Log.d("InicioFragment", "Clic en mapa para ubicación: $ubicacion")

        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(ubicacion)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(
                requireContext(),
                "No se encontró una aplicación de mapas para mostrar la ubicación.",
                Toast.LENGTH_LONG
            ).show()
            Log.e("InicioFragment", "No hay aplicación de mapas disponible para la Intent: $gmmIntentUri")
        }
    }

    override fun onEditButtonClick(evento: Evento) {
        Log.d("InicioFragment", "Clic en editar evento: ${evento.descripcion} (ID: ${evento.idEvento})")

        val bundle = Bundle().apply {
            putInt("eventId", evento.idEvento)
            putString("fecha", evento.fecha)
            putString("hora", evento.hora)
            putString("categoria", evento.categoria)
            putString("status", evento.status)
            putString("descripcion", evento.descripcion)
            putString("contacto", evento.contacto)
            putString("ubicacion", evento.ubicacion)
            putString("recordatorio", evento.recordatorio)
        }

        val anadirEventoFragment = AnadirEventoFragment().apply {
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, anadirEventoFragment)
            .addToBackStack(null)
            .commit()
    }
}
