package com.example.proyectofinal.adapter

import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R
import com.example.proyectofinal.models.Venta
import java.text.NumberFormat
import java.util.Locale

class ReporteAdapter(
    private var ventas: List<Venta>,
    private val tipoLista: String,
    private val zonasSeleccionadas: List<Int> = emptyList()
) : RecyclerView.Adapter<ReporteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInfo: TextView = view.findViewById(android.R.id.text1)
        val tvMonto: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val venta = ventas[position]
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        holder.tvMonto.setTextColor(Color.DKGRAY)

        when (tipoLista) {
            "DESCUENTO" -> {

                holder.tvInfo.text = "Ticket #${venta.identificador} (${venta.tipo_pedido})"
                holder.tvMonto.text = "Desc: -${format.format(venta.descuento_monto ?: 0.0)}"
                holder.tvMonto.setTextColor(Color.parseColor("#FF9800"))
            }
            "CANCELADO" -> {

                holder.tvInfo.text = "Ticket #${venta.identificador} (${venta.tipo_pedido}) - CANCELADO"


                val sb = StringBuilder()
                var totalEstimado = 0.0
                venta.detalles?.forEach { det ->
                    totalEstimado += det.precio_unidad * det.cantidad
                    sb.append("• ${det.cantidad}x ${det.producto?.nombre}<br>")
                }

                val textoFinal = "<b>${format.format(totalEstimado)}</b><br><small>$sb</small>"

                holder.tvMonto.text = Html.fromHtml(textoFinal, Html.FROM_HTML_MODE_LEGACY)
                holder.tvMonto.setTextColor(Color.RED)
            }
            else -> {

                val isCancelado = venta.id_estado == 4
                val hasDescuento = (venta.descuento_monto ?: 0.0) > 0

                val mostrarTodo = isCancelado || hasDescuento || zonasSeleccionadas.isEmpty()

                val textoEstado = if (isCancelado) " <font color='red'>(CANCELADO)</font>" else ""
                val titulo = "${venta.fecha.take(16)} | #${venta.identificador} (${venta.tipo_pedido})$textoEstado"
                holder.tvInfo.text = Html.fromHtml(titulo, Html.FROM_HTML_MODE_LEGACY)

                val sb = StringBuilder()
                var subtotalCalculado = 0.0

                venta.detalles?.forEach { det ->
                    val zonaProd = det.producto?.id_zona_produccion ?: 0
                    if (mostrarTodo || zonasSeleccionadas.contains(zonaProd)) {
                        val totalProd = det.cantidad * det.precio_unidad
                        subtotalCalculado += totalProd
                        sb.append("• ${det.cantidad}x ${det.producto?.nombre ?: "Art."} (${format.format(totalProd)})<br>")
                    }
                }

                val totalAMostrar = if (mostrarTodo) (venta.total_final ?: 0.0) else subtotalCalculado
                val descuento = venta.descuento_monto ?: 0.0
                val colorTotal = if (isCancelado) "#F44336" else "#2E7D32"

                sb.append("<br>")

                if (hasDescuento && mostrarTodo) {
                    sb.append("Subtotal: ${format.format(subtotalCalculado)}<br>")
                    sb.append("<b>Descuento: <font color='#FF9800'>-${format.format(descuento)}</font></b><br>")
                }

                sb.append("<b>Total: <font color='$colorTotal'>${format.format(totalAMostrar)}</font></b>")

                holder.tvMonto.text = Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
            }
        }

        holder.itemView.findViewById<View>(R.id.btnEditNota)?.visibility = View.GONE
    }

    override fun getItemCount(): Int = ventas.size

    fun updateData(newDetails: List<Venta>) {
        ventas = newDetails
        notifyDataSetChanged()
    }
}