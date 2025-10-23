package com.example.proyectofinal.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.Articulo
import java.text.NumberFormat
import java.util.Locale

class ArticulosAdapter(
    private var articulos: MutableList<Articulo>,
    private val onEditClick: (Articulo) -> Unit,
    private val onDeleteClick: (Articulo) -> Unit,
    private val onIngredientsClick: (Articulo) -> Unit
) : RecyclerView.Adapter<ArticulosAdapter.ArticuloViewHolder>() {

    inner class ArticuloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.articuloNameTextView)
        val costoTextView: TextView = itemView.findViewById(R.id.costoTextView)
        val precioTextView: TextView = itemView.findViewById(R.id.precioTextView)
        val precioSugeridoTextView: TextView = itemView.findViewById(R.id.precioSugeridoTextView)
        val ingredientsButton: ImageButton = itemView.findViewById(R.id.ingredientsButton)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticuloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_articulo, parent, false)
        return ArticuloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticuloViewHolder, position: Int) {
        val articulo = articulos[position]

        holder.nameTextView.text = articulo.nombre

        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        holder.precioTextView.text = "Precio: ${currencyFormat.format(articulo.precio)}"

        val costoTotal = articulo.producto_ingrediente.sumOf {
            it.ingrediente.costo * it.cantidad
        }
        holder.costoTextView.text = "Costo: ${currencyFormat.format(costoTotal)}"

        val precioSugerido = costoTotal * 3.0
        holder.precioSugeridoTextView.text = "Sugerido: ${currencyFormat.format(precioSugerido)}"

        holder.ingredientsButton.setOnClickListener {
            onIngredientsClick(articulo)
        }
        holder.editButton.setOnClickListener {
            onEditClick(articulo)
        }
        holder.deleteButton.setOnClickListener {
            onDeleteClick(articulo)
        }
    }

    override fun getItemCount(): Int = articulos.size

    fun updateData(newArticulos: List<Articulo>) {
        articulos.clear()
        articulos.addAll(newArticulos)
        notifyDataSetChanged()
    }
}