package com.example.proyectofinal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.Articulo
import java.text.NumberFormat
import java.util.Locale

class ProductoMenuAdapter(
    private var productos: List<Articulo>,
    private val onProductoClick: (Articulo) -> Unit
) : RecyclerView.Adapter<ProductoMenuAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreGrid)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecioGrid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prod = productos[position]
        holder.tvNombre.text = prod.nombre

        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        holder.tvPrecio.text = format.format(prod.precio)

        holder.itemView.setOnClickListener { onProductoClick(prod) }
    }

    override fun getItemCount() = productos.size

    fun updateData(newProds: List<Articulo>) {
        productos = newProds
        notifyDataSetChanged()
    }
}