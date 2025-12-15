package com.example.proyectofinal.adapter
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.Ingrediente
import java.text.NumberFormat
import java.util.Locale

class IngredientesAdapter(
    private var ingredientes: MutableList<Ingrediente>,
    private val onEditClick: (Ingrediente) -> Unit,
    private val onDeleteClick: (Ingrediente) -> Unit
) : RecyclerView.Adapter<IngredientesAdapter.IngredienteViewHolder>() {

    inner class IngredienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.ingredienteNameTextView)
        val estadoTextView: TextView = itemView.findViewById(R.id.estadoTextView)
        val costoTextView: TextView = itemView.findViewById(R.id.costoTextView)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        val stockActualTextView: TextView = itemView.findViewById(R.id.stockActualTextView)
        val stockMinimoTextView: TextView = itemView.findViewById(R.id.stockMinimoTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredientes, parent, false)
        return IngredienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredienteViewHolder, position: Int) {
        val ingrediente = ingredientes[position]

        holder.nameTextView.text = ingrediente.nombre
        holder.estadoTextView.text = ingrediente.estado.estado
        holder.stockActualTextView.text = "Stock: ${ingrediente.stock_actual}"
        holder.stockMinimoTextView.text = "Mín: ${ingrediente.stock_minimo}"

        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        holder.costoTextView.text = "Costo: ${format.format(ingrediente.costo)}"

        if (ingrediente.estado.estado.equals("Activo", ignoreCase = true)) {
            holder.estadoTextView.setBackgroundColor(Color.parseColor("#4CAF50"))
        } else {
            holder.estadoTextView.setBackgroundColor(Color.parseColor("#F44336"))
        }

        holder.editButton.setOnClickListener { onEditClick(ingrediente) }
        holder.deleteButton.setOnClickListener { onDeleteClick(ingrediente) }

        holder.editButton.setOnClickListener {
            Log.d("InggredienteDebug", "Clic en Editar para: ${ingrediente.nombre}")
            onEditClick(ingrediente)
        }
    }

    override fun getItemCount(): Int = ingredientes.size

    fun updateData(newIngredientes: List<Ingrediente>) {
        ingredientes.clear()
        ingredientes.addAll(newIngredientes)
        notifyDataSetChanged()
    }
}