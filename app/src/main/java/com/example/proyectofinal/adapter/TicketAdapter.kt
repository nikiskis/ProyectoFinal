package com.example.proyectofinal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.DetalleVenta
import java.text.NumberFormat
import java.util.Locale

class TicketAdapter(
    private var detalles: MutableList<DetalleVenta>,
    private val onEditNotaClick: (DetalleVenta) -> Unit
) : RecyclerView.Adapter<TicketAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(android.R.id.text1)
        val tvPrecio: TextView = itemView.findViewById(android.R.id.text2)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditNota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = detalles[position]
        val nombre = item.producto?.nombre ?: "Articulo"
        val notas = if (item.notas.isNullOrBlank()) "" else " (${item.notas})"

        // CAMBIO 1: Agregamos la cantidad al inicio (Ej: "3x Hamburguesa")
        holder.tvNombre.text = "${item.cantidad}x $nombre$notas"

        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        // CAMBIO 2: Multiplicamos por la cantidad para mostrar el total de esa línea
        val totalLinea = item.precio_unidad * item.cantidad
        holder.tvPrecio.text = format.format(totalLinea)

        holder.btnEdit.setOnClickListener { onEditNotaClick(item) }
    }

    override fun getItemCount() = detalles.size

    fun updateData(newItems: List<DetalleVenta>) {
        detalles.clear()
        detalles.addAll(newItems)
        notifyDataSetChanged()
    }
}