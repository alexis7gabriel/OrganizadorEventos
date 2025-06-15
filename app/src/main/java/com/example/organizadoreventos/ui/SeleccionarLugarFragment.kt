package com.example.organizadoreventos.ui

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.organizadoreventos.R
import com.example.organizadoreventos.databinding.FragmentSeleccionarMapaBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.Locale

class SeleccionarLugarFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentSeleccionarMapaBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null

    companion object {
        const val REQUEST_KEY = "request_key_location_selection"
        const val BUNDLE_KEY_ADDRESS = "bundle_key_address"
        const val BUNDLE_KEY_LAT = "bundle_key_lat"
        const val BUNDLE_KEY_LNG = "bundle_key_lng"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeleccionarMapaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener la instancia del SupportMapFragment y preparar el mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.btnConfirmLocation.setOnClickListener {
            if (selectedAddress != null) {
                // Enviar el resultado de vuelta a AnadirEventoFragment
                setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(BUNDLE_KEY_ADDRESS to selectedAddress)
                )
                // Volver al fragmento anterior (AnadirEventoFragment)
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Por favor, selecciona una ubicación en el mapa.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val mexicoCity = LatLng(19.4326, -99.1332)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mexicoCity, 10f))

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        googleMap.setOnMapLongClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
            selectedLatLng = latLng
            getAddressFromLatLng(latLng)

            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }

        googleMap.setOnMapClickListener {
        }
    }

    private fun getAddressFromLatLng(latLng: LatLng) {
        if (context == null) return

        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val fullAddress = address.getAddressLine(0)
                selectedAddress = fullAddress
                binding.tvSelectedAddress.text = fullAddress
                binding.tvSelectedAddress.visibility = View.VISIBLE
            } else {
                selectedAddress = "${latLng.latitude}, ${latLng.longitude}"
                binding.tvSelectedAddress.text = "Sin dirección: ${selectedAddress}"
                binding.tvSelectedAddress.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "No se encontró dirección para este punto.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Log.e("LocationSelection", "Error de geocodificación: ${e.message}")
            selectedAddress = "${latLng.latitude}, ${latLng.longitude}"
            binding.tvSelectedAddress.text = "Error de red: ${selectedAddress}"
            binding.tvSelectedAddress.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Error de red al obtener dirección.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
