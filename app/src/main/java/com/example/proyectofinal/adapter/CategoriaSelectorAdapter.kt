package com.example.proyectofinal.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.CategoriaProducto

class CategoriaSelectorAdapter(
    private var categorias: List<CategoriaProducto>,
    private val onCategoriaClick: (CategoriaProducto) -> Unit
) : RecyclerView.Adapter<CategoriaSelectorAdapter.ViewHolder>() {

    private var selectedPosition = 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = categorias[position]
        holder.tvNombre.text = cat.nombre
        holder.tvNombre.setPadding(30, 20, 30, 20)

        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#488079"))
            holder.tvNombre.setTextColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundColor(Color.LTGRAY)
            holder.tvNombre.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(prev)
            notifyItemChanged(selectedPosition)
            onCategoriaClick(cat)
        }
    }

    override fun getItemCount() = categorias.size

    fun updateData(newCats: List<CategoriaProducto>) {
        categorias = newCats
        notifyDataSetChanged()
    }
}