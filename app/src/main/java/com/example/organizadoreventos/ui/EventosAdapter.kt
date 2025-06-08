package com.example.organizadoreventos.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.databinding.ItemEventoBinding

class EventosAdapter(private val eventos: List<Evento>) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    inner class EventoViewHolder(private val binding: ItemEventoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(evento: Evento) {
            binding.tvFecha.text = evento.fecha
            binding.tvCategoria.text = evento.categoria
            binding.tvDescripcion.text = evento.descripcion
            binding.tvStatus.text = evento.status
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.bind(evento)
    }

    override fun getItemCount(): Int = eventos.size
}
