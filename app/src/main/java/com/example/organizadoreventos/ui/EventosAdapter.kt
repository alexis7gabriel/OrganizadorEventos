package com.example.organizadoreventos.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventos.R
import com.example.organizadoreventos.data.entities.Evento
import com.example.organizadoreventos.databinding.ItemEventoExpandibleBinding

class EventosAdapter(private var eventos: List<Evento>) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    // Interfaz para comunicar eventos de clics al Fragmento/Activity que usa el adaptador
    interface OnItemClickListener {
        fun onItemClick(evento: Evento)
        fun onMapButtonClick(ubicacion: String)
        fun onEditButtonClick(evento: Evento)
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
        val binding = ItemEventoExpandibleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.bind(evento)
    }

    override fun getItemCount(): Int = eventos.size

    inner class EventoViewHolder(private val binding: ItemEventoExpandibleBinding) : RecyclerView.ViewHolder(binding.root) {
        private var isExpanded = false

        init {
            binding.collapsedSection.setOnClickListener {
                isExpanded = !isExpanded // Cambia el estado de expansión
                toggleExpandedState() // Actualiza la visibilidad de la sección y el icono
                listener?.onItemClick(eventos[absoluteAdapterPosition]) // Notifica al fragmento del clic en el ítem
            }

            // Configura el listener para el clic en el botón/icono del mapa
            binding.btnOpenMap.setOnClickListener {
                // Obtiene la ubicación del TextView y la limpia de "Ubicación: "
                val ubicacionText = binding.tvUbicacion.text.toString()
                val locationPrefix = itemView.context.getString(R.string.event_detail_location, "")
                    .replace("%s", "").trim()
                val ubicacionValue = ubicacionText.replace(locationPrefix, "").trim()
                listener?.onMapButtonClick(ubicacionValue) // Notifica al fragmento del clic en el mapa
            }

            binding.btnEditEvent.setOnClickListener {
                listener?.onEditButtonClick(eventos[absoluteAdapterPosition]) // Notifica al fragmento el clic en el botón de edición
            }
        }

        fun bind(evento: Evento) {
            binding.tvFecha.text = evento.fecha
            binding.tvCategoria.text = evento.categoria
            binding.tvDescripcion.text = evento.descripcion

            binding.tvHora.text = itemView.context.getString(R.string.event_detail_time, evento.hora)
            binding.tvStatus.text = itemView.context.getString(R.string.event_detail_status, evento.status)
            binding.tvUbicacion.text = itemView.context.getString(R.string.event_detail_location, evento.ubicacion)
            binding.tvContacto.text = itemView.context.getString(R.string.event_detail_contact, evento.contacto)
            binding.tvRecordatorio.text = itemView.context.getString(R.string.event_detail_reminder, evento.recordatorio)

            toggleExpandedState()
        }

        // Método para alternar la visibilidad de la sección expandible y el icono de la flecha
        private fun toggleExpandedState() {
            binding.expandableSection.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivExpandArrow.setImageResource(if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more)

            // Controla las líneas de la descripción para la expansión
            if (isExpanded) {
                binding.tvDescripcion.maxLines = Integer.MAX_VALUE
                binding.tvDescripcion.ellipsize = null
            } else {
                binding.tvDescripcion.maxLines = 2
                binding.tvDescripcion.ellipsize = android.text.TextUtils.TruncateAt.END
            }
        }
    }
}
