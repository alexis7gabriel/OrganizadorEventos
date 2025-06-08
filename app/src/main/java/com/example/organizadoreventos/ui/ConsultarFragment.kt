package com.example.organizadoreventos.ui.fragmentos

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.organizadoreventos.R
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.databinding.FragmentConsultarBinding
import com.example.organizadoreventos.ui.AnadirEventoFragment // Asegúrate de esta importación
import com.example.organizadoreventos.ui.adapters.EventosAdapter
import com.example.organizadoreventos.viewmodel.EventoViewModel
import com.google.android.material.tabs.TabLayout
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

// ConsultarFragment ahora implementa la interfaz OnItemClickListener de EventosAdapter
class ConsultarFragment : Fragment(), EventosAdapter.OnItemClickListener {

    private var _binding: FragmentConsultarBinding? = null
    private val binding get() = _binding!!

    // Formato de fecha para parsear y formatear (debe coincidir con el formato de tu Evento.fecha)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    // Formato solo para año
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    // Formato solo para mes y año
    private val monthYearFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())

    private lateinit var adapter: EventosAdapter

    // ViewModel para acceder a los datos
    private val eventoViewModel: EventoViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConsultarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupRecyclerView()
        setupBusqueda()
        setupBotonConsultar()

        eventoViewModel.todosLosEventos.observe(viewLifecycleOwner) { eventos ->
            eventos.forEach {
                println("📅 Evento: ${it.fecha} - ${it.descripcion}")
            }
            aplicarFiltro()
        }
    }

    private fun setupTabs() {
        binding.tabLayoutConsulta.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.etFechaInicial.setText("")
                binding.etFechaFinal.setText("")

                when (tab.position) {
                    0 -> { // Por rango
                        binding.layoutFechas.visibility = View.VISIBLE
                        binding.etFechaInicial.hint = "Fecha Inicial (dd/MM/yyyy)"
                        binding.etFechaFinal.visibility = View.VISIBLE
                        binding.etFechaFinal.hint = "Fecha Final (dd/MM/yyyy)"
                        binding.etFechaInicial.setOnClickListener { showDatePicker { date -> binding.etFechaInicial.setText(date) } }
                        binding.etFechaFinal.setOnClickListener { showDatePicker { date -> binding.etFechaFinal.setText(date) } }
                        binding.etFechaInicial.inputType = InputType.TYPE_NULL
                        binding.etFechaFinal.inputType = InputType.TYPE_NULL
                    }
                    1 -> { // Por año
                        binding.layoutFechas.visibility = View.VISIBLE
                        binding.etFechaInicial.hint = "Año (yyyy)"
                        binding.etFechaFinal.visibility = View.GONE
                        binding.etFechaInicial.setOnClickListener {
                            showDatePicker { dateString ->
                                try {
                                    val parsedDate = dateFormat.parse(dateString)
                                    val selectedYear = yearFormat.format(parsedDate)
                                    binding.etFechaInicial.setText(selectedYear)
                                } catch (e: ParseException) {
                                    Log.e("ConsultarFragment", "Error al parsear fecha para año: ${e.message}")
                                    binding.etFechaInicial.setText("")
                                }
                            }
                        }
                        binding.etFechaInicial.inputType = InputType.TYPE_NULL
                    }
                    2 -> { // Por día
                        binding.layoutFechas.visibility = View.VISIBLE
                        binding.etFechaInicial.hint = "Fecha (dd/MM/yyyy)"
                        binding.etFechaFinal.visibility = View.GONE
                        binding.etFechaInicial.setOnClickListener { showDatePicker { date -> binding.etFechaInicial.setText(date) } }
                        binding.etFechaInicial.inputType = InputType.TYPE_NULL
                    }
                    3 -> { // Por mes
                        binding.layoutFechas.visibility = View.VISIBLE
                        binding.etFechaInicial.hint = "Mes (MM/yyyy)"
                        binding.etFechaFinal.visibility = View.GONE
                        binding.etFechaInicial.setOnClickListener {
                            showDatePicker { date ->
                                try {
                                    val parsedDate = dateFormat.parse(date)
                                    binding.etFechaInicial.setText(monthYearFormat.format(parsedDate))
                                } catch (e: ParseException) {
                                    Log.e("ConsultarFragment", "Error al parsear fecha para mes/año: ${e.message}")
                                    binding.etFechaInicial.setText("")
                                }
                            }
                        }
                        binding.etFechaInicial.inputType = InputType.TYPE_NULL
                    }
                }
                aplicarFiltro()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                aplicarFiltro()
            }
        })
        binding.tabLayoutConsulta.getTabAt(0)?.select()
    }

    private fun setupDatePickers() {
        // Handled in setupTabs()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val dpd = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            onDateSelected(dateFormat.format(selectedDate.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dpd.show()
    }

    private fun setupRecyclerView() {
        adapter = EventosAdapter(emptyList())
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventos.adapter = adapter
        adapter.setOnItemClickListener(this)
    }

    private fun setupBusqueda() {
        binding.etBusqueda.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                aplicarFiltro()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupBotonConsultar() {
        binding.btnConsultar.setOnClickListener {
            aplicarFiltro()
        }
    }

    private fun aplicarFiltro() {
        eventoViewModel.todosLosEventos.value?.let { eventos ->
            val textoBusqueda = binding.etBusqueda.text.toString().trim().lowercase(Locale.getDefault())
            val categoriaSeleccionada = getCategoriaSeleccionada()
            val tabPos = binding.tabLayoutConsulta.selectedTabPosition

            val fechaInicialStr = binding.etFechaInicial.text.toString()
            val fechaFinalStr = binding.etFechaFinal.text.toString()

            val eventosFiltrados = eventos.filter { evento ->
                val cumpleCategoria = categoriaSeleccionada == "Todos" || evento.categoria.equals(categoriaSeleccionada, ignoreCase = true)

                val cumpleTexto = textoBusqueda.isEmpty() || (
                        evento.descripcion.lowercase(Locale.getDefault()).contains(textoBusqueda) ||
                                evento.contacto.lowercase(Locale.getDefault()).contains(textoBusqueda) ||
                                evento.ubicacion.lowercase(Locale.getDefault()).contains(textoBusqueda)
                        )

                var cumpleFecha = true

                if (fechaInicialStr.isNotEmpty()) {
                    try {
                        val eventDate = dateFormat.parse(evento.fecha) ?: throw ParseException("Invalid date", 0)
                        val eventCalendar = Calendar.getInstance().apply { time = eventDate }

                        when (tabPos) {
                            0 -> { // Por rango
                                if (fechaFinalStr.isEmpty()) {
                                    cumpleFecha = false
                                } else {
                                    val startDate = dateFormat.parse(fechaInicialStr) ?: throw ParseException("Invalid start date", 0)
                                    val endDate = dateFormat.parse(fechaFinalStr) ?: throw ParseException("Invalid end date", 0)

                                    val startCal = Calendar.getInstance().apply { time = startDate }
                                    val endCal = Calendar.getInstance().apply { time = endDate }

                                    startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
                                    endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59); endCal.set(Calendar.MILLISECOND, 999)
                                    eventCalendar.set(Calendar.HOUR_OF_DAY, 0); eventCalendar.set(Calendar.MINUTE, 0); eventCalendar.set(Calendar.SECOND, 0); eventCalendar.set(Calendar.MILLISECOND, 0)

                                    cumpleFecha = !eventCalendar.before(startCal) && !eventCalendar.after(endCal)
                                }
                            }
                            1 -> { // Por año
                                val yearToFilter = fechaInicialStr
                                val eventYear = yearFormat.format(eventDate)
                                cumpleFecha = eventYear == yearToFilter
                            }
                            2 -> { // Por día
                                cumpleFecha = evento.fecha == fechaInicialStr
                            }
                            3 -> { // Por mes
                                val monthYearToFilter = fechaInicialStr
                                val eventMonthYear = monthYearFormat.format(eventDate)
                                cumpleFecha = eventMonthYear == monthYearToFilter
                            }
                        }
                    } catch (e: ParseException) {
                        Log.e("ConsultarFragment", "Error al parsear fecha en filtro: ${e.message}. Fecha: $fechaInicialStr, Evento fecha: ${evento.fecha}")
                        cumpleFecha = false
                    } catch (e: Exception) {
                        Log.e("ConsultarFragment", "Error inesperado en filtro de fecha: ${e.message}. Fecha: $fechaInicialStr, Evento fecha: ${evento.fecha}")
                        cumpleFecha = false
                    }
                }

                cumpleCategoria && cumpleTexto && cumpleFecha
            }.sortedWith(compareBy<Evento> {
                try { dateFormat.parse(it.fecha) } catch (e: ParseException) { Date(0) }
            }.thenBy {
                try { SimpleDateFormat("HH:mm", Locale.getDefault()).parse(it.hora) } catch (e: ParseException) { Date(0) }
            })


            adapter.updateEventos(eventosFiltrados)
            if (eventosFiltrados.isEmpty()) {
                binding.noEventosMessageConsulta.visibility = View.VISIBLE
                binding.rvEventos.visibility = View.GONE
            } else {
                binding.noEventosMessageConsulta.visibility = View.GONE
                binding.rvEventos.visibility = View.VISIBLE
            }
        }
    }


    private fun getCategoriaSeleccionada(): String {
        val selectedTab = binding.tabCategoriaFiltro.getTabAt(binding.tabCategoriaFiltro.selectedTabPosition)
        return selectedTab?.text?.toString() ?: "Todos"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Implementación de la interfaz OnItemClickListener ---
    override fun onItemClick(evento: Evento) {
        Log.d("ConsultarFragment", "Evento clickeado en Consultar: ${evento.descripcion}")
    }

    override fun onMapButtonClick(ubicacion: String) {
        Log.d("ConsultarFragment", "Clic en mapa para ubicación: $ubicacion")
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
            Log.e("ConsultarFragment", "No hay aplicación de mapas disponible para la Intent: $gmmIntentUri")
        }
    }

    // <--- ¡NUEVO!: Implementación del clic en el botón de edición
    override fun onEditButtonClick(evento: Evento) {
        Log.d("ConsultarFragment", "Clic en editar evento: ${evento.descripcion} (ID: ${evento.idEvento})")

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
