package com.example.proyectofinal.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.example.proyectofinal.R
import com.example.proyectofinal.models.DetalleVenta
import com.example.proyectofinal.models.Venta
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketPrinter(private val context: Context) {

    fun imprimir(venta: Venta, detalles: List<DetalleVenta>, tipoTicket: String, macAddress: String?) {
        if (macAddress.isNullOrEmpty()) {
            Toast.makeText(context, "No hay impresora configurada", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permiso Bluetooth requerido", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val device = bluetoothAdapter.getRemoteDevice(macAddress)
            val connection = BluetoothConnection(device)

            // 32 es el ancho de caracteres configurado
            val printer = EscPosPrinter(connection, 203, 48f, 32)

            val texto = when (tipoTicket) {
                "Cliente" -> generarTicketCliente(printer, venta, detalles)
                "Preparacion" -> generarTicketCocina(venta, detalles)
                "Reimpresion" -> generarTicketCliente(printer, venta, detalles)
                else -> ""
            }

            printer.printFormattedText(texto)

        } catch (e: Exception) {
            Log.e("TicketPrinter", "Error al imprimir en $macAddress", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 150
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 150
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun generarTicketCliente(
        printer: EscPosPrinter,
        venta: Venta,
        detalles: List<DetalleVenta>
    ): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Date()
        val total = detalles.sumOf { it.precio_unidad }

        // El ancho configurado en EscPosPrinter es 32
        val maxAnchoLinea = 32

        var ticket = ""

        // --- LOGO ---
        try {
            val logoBitmap = getBitmapFromVectorDrawable(context, R.mipmap.ic_launcher)
            if (logoBitmap != null) {
                val logoHex = PrinterTextParserImg.bitmapToHexadecimalString(printer, logoBitmap)
                ticket += "[C]<img>$logoHex</img>\n"
            }
        } catch (e: Exception) {}

        // --- ENCABEZADO ---
        ticket += "[C]<u><font size='big'>RESTAURANTE</font></u>\n" +
                "[C]POR QUE MERECES COMER BIEN\n" +
                "[C]--------------------------------\n" +
                "[C]CALLE EJEMPLO #123, COL. CENTRO\n" + // CAMBIAR DIRECCION
                "[C]--------------------------------\n" +
                "[C]<b>TEL: 55-0000-0000</b>\n" +       // CAMBIAR TELEFONO
                "[C]--------------------------------\n" +
                "[L]<b>FECHA: ${dateFormat.format(now)}</b>\n" +
                "[L]<b>HORA: ${timeFormat.format(now)}</b>\n" +
                "[C]--------------------------------\n"

        // --- PRODUCTOS ---
        detalles.forEach {
            val nombre = it.producto?.nombre ?: "Articulo"
            val precio = format.format(it.precio_unidad)
            val textoProducto = "${it.cantidad}x $nombre"

            // Calculamos si texto + precio exceden el ancho (dejando 1 espacio de margen)
            if ((textoProducto.length + precio.length + 1) > maxAnchoLinea) {
                // Si es muy largo, bajamos el precio a la siguiente linea alineado a la derecha
                ticket += "[L]$textoProducto\n[R]$precio\n"
            } else {
                // Si cabe, mantenemos el formato original en una sola linea
                ticket += "[L]$textoProducto[R]$precio\n"
            }
        }

        // --- TOTALES Y PIE DE PAGINA ---
        ticket += "[C]--------------------------------\n" +
                "[R]TOTAL: <font size='big'>${format.format(total)}</font>\n" +
                "[C]--------------------------------\n" +
                "[C]***TICKET NECESARIO PARA LA ENTREGA***\n" +
                "[C]--------------------------------\n" +
                "[C]SIGUENOS EN FACEBOOK:\n" +
                "[C]PAGINA DE FACEBOOK\n" +
                "[L]\n" +
                "[L]\n" +
                "[C]AGRADECEMOS SU PROPINA\n" +
                "[L]\n[L]\n"

        return ticket
    }

    private fun generarTicketCocina(venta: Venta, detalles: List<DetalleVenta>): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Date()

        var ticket = "[C]<b><font size='big'>RESTAURANTE</font></b>\n" +
                "[L]<b>Mesa:</b> ${venta.identificador}\n" +
                "[L]<b>Fecha:</b> ${dateFormat.format(now)}\n" +
                "[L]<b>Hora:</b> ${timeFormat.format(now)}\n" +
                "[L]<b>Zona:</b> ${venta.tipo_pedido}\n" +
                "[C]--------------------------------\n" +
                "[C]--------------------------------\n"

        detalles.forEach {
            val nombre = it.producto?.nombre ?: "Articulo"

            ticket += "[L][] <b>${it.cantidad}</b> $nombre\n"

            if (!it.notas.isNullOrBlank()) {
                ticket += "[L]   (Nota: ${it.notas})\n"
            }
            ticket += "[C]--------------------------------\n"
        }

        ticket += "[L]\n[L]\n[L]\n"
        return ticket
    }
}