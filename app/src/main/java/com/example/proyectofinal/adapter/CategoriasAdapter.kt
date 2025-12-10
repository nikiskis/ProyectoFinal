package com.example.proyectofinal.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.repositories.CategoriasRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Categoria(val id: Int, var nombre: String)


class CategoriasAdapter(
    private var categorias: MutableList<Categoria>,
    private val context: Context
) : RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder>() {

    private val categoriasRepo = CategoriasRepository()

    inner class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.categoriaNameTextView)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.nameTextView.text = categoria.nombre

        holder.editButton.setOnClickListener {
            showEditDialog(categoria, position)
        }

        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(categoria, position)
        }
    }

    override fun getItemCount(): Int = categorias.size

    private fun showEditDialog(categoria: Categoria, position: Int) {
        val editText = EditText(context).apply {
            setText(categoria.nombre)
        }

        AlertDialog.Builder(context)
            .setTitle("Editar Categoría")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editText.text.toString()
                if (nuevoNombre.isNotBlank() && nuevoNombre != categoria.nombre) {
                    CoroutineScope(Dispatchers.Main).launch {
                        categoriasRepo.updateCategoria(categoria.id, nuevoNombre)
                        categoria.nombre = nuevoNombre
                        notifyItemChanged(position)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(categoria: Categoria, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar Categoría")
            .setMessage("¿Estás seguro de que quieres eliminar '${categoria.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    categoriasRepo.deleteCategoria(categoria.id)
                    categorias.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun updateData(newCategorias: List<Categoria>) {
        categorias.clear()
        categorias.addAll(newCategorias)
        notifyDataSetChanged()
    }
}