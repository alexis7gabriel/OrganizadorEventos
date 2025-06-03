package com.example.organizadoreventos

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.organizadoreventos.databinding.FragmentConsultarBinding
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

class ConsultarFragment : Fragment() {

    private var _binding: FragmentConsultarBinding? = null
    private val binding get() = _binding!!

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val eventosTotales = mutableListOf<Evento>()
    private val eventosFiltrados = mutableListOf<Evento>()
    private lateinit var adapter: EventosAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConsultarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupDatePickers()
        setupRecyclerView()
        setupBusqueda()
        setupBotonConsultar()

        cargarEventosSimulados()
        aplicarFiltro() // mostrar todos inicialmente
    }

    private fun setupTabs() {
        binding.tabLayoutConsulta.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { // Por rango
                        binding.layoutFechas.visibility = View.VISIBLE
                        binding.etFechaInicial.hint = "Fecha Inicial"
                        binding.etFechaFinal.visibility = View.VISIBLE
                    }
                    1 -> { // Por año
                        binding.layoutFechas.visibility = View.VISIBLE
                        binding.etFechaInicial.hint = "Año"
                        binding.etFechaFinal.visibility = View.GONE
                    }
                    2 -> { // Por día
                        binding.layoutFechas.visibility = View.VISIBLE
                        binding.etFechaInicial.hint = "Fecha"
                        binding.etFechaFinal.visibility = View.GONE
                    }
                    3 -> { // Por mes
                        binding.layoutFechas.visibility = View.VISIBLE
                        binding.etFechaInicial.hint = "Mes (MM/yyyy)"
                        binding.etFechaFinal.visibility = View.GONE
                    }
                }
                // Limpiar fechas al cambiar tab
                binding.etFechaInicial.setText("")
                binding.etFechaFinal.setText("")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Inicial: seleccionar primer tab
        binding.tabLayoutConsulta.getTabAt(0)?.select()
    }

    private fun setupDatePickers() {
        binding.etFechaInicial.setOnClickListener {
            showDatePicker { date ->
                binding.etFechaInicial.setText(date)
            }
        }
        binding.etFechaFinal.setOnClickListener {
            showDatePicker { date ->
                binding.etFechaFinal.setText(date)
            }
        }
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
        adapter = EventosAdapter(eventosFiltrados)
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventos.adapter = adapter
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
        val textoBusqueda = binding.etBusqueda.text.toString().trim().lowercase(Locale.getDefault())
        val categoriaSeleccionada = getCategoriaSeleccionada()
        val tabPos = binding.tabLayoutConsulta.selectedTabPosition

        val fechaInicialStr = binding.etFechaInicial.text.toString()
        val fechaFinalStr = binding.etFechaFinal.text.toString()

        // Para simplicidad, el filtro de fecha se basa en strings. Idealmente parsear fechas.
        eventosFiltrados.clear()
        eventosFiltrados.addAll(eventosTotales.filter { evento ->
            val cumpleCategoria = evento.categoria.equals(categoriaSeleccionada, ignoreCase = true)

            val cumpleTexto = textoBusqueda.isEmpty() || (
                    evento.descripcion.lowercase(Locale.getDefault()).contains(textoBusqueda) ||
                            evento.contacto.lowercase(Locale.getDefault()).contains(textoBusqueda)
                    )

            val cumpleFecha = when (tabPos) {
                0 -> { // Por rango
                    // Considerar evento.fecha entre fechaInicialStr y fechaFinalStr
                    if (fechaInicialStr.isEmpty() || fechaFinalStr.isEmpty()) true
                    else evento.fecha >= fechaInicialStr && evento.fecha <= fechaFinalStr
                }
                1 -> { // Por año
                    if (fechaInicialStr.isEmpty()) true
                    else evento.fecha.startsWith(fechaInicialStr.takeLast(4)) // año en yyyy
                }
                2 -> { // Por día
                    if (fechaInicialStr.isEmpty()) true
                    else evento.fecha == fechaInicialStr
                }
                3 -> { // Por mes
                    if (fechaInicialStr.isEmpty()) true
                    else {
                        val mesAnio = fechaInicialStr.split("/")
                        if (mesAnio.size == 2) {
                            val mes = mesAnio[0].padStart(2, '0')
                            val anio = mesAnio[1]
                            evento.fecha.startsWith("$anio-$mes")
                        } else true
                    }
                }
                else -> true
            }

            cumpleCategoria && cumpleTexto && cumpleFecha
        })

        adapter.notifyDataSetChanged()
    }

    private fun getCategoriaSeleccionada(): String {
        val index = binding.tabCategoriaFiltro.selectedTabPosition
        return binding.tabCategoriaFiltro.getTabAt(index)?.text?.toString() ?: "Cita"
    }


    private fun cargarEventosSimulados() {
        eventosTotales.clear()
        eventosTotales.add(
            Evento(
                fecha = "2018-05-11",
                hora = "00:11",
                categoria = "Cita",
                status = "pendiente",
                descripcion = "Cita para comer",
                contacto = "Alejandro"
            )
        )
        eventosTotales.add(
            Evento(
                fecha = "2018-05-12",
                hora = "14:00",
                categoria = "Junta",
                status = "realizado",
                descripcion = "Reunión importante",
                contacto = "María"
            )
        )
        eventosTotales.add(
            Evento(
                fecha = "2018-05-15",
                hora = "10:30",
                categoria = "Examen",
                status = "aplazado",
                descripcion = "Examen de matemáticas",
                contacto = "Carlos"
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
