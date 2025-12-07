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
    private val onPrintClick: (Venta) -> Unit
) : RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIdentificador: TextView = view.findViewById(R.id.tvIdentificador)
        val tvTipoPedido: TextView = view.findViewById(R.id.tvTipoPedido)
        val tvEstadoPedido: TextView = view.findViewById(R.id.tvEstadoPedido)
        val btnPrint: ImageButton = view.findViewById(R.id.btnPrintPedido)
        val cardView: MaterialCardView = view.findViewById(R.id.cardPedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]

        holder.tvIdentificador.text = pedido.identificador
        holder.tvTipoPedido.text = pedido.tipo_pedido

        val colorFondo: Int = when {
            pedido.impreso_cocina -> Color.parseColor("#C8E6C9")

            !pedido.detalles.isNullOrEmpty() -> Color.parseColor("#FFF9C4")

            else -> Color.WHITE
        }

        val colorBorde: Int = when (pedido.tipo_pedido) {
            "Mesa" -> Color.parseColor("#2196F3")        // Azul
            "Para Llevar" -> Color.parseColor("#FF9800") // Naranja
            "Domicilio" -> Color.parseColor("#F44336")   // Rojo
            "Terraza" -> Color.parseColor("#4CAF50")     // Verde
            else -> Color.parseColor("#9E9E9E")          // Gris
        }

        if (pedido.impreso_cocina) {
            holder.tvEstadoPedido.text = "En Cocina"
            holder.tvEstadoPedido.setBackgroundResource(R.drawable.status_background)
        } else {
            holder.tvEstadoPedido.text = "Pendiente"
            holder.tvEstadoPedido.setBackgroundColor(Color.TRANSPARENT)
            holder.tvEstadoPedido.setTextColor(Color.DKGRAY)
        }

        holder.cardView.setCardBackgroundColor(colorFondo)
        holder.cardView.strokeColor = colorBorde
        holder.cardView.strokeWidth = 6

        holder.itemView.setOnClickListener { onPedidoClick(pedido) }
        holder.btnPrint.setOnClickListener { onPrintClick(pedido) }
    }

    override fun getItemCount(): Int = pedidos.size

    fun updateData(newPedidos: List<Venta>) {
        pedidos.clear()
        pedidos.addAll(newPedidos)
        notifyDataSetChanged()
    }
}