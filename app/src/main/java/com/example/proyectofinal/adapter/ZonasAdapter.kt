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
import com.example.proyectofinal.repositories.ZonasRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
@Serializable
data class Zona(val id: Int, var nombre: String)


class ZonasAdapter(
    private var zonas: MutableList<Zona>,
    private val context: Context
) : RecyclerView.Adapter<ZonasAdapter.ZonaViewHolder>() {

    private val zonasRepo = ZonasRepository()
    inner class ZonaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.zonaNameTextView)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZonaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_zona, parent, false)
        return ZonaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZonaViewHolder, position: Int) {
        val zona = zonas[position]
        holder.nameTextView.text = zona.nombre

        holder.editButton.setOnClickListener {
            showEditDialog(zona, position)
        }

        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(zona, position)
        }
    }

    override fun getItemCount(): Int = zonas.size

    private fun showEditDialog(zona: Zona, position: Int) {
        val editText = EditText(context).apply {
            setText(zona.nombre)
        }

        AlertDialog.Builder(context)
            .setTitle("Editar Zona")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editText.text.toString()
                if (nuevoNombre.isNotBlank() && nuevoNombre != zona.nombre) {
                    CoroutineScope(Dispatchers.Main).launch {
                        zonasRepo.updateZona(zona.id, nuevoNombre)
                        zona.nombre = nuevoNombre
                        notifyItemChanged(position)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(zona: Zona, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar Zona")
            .setMessage("¿Estás seguro de que quieres eliminar '${zona.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    zonasRepo.deleteZona(zona.id)
                    zonas.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun updateData(newZonas: List<Zona>) {
        zonas.clear()
        zonas.addAll(newZonas)
        notifyDataSetChanged()
    }
}