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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener // Importar para escuchar resultados de fragmentos
import androidx.fragment.app.viewModels
import com.example.organizadoreventos.R
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.databinding.FragmentAnadirEventoBinding
import com.example.organizadoreventos.viewmodel.EventoViewModel
import com.google.android.libraries.places.api.Places // Importa la API de Places (aún útil si usas Place Autocomplete en otro lugar)
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

    // Este launcher era para Place Autocomplete, ya no se usará directamente para el pin
    // Puedes eliminarlo si ya no usas el Place Autocomplete en ningún otro lado.
    /*
    private val placeAutocompleteLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    val placeName = place.name ?: place.address ?: "Ubicación seleccionada"
                    binding.etUbicacion.setText(placeName)
                    Log.d("PlaceSelected", "Place: ${place.name}, ${place.address}")
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.i("PlaceAutocomplete", "User canceled autocomplete")
                Toast.makeText(requireContext(), "Selección de ubicación cancelada", Toast.LENGTH_SHORT).show()
            }
            AutocompleteActivityMode.RESULT_ERROR -> {
                val status = Autocomplete.getStatusFromIntent(result.data!!)
                Log.e("PlaceAutocomplete", "Error: ${status.statusMessage}")
                Toast.makeText(requireContext(), "Error al buscar ubicación: ${status.statusMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
    */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnadirEventoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa el Places SDK (aún es necesario si vas a usar Place Autocomplete en el futuro o Geocoding API)
        if (!Places.isInitialized()) {
            val apiKey = resources.getString(R.string.google_maps_api_key) // Asumiendo que la API key está en strings.xml
            Places.initialize(requireContext(), apiKey)
        }

        // Configura el listener para recibir el resultado del fragmento de selección de mapa
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
            // Lanza el nuevo fragmento para seleccionar la ubicación en el mapa
            openMapForSelection()
        }

        binding.btnGuardar.setOnClickListener {
            guardarEvento()
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
                contactos.add(name)
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

    // Función para abrir el fragmento de selección de ubicación en el mapa
    private fun openMapForSelection() {
        val selectLocationFragment = SeleccionarLugarFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, selectLocationFragment) // Usa tu ID de contenedor de fragmentos
            .addToBackStack(null) // Agrega a la pila para poder regresar
            .commit()
    }

    private fun guardarEvento() {
        val categoria = binding.tabCategoria.getTabAt(binding.tabCategoria.selectedTabPosition)?.text.toString()
        val fecha = binding.etFecha.text.toString()
        val hora = binding.etHora.text.toString()
        val descripcion = binding.etDescripcion.text.toString()
        val status = binding.spinnerStatus.selectedItem.toString()
        val ubicacion = binding.etUbicacion.text.toString()
        val contacto = binding.spinnerContacto.selectedItem?.toString() ?: "No seleccionado"
        val recordatorio = binding.spinnerRecordatorio.selectedItem.toString()

        // Validaciones básicas de campos
        if (fecha.isEmpty() || hora.isEmpty() || descripcion.isEmpty() || ubicacion.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos obligatorios (incluida la ubicación).", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un objeto Evento
        val evento = Evento(
            idUsuario = 1,
            fecha = fecha,
            hora = hora,
            categoria = categoria,
            status = status,
            descripcion = descripcion,
            contacto = contacto,
            ubicacion = ubicacion,
            recordatorio = recordatorio
        )

        // Insertar el evento en la base de datos a través del ViewModel
        eventoViewModel.insertarEvento(evento)

        // Mostrar un mensaje de confirmación
        Toast.makeText(requireContext(), "Evento guardado exitosamente", Toast.LENGTH_LONG).show()

        // Para ver los datos en el log
        Log.d("EventoGuardado", "--- Datos del Evento ---")
        Log.d("EventoGuardado", "Categoría: $categoria")
        Log.d("EventoGuardado", "Fecha: $fecha")
        Log.d("EventoGuardado", "Hora: $hora")
        Log.d("EventoGuardado", "Descripción: $descripcion")
        Log.d("EventoGuardado", "Status: $status")
        Log.d("EventoGuardado", "Ubicación: $ubicacion")
        Log.d("EventoGuardado", "Contacto: $contacto")
        Log.d("EventoGuardado", "Recordatorio: $recordatorio")

        // Limpiar campos después de guardar (opcional)
        binding.etFecha.setText("")
        binding.etHora.setText("")
        binding.etDescripcion.setText("")
        binding.etUbicacion.setText("")
        binding.spinnerStatus.setSelection(0)
        binding.spinnerContacto.setSelection(0)
        binding.spinnerRecordatorio.setSelection(0)
        binding.tabCategoria.getTabAt(0)?.select()
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
