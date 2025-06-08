package com.example.organizadoreventos.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Importar AlertDialog de AndroidX
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.organizadoreventos.R
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.databinding.FragmentAnadirEventoBinding
import com.example.organizadoreventos.viewmodel.EventoViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnadirEventoFragment : Fragment() {

    private var _binding: FragmentAnadirEventoBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_CONTACTS = 1001

    private val eventoViewModel: EventoViewModel by viewModels()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var eventIdToEdit: Int? = null // Para almacenar el ID del evento si estamos en modo edición
    private var currentEvento: Evento? = null // Para almacenar el evento actual en modo edición

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnadirEventoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!Places.isInitialized()) {
            val apiKey = resources.getString(R.string.google_maps_api_key)
            Places.initialize(requireContext(), apiKey)
        }

        setFragmentResultListener(SeleccionarLugarFragment.REQUEST_KEY) { requestKey, bundle ->
            if (requestKey == SeleccionarLugarFragment.REQUEST_KEY) {
                val selectedAddress = bundle.getString(SeleccionarLugarFragment.BUNDLE_KEY_ADDRESS)
                if (!selectedAddress.isNullOrEmpty()) {
                    binding.etUbicacion.setText(selectedAddress)
                    Toast.makeText(requireContext(), "Ubicación seleccionada: $selectedAddress", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show()
                }
            }
        }

        checkContactsPermissionAndLoad()
        setupSpinners()
        setupPickers()

        binding.etUbicacion.setOnClickListener {
            openMapForSelection()
        }

        // --- Lógica para determinar si estamos en modo edición ---
        arguments?.let { bundle ->
            eventIdToEdit = bundle.getInt("eventId", -1).takeIf { it != -1 }
            if (eventIdToEdit != null) {
                // Estamos en modo edición: precargar datos, cambiar texto y visibilidad
                binding.tvScreenTitle.text = "Modificar Evento" // Cambiar título de la pantalla
                binding.btnGuardar.text = "Actualizar Evento"
                binding.btnEliminar.visibility = View.VISIBLE // Hacer visible el botón de eliminar
                loadEventDataForEdit(bundle)
            } else {
                binding.tvScreenTitle.text = "Añadir Nuevo Evento" // Título por defecto
                binding.btnGuardar.text = "Guardar Evento"
                binding.btnEliminar.visibility = View.GONE // Asegurarse de que esté oculto
            }
        } ?: run {
            binding.tvScreenTitle.text = "Añadir Nuevo Evento" // Título por defecto si no hay argumentos
            binding.btnGuardar.text = "Guardar Evento"
            binding.btnEliminar.visibility = View.GONE
        }
        // --- FIN Lógica de Edición ---

        binding.btnGuardar.setOnClickListener {
            if (eventIdToEdit != null) {
                actualizarEvento()
            } else {
                guardarNuevoEvento()
            }
        }

        // --- ¡NUEVO!: Listener para el botón de eliminar ---
        binding.btnEliminar.setOnClickListener {
            mostrarDialogoConfirmacionEliminar()
        }
        // --- FIN NUEVO LISTENER ---
    }

    private fun loadEventDataForEdit(bundle: Bundle) {
        // Almacenar el evento completo para facilitar la eliminación si es necesario
        currentEvento = Evento(
            idEvento = bundle.getInt("eventId"),
            idUsuario = 1, // Asumiendo un idUsuario fijo o manejándolo de forma externa
            fecha = bundle.getString("fecha") ?: "",
            hora = bundle.getString("hora") ?: "",
            categoria = bundle.getString("categoria") ?: "",
            status = bundle.getString("status") ?: "",
            descripcion = bundle.getString("descripcion") ?: "",
            contacto = bundle.getString("contacto") ?: "",
            ubicacion = bundle.getString("ubicacion") ?: "",
            recordatorio = bundle.getString("recordatorio") ?: ""
        )

        binding.etFecha.setText(currentEvento?.fecha)
        binding.etHora.setText(currentEvento?.hora)
        binding.etDescripcion.setText(currentEvento?.descripcion)
        binding.etUbicacion.setText(currentEvento?.ubicacion)

        val statusAdapter = binding.spinnerStatus.adapter as? ArrayAdapter<String>
        statusAdapter?.let { adapter ->
            val position = adapter.getPosition(currentEvento?.status)
            if (position != -1) binding.spinnerStatus.setSelection(position)
        }

        val contactoAdapter = binding.spinnerContacto.adapter as? ArrayAdapter<String>
        contactoAdapter?.let { adapter ->
            val position = adapter.getPosition(currentEvento?.contacto)
            if (position != -1) binding.spinnerContacto.setSelection(position)
        }

        val recordatorioAdapter = binding.spinnerRecordatorio.adapter as? ArrayAdapter<String>
        recordatorioAdapter?.let { adapter ->
            val position = adapter.getPosition(currentEvento?.recordatorio)
            if (position != -1) binding.spinnerRecordatorio.setSelection(position)
        }

        val tabLayout = binding.tabCategoria
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (tab?.text.toString().equals(currentEvento?.categoria, ignoreCase = true)) {
                if (tab != null) {
                    tab.select()
                }
                break
            }
        }
    }


    private fun setupPickers() {
        binding.etFecha.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(requireContext(), { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                binding.etFecha.setText(dateFormat.format(selectedDate.time))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        binding.etHora.setOnClickListener {
            val c = Calendar.getInstance()
            val tpd = TimePickerDialog(requireContext(), { _, hour, minute ->
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedTime.set(Calendar.MINUTE, minute)
                binding.etHora.setText(timeFormat.format(selectedTime.time))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true)
            tpd.show()
        }
    }

    private fun checkContactsPermissionAndLoad() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadContactos()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                Toast.makeText(
                    requireContext(),
                    "Permiso de contactos necesario para seleccionar contacto",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACTS)
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACTS)
            }
        }
    }

    private fun loadContactos() {
        val contactos = mutableListOf<String>()
        // Añadir una opción por defecto "Seleccionar contacto"
        contactos.add("Seleccionar contacto")
        val cursor = requireContext().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                // Evitar duplicados si el nombre ya está en la lista (por la opción por defecto)
                if (!contactos.contains(name)) {
                    contactos.add(name)
                }
            }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, contactos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerContacto.adapter = adapter
    }


    private fun setupSpinners() {
        val estados = listOf("Pendiente", "Realizado", "Aplazado")
        val adapterEstados =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estados)
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapterEstados

        val recordatorios = listOf("Sin recordatorio", "10 minutos antes", "1 día antes")
        val adapterRecordatorios =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recordatorios)
        adapterRecordatorios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRecordatorio.adapter = adapterRecordatorios
    }

    private fun openMapForSelection() {
        val selectLocationFragment = SeleccionarLugarFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, selectLocationFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun guardarNuevoEvento() {
        val categoria = binding.tabCategoria.getTabAt(binding.tabCategoria.selectedTabPosition)?.text.toString()
        val fecha = binding.etFecha.text.toString()
        val hora = binding.etHora.text.toString()
        val descripcion = binding.etDescripcion.text.toString()
        val status = binding.spinnerStatus.selectedItem.toString()
        val ubicacion = binding.etUbicacion.text.toString()
        val contacto = binding.spinnerContacto.selectedItem?.toString() ?: "No seleccionado"
        val recordatorio = binding.spinnerRecordatorio.selectedItem.toString()

        if (fecha.isEmpty() || hora.isEmpty() || descripcion.isEmpty() || ubicacion.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
            return
        }

        val evento = Evento(
            idUsuario = 1, // Puedes manejar esto de forma más dinámica si tienes usuarios reales
            fecha = fecha,
            hora = hora,
            categoria = categoria,
            status = status,
            descripcion = descripcion,
            contacto = contacto,
            ubicacion = ubicacion,
            recordatorio = recordatorio
        )

        eventoViewModel.insertarEvento(evento)
        Toast.makeText(requireContext(), "Evento guardado exitosamente", Toast.LENGTH_LONG).show()
        logEventData(evento)
        clearFormFields()
    }

    private fun actualizarEvento() {
        val id = eventIdToEdit ?: run {
            Toast.makeText(requireContext(), "Error: No se pudo obtener el ID del evento para actualizar.", Toast.LENGTH_SHORT).show()
            return
        }
        val categoria = binding.tabCategoria.getTabAt(binding.tabCategoria.selectedTabPosition)?.text.toString()
        val fecha = binding.etFecha.text.toString()
        val hora = binding.etHora.text.toString()
        val descripcion = binding.etDescripcion.text.toString()
        val status = binding.spinnerStatus.selectedItem.toString()
        val ubicacion = binding.etUbicacion.text.toString()
        val contacto = binding.spinnerContacto.selectedItem?.toString() ?: "No seleccionado"
        val recordatorio = binding.spinnerRecordatorio.selectedItem.toString()

        if (fecha.isEmpty() || hora.isEmpty() || descripcion.isEmpty() || ubicacion.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
            return
        }

        val eventoActualizado = Evento(
            idEvento = id, // Asegúrate de usar el ID existente
            idUsuario = 1, // Mantén el mismo ID de usuario
            fecha = fecha,
            hora = hora,
            categoria = categoria,
            status = status,
            descripcion = descripcion,
            contacto = contacto,
            ubicacion = ubicacion,
            recordatorio = recordatorio
        )

        eventoViewModel.actualizarEvento(eventoActualizado)
        Toast.makeText(requireContext(), "Evento actualizado exitosamente", Toast.LENGTH_LONG).show()
        logEventData(eventoActualizado)
        clearFormFields()

        // Volver al fragmento anterior después de actualizar
        requireActivity().supportFragmentManager.popBackStack()
    }

    // --- ¡NUEVO!: Diálogo de confirmación para eliminar ---
    private fun mostrarDialogoConfirmacionEliminar() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar este evento?")
            .setPositiveButton("Sí") { dialog, _ ->
                currentEvento?.let { evento ->
                    eventoViewModel.eliminarEvento(evento)
                    Toast.makeText(requireContext(), "Evento eliminado.", Toast.LENGTH_SHORT).show()
                    // Después de eliminar, limpiar formulario y volver a la pantalla anterior
                    clearFormFields()
                    requireActivity().supportFragmentManager.popBackStack()
                } ?: run {
                    Toast.makeText(requireContext(), "Error: No se pudo eliminar el evento.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    // --- FIN NUEVO DIÁLOGO ---


    private fun logEventData(evento: Evento) {
        Log.d("Evento", "--- Datos del Evento ---")
        Log.d("Evento", "ID: ${evento.idEvento}")
        Log.d("Evento", "Categoría: ${evento.categoria}")
        Log.d("Evento", "Fecha: ${evento.fecha}")
        Log.d("Evento", "Hora: ${evento.hora}")
        Log.d("Evento", "Descripción: ${evento.descripcion}")
        Log.d("Evento", "Status: ${evento.status}")
        Log.d("Evento", "Ubicación: ${evento.ubicacion}")
        Log.d("Evento", "Contacto: ${evento.contacto}")
        Log.d("Evento", "Recordatorio: ${evento.recordatorio}")
    }

    private fun clearFormFields() {
        binding.etFecha.setText("")
        binding.etHora.setText("")
        binding.etDescripcion.setText("")
        binding.etUbicacion.setText("")
        binding.spinnerStatus.setSelection(0)
        binding.spinnerContacto.setSelection(0)
        binding.spinnerRecordatorio.setSelection(0)
        binding.tabCategoria.getTabAt(0)?.select()
        eventIdToEdit = null // Importante: resetear el ID de edición
        currentEvento = null // Limpiar el evento actual
        binding.btnGuardar.text = "Guardar Evento" // Restablecer texto del botón
        binding.tvScreenTitle.text = "Añadir Nuevo Evento" // Restablecer título
        binding.btnEliminar.visibility = View.GONE // Ocultar botón de eliminar
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContactos()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso denegado, no se pueden cargar contactos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
