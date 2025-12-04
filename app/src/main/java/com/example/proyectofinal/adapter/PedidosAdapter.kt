package com.example.proyectofinal.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.Venta

class PedidosAdapter(
    private var pedidos: MutableList<Venta>,
    private val onPedidoClick: (Venta) -> Unit
) : RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder>() {

    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardPedido)
        val tvTipo: TextView = itemView.findViewById(R.id.tvTipoPedido)
        val tvIdentificador: TextView = itemView.findViewById(R.id.tvIdentificador)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoPedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]

        holder.tvTipo.text = pedido.tipo_pedido
        holder.tvIdentificador.text = pedido.identificador ?: "#"
        holder.tvEstado.text = pedido.estado?.estado ?: "Pendiente"

        val colorFondo = when (pedido.tipo_pedido) {
            "Mesa" -> "#E3F2FD"
            "Para Llevar" -> "#FFF3E0"
            "Terraza" -> "#E8F5E9"
            "Domicilio" -> "#FCE4EC"
            else -> "#FFFFFF"
        }
        holder.cardView.setCardBackgroundColor(Color.parseColor(colorFondo))

        holder.itemView.setOnClickListener {
            onPedidoClick(pedido)
        }
    }

    override fun getItemCount(): Int = pedidos.size

    fun updateData(newPedidos: List<Venta>) {
        pedidos.clear()
        pedidos.addAll(newPedidos)
        notifyDataSetChanged()
    }
}