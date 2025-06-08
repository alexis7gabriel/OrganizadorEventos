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
import com.example.organizadoreventos.ui.AnadirEventoFragment // Asegúrate de esta importación
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

        // Establece el listener en el adaptador.
        // 'this' se refiere a InicioFragment, que implementa OnItemClickListener.
        eventosAdapter.setOnItemClickListener(this)

        // Observar los eventos desde la base de datos
        eventoViewModel.todosLosEventos.observe(viewLifecycleOwner) { todosLosEventos ->
            // Obtener la fecha actual (solo día, mes, año)
            val today = LocalDate.now()

            // Calcular la fecha límite: hoy + 2 días (total 3 días incluyendo hoy)
            val threeDaysFromNow = today.plusDays(2) // Esto será el día de hoy + 2 días futuros

            // Filtrar los eventos para mostrar solo los de hoy y los próximos dos días
            val eventosFiltrados = todosLosEventos.filter { evento ->
                try {
                    val eventDate = LocalDate.parse(evento.fecha, dateFormatter)
                    // Compara si la fecha del evento está dentro del rango [hoy, hoy + 2 días] (inclusive)
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
                    // Convertir a epoch day y nano of day para una comparación única de fecha y hora
                    date.toEpochDay() * 86400_000_000_000L + time.toNanoOfDay()
                } catch (e: Exception) {
                    Long.MIN_VALUE // Usar un valor mínimo para ordenar eventos con fecha/hora inválida al principio
                }
            }


            if (eventosFiltrados.isEmpty()) {
                // Si no hay eventos para el rango de fechas, muestra el mensaje y oculta el RecyclerView
                binding.noEventosMessage.visibility = View.VISIBLE
                binding.rvEventos.visibility = View.GONE
                binding.noEventosMessage.text = "¡No hay eventos programados para los próximos 3 días!" // Actualiza el texto
            } else {
                // Si hay eventos, oculta el mensaje y muestra el RecyclerView
                binding.noEventosMessage.visibility = View.GONE
                binding.rvEventos.visibility = View.VISIBLE
                // Actualiza los datos del adaptador
                eventosAdapter.updateEventos(eventosFiltrados)
            }
        }
    }

    // --- Implementación de la interfaz OnItemClickListener ---

    override fun onItemClick(evento: Evento) {
        // Esta función se llama cuando se hace clic en la sección colapsada del ítem.
        // La lógica de expansión/contracción ya se maneja dentro del EventoViewHolder.
        // Puedes añadir aquí cualquier otra acción global que quieras al hacer clic en un ítem.
        Log.d("InicioFragment", "Clic en evento: ${evento.descripcion}")
    }

    override fun onMapButtonClick(ubicacion: String) {
        // Esta función se llama cuando se hace clic en el icono del mapa.
        // Abre una aplicación de mapas con la ubicación proporcionada.
        Log.d("InicioFragment", "Clic en mapa para ubicación: $ubicacion")

        // Crea un URI geográfico para la ubicación. Uri.encode() asegura que la cadena sea URL-safe.
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(ubicacion)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        // Opcional: Especifica que se intente abrir Google Maps. Si no está instalado, buscará otras apps.
        mapIntent.setPackage("com.google.android.apps.maps")

        // Verifica si hay alguna aplicación que pueda manejar esta intención
        if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Si no se encuentra una aplicación de mapas compatible, muestra un Toast
            Toast.makeText(
                requireContext(),
                "No se encontró una aplicación de mapas para mostrar la ubicación.",
                Toast.LENGTH_LONG
            ).show()
            Log.e("InicioFragment", "No hay aplicación de mapas disponible para la Intent: $gmmIntentUri")
        }
    }

    // <--- ¡NUEVO!: Implementación del clic en el botón de edición
    override fun onEditButtonClick(evento: Evento) {
        Log.d("InicioFragment", "Clic en editar evento: ${evento.descripcion} (ID: ${evento.idEvento})")

        // Crear un Bundle para pasar los datos del evento al fragmento de edición
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

        // Navegar a AnadirEventoFragment con los argumentos para editar
        val anadirEventoFragment = AnadirEventoFragment().apply {
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, anadirEventoFragment)
            .addToBackStack(null) // Permite al usuario regresar al fragmento anterior
            .commit()
    }
}
