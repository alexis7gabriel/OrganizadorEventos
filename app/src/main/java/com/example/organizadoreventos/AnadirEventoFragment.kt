package com.example.organizadoreventos

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
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.organizadoreventos.databinding.FragmentAnadirEventoBinding
import java.util.Calendar

class AnadirEventoFragment : Fragment() {

    private var _binding: FragmentAnadirEventoBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_CONTACTS = 1001

    private val mapLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Aquí deberías obtener latitud y longitud desde el resultado
            // Por ahora mostramos texto fijo
            binding.etUbicacion.setText("Ubicación seleccionada (ejemplo)")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnadirEventoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkContactsPermissionAndLoad()

        setupSpinners()
        setupPickers()

        binding.etUbicacion.setOnClickListener {
            openMap()
        }

        binding.btnGuardar.setOnClickListener {
            guardarEvento()
        }
    }

    private fun setupPickers() {
        binding.etFecha.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(requireContext(), { _, year, month, day ->
                val date = String.format("%02d/%02d/%04d", day, month + 1, year)
                binding.etFecha.setText(date)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        binding.etHora.setOnClickListener {
            val c = Calendar.getInstance()
            val tpd = TimePickerDialog(requireContext(), { _, hour, minute ->
                val hora = String.format("%02d:%02d", hour, minute)
                binding.etHora.setText(hora)
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

    private fun openMap() {
        val gmmIntentUri = Uri.parse("geo:0,0?q=")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
            mapLauncher.launch(mapIntent)
        } else {
            Toast.makeText(requireContext(), "No se encontró app de mapas", Toast.LENGTH_SHORT).show()
        }
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

        val resumen = """
        Evento creado:
        Categoría: $categoria
        Fecha: $fecha
        Hora: $hora
        Descripción: $descripcion
        Status: $status
        Ubicación: $ubicacion
        Contacto: $contacto
        Recordatorio: $recordatorio
    """.trimIndent()

        Toast.makeText(requireContext(), resumen, Toast.LENGTH_LONG).show()

        Log.d("EventoGuardado", "--- Datos del Evento ---")
        Log.d("EventoGuardado", "Categoría: $categoria")
        Log.d("EventoGuardado", "Fecha: $fecha")
        Log.d("EventoGuardado", "Hora: $hora")
        Log.d("EventoGuardado", "Descripción: $descripcion")
        Log.d("EventoGuardado", "Status: $status")
        Log.d("EventoGuardado", "Ubicación: $ubicacion")
        Log.d("EventoGuardado", "Contacto: $contacto")
        Log.d("EventoGuardado", "Recordatorio: $recordatorio")
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
