package com.example.organizadoreventos.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.databinding.ItemEventoBinding

class EventosAdapter(private var eventos: List<Evento>) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    inner class EventoViewHolder(val binding: ItemEventoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]

        // Setear los valores de cada evento
        holder.binding.tvFecha.text = evento.fecha
        holder.binding.tvHora.text = evento.hora
        holder.binding.tvCategoria.text = evento.categoria
        holder.binding.tvStatus.text = evento.status
        holder.binding.tvDescripcion.text = evento.descripcion
        holder.binding.tvContacto.text = evento.contacto
    }

    fun updateEventos(newEvents: List<Evento>) {
        this.eventos = newEvents
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }

    override fun getItemCount(): Int = eventos.size
}
