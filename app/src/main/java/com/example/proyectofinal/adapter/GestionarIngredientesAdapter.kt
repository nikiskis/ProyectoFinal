package com.example.proyectofinal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.ArticuloIngrediente

class GestionarIngredientesAdapter(
    private var ingredientesRelacion: MutableList<ArticuloIngrediente>,
    private val onDeleteClick: (ArticuloIngrediente) -> Unit
) : RecyclerView.Adapter<GestionarIngredientesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.tvNombreIngrediente)
        val cantidadTextView: TextView = itemView.findViewById(R.id.tvCantidadIngrediente)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnEliminarIngredienteArticulo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_articulo_ingrediente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val relacion = ingredientesRelacion[position]
        holder.nombreTextView.text = relacion.ingrediente.nombre
        holder.cantidadTextView.text = "Cant: ${relacion.cantidad}"

        holder.deleteButton.setOnClickListener {
            onDeleteClick(relacion)
        }
    }

    override fun getItemCount(): Int = ingredientesRelacion.size

    fun updateData(newRelaciones: List<ArticuloIngrediente>) {
        ingredientesRelacion.clear()
        ingredientesRelacion.addAll(newRelaciones)
        notifyDataSetChanged()
    }
}