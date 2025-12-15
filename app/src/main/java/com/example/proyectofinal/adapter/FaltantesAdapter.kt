package com.example.proyectofinal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.Faltante

class FaltantesAdapter(
    private var lista: MutableList<Faltante>,
    private val onDelete: (Faltante) -> Unit
) : RecyclerView.Adapter<FaltantesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreFaltante)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidadFaltante)
        val btnBorrar: ImageButton = view.findViewById(R.id.btnBorrarFaltante)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faltante, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre
        holder.tvCantidad.text = item.cantidad
        holder.btnBorrar.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = lista.size

    fun updateData(nuevaLista: List<Faltante>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}