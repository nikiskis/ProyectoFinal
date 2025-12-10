package com.example.proyectofinal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.Articulo
import com.example.proyectofinal.models.CategoriaExpandible

class AdminExpandibleAdapter(
    private var listaCategorias: List<CategoriaExpandible>,
    private val onEditClick: (Articulo) -> Unit,
    private val onDeleteClick: (Articulo) -> Unit,
    private val onIngredientsClick: (Articulo) -> Unit
) : RecyclerView.Adapter<AdminExpandibleAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreCategoriaHeader)
        val rvInterno: RecyclerView = itemView.findViewById(R.id.rvProductosInterno)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria_expandible, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaCategorias[position]

        holder.tvNombre.text = item.categoria.nombre
        holder.rvInterno.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

        val adapterInterno = ArticulosAdapter(
            item.productos.toMutableList(),
            onEditClick,
            onDeleteClick,
            onIngredientsClick
        )

        holder.rvInterno.adapter = adapterInterno
        holder.rvInterno.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvInterno.isNestedScrollingEnabled = false

        holder.tvNombre.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = listaCategorias.size

    fun updateData(nuevaLista: List<CategoriaExpandible>) {
        listaCategorias = nuevaLista
        notifyDataSetChanged()
    }
}