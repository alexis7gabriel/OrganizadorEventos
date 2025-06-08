package com.example.organizadoreventos.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventos.R
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.databinding.ItemEventoExpandibleBinding // Asegúrate de que este binding sea el correcto
import java.util.Locale

class EventosAdapter(private var eventos: List<Evento>) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    // Interfaz para comunicar eventos de clics al Fragmento/Activity que usa el adaptador
    interface OnItemClickListener {
        fun onItemClick(evento: Evento) // Clic en el ítem completo
        fun onMapButtonClick(ubicacion: String) // Clic en el botón del mapa
    }

    private var listener: OnItemClickListener? = null

    // Método para establecer el listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    // Método para actualizar la lista de eventos y notificar al RecyclerView
    fun updateEventos(newEvents: List<Evento>) {
        this.eventos = newEvents
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        // Infla el layout del ítem usando View Binding
        val binding = ItemEventoExpandibleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.bind(evento)
    }

    override fun getItemCount(): Int = eventos.size

    inner class EventoViewHolder(private val binding: ItemEventoExpandibleBinding) : RecyclerView.ViewHolder(binding.root) {
        // Estado local para cada ítem: si está expandido o no
        private var isExpanded = false

        init {
            // Configura el listener para el clic en todo el CardView (collapsed_section)
            // Esto permite expandir/contraer al tocar el área principal del ítem
            binding.collapsedSection.setOnClickListener {
                isExpanded = !isExpanded // Cambia el estado de expansión
                toggleExpandedState() // Actualiza la visibilidad de la sección y el icono
                // Usar absoluteAdapterPosition para obtener la posición de forma más robusta
                listener?.onItemClick(eventos[absoluteAdapterPosition]) // Notifica al fragmento del clic en el ítem
            }

            // Configura el listener para el clic en el botón/icono del mapa
            binding.btnOpenMap.setOnClickListener {
                // Obtiene la ubicación del TextView y la limpia de "Ubicación: "
                val ubicacionText = binding.tvUbicacion.text.toString()
                val ubicacionValue = ubicacionText.replace("Ubicación: ", "")
                listener?.onMapButtonClick(ubicacionValue) // Notifica al fragmento del clic en el mapa
            }
        }

        fun bind(evento: Evento) {
            // Sección Visible (Resumen)
            binding.tvFecha.text = evento.fecha
            binding.tvCategoria.text = evento.categoria
            binding.tvDescripcion.text = evento.descripcion

            // Sección Expandible (Detalles Adicionales)
            binding.tvHora.text = itemView.context.getString(R.string.event_detail_time, evento.hora)
            binding.tvStatus.text = itemView.context.getString(R.string.event_detail_status, evento.status)
            binding.tvUbicacion.text = itemView.context.getString(R.string.event_detail_location, evento.ubicacion)
            binding.tvContacto.text = itemView.context.getString(R.string.event_detail_contact, evento.contacto)
            binding.tvRecordatorio.text = itemView.context.getString(R.string.event_detail_reminder, evento.recordatorio)

            // Asignar color de fondo a la categoría (usando el drawable genérico)
            //binding.tvCategoria.setBackgroundResource(R.drawable.rounded_category_background)

            // Cambiar el color de fondo del Status según el valor
         /*   when (evento.status.toLowerCase(Locale.getDefault())) {
                "pendiente" -> binding.tvStatus.setBackgroundResource(R.drawable.rounded_status_pending_background)
                "realizado" -> binding.tvStatus.setBackgroundResource(R.drawable.rounded_status_completed_background)
                "aplazado" -> binding.tvStatus.setBackgroundResource(R.drawable.rounded_status_postponed_background)
                else -> binding.tvStatus.setBackgroundResource(R.drawable.rounded_status_default_background)
            }*/

            // Asegura que la sección expandible y la flecha estén en el estado inicial correcto
            toggleExpandedState()
        }

        // Método para alternar la visibilidad de la sección expandible y el icono de la flecha
        private fun toggleExpandedState() {
            binding.expandableSection.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivExpandArrow.setImageResource(if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more)
        }
    }
}
