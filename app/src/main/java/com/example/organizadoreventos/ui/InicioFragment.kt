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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private val eventoViewModel: EventoViewModel by viewModels()
    private lateinit var binding: FragmentInicioBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del RecyclerView
        var eventosAdapter = EventosAdapter(emptyList())
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventos.adapter = eventosAdapter

        // Observar los eventos y actualizarlos en el RecyclerView
        eventoViewModel.todosLosEventos.observe(viewLifecycleOwner) { eventos ->
            eventosAdapter = EventosAdapter(eventos)
            binding.rvEventos.adapter = eventosAdapter
        }
    }
}
