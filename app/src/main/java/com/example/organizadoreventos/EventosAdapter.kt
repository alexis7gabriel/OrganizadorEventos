package com.example.organizadoreventos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventos.databinding.ItemEventoBinding

class EventosAdapter(private val eventos: List<Evento>) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    inner class EventoViewHolder(val binding: ItemEventoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.binding.tvFecha.text = evento.fecha
        holder.binding.tvHora.text = evento.hora
        holder.binding.tvCategoria.text = evento.categoria
        holder.binding.tvStatus.text = evento.status
        holder.binding.tvDescripcion.text = evento.descripcion
        holder.binding.tvContacto.text = evento.contacto
    }

    override fun getItemCount(): Int = eventos.size
}
