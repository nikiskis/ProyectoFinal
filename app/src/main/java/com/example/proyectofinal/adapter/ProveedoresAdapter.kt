// 2. Modificar: app/src/main/java/com/example/proyectofinal/adapter/ProveedoresAdapter.kt
package com.example.proyectofinal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.Proveedor

class ProveedoresAdapter(
    private var lista: MutableList<Proveedor>,
    private val onEdit: (Proveedor) -> Unit,
    private val onDelete: (Proveedor) -> Unit,
    private val onLink: (Proveedor) -> Unit,
    private val onCall: (String) -> Unit // Nuevo callback para llamar
) : RecyclerView.Adapter<ProveedoresAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvNombre: TextView = v.findViewById(R.id.tvNombreProv)
        val tvEmpresa: TextView = v.findViewById(R.id.tvEmpresaProv)
        val tvTel: TextView = v.findViewById(R.id.tvTelProv)
        val tvIngredientes: TextView = v.findViewById(R.id.tvIngredientesLista)
        val btnEdit: ImageButton = v.findViewById(R.id.btnEditProv)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDeleteProv)
        val btnLink: ImageButton = v.findViewById(R.id.btnLinkIngrediente)
        val btnCall: ImageButton = v.findViewById(R.id.btnCallProv) // Nuevo botón
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_proveedor, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = lista[position]

        holder.tvNombre.text = p.nombre
        holder.tvEmpresa.text = p.empresa ?: ""
        holder.tvTel.text = "Tel: ${p.telefono}"

        val nombresIngredientes = p.relaciones?.map { it.ingrediente.nombre } ?: emptyList()

        if (nombresIngredientes.isNotEmpty()) {
            holder.tvIngredientes.text = "Surte: ${nombresIngredientes.joinToString(", ")}"
        } else {
            holder.tvIngredientes.text = "Surte: Ninguno"
        }

        holder.btnEdit.setOnClickListener { onEdit(p) }
        holder.btnDelete.setOnClickListener { onDelete(p) }
        holder.btnLink.setOnClickListener { onLink(p) }

        holder.btnCall.setOnClickListener {
            if (p.telefono.isNotBlank()) {
                onCall(p.telefono)
            }
        }
    }

    override fun getItemCount() = lista.size

    fun updateData(nueva: List<Proveedor>) {
        lista.clear()
        lista.addAll(nueva)
        notifyDataSetChanged()
    }
}