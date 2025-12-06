package com.example.proyectofinal.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.Venta
import com.google.android.material.card.MaterialCardView

class PedidosAdapter(
    private var pedidos: MutableList<Venta>,
    private val onPedidoClick: (Venta) -> Unit,
    private val onPrintClick: (Venta) -> Unit // Nuevo parámetro
) : RecyclerView.Adapter<PedidosAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardPedido)
        val tvTipo: TextView = itemView.findViewById(R.id.tvTipoPedido)
        val tvId: TextView = itemView.findViewById(R.id.tvIdentificador)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoPedido)
        val btnPrint: ImageButton = itemView.findViewById(R.id.btnPrintPedido)

        init {
            itemView.setOnClickListener { onPedidoClick(pedidos[bindingAdapterPosition]) }
            btnPrint.setOnClickListener { onPrintClick(pedidos[bindingAdapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = pedidos[position]
        holder.tvTipo.text = item.tipo_pedido ?: "Pedido"
        holder.tvId.text = item.identificador ?: "#"
        holder.tvEstado.text = item.estado?.estado ?: "Activo"

        val colorBorde = when (item.tipo_pedido) {
            "Mesa" -> Color.parseColor("#2196F3")
            "Para Llevar" -> Color.parseColor("#FF9800")
            "Terraza" -> Color.parseColor("#4CAF50")
            "Domicilio" -> Color.parseColor("#F44336")
            else -> Color.parseColor("#9E9E9E")
        }

        holder.cardView.strokeColor = colorBorde
        holder.cardView.setCardBackgroundColor(Color.WHITE)
    }

    override fun getItemCount() = pedidos.size

    fun updateData(newItems: List<Venta>) {
        pedidos.clear()
        pedidos.addAll(newItems)
        notifyDataSetChanged()
    }
}