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

    fun imprimir(venta: Venta, detalles: List<DetalleVenta>, tipoTicket: String, macAddress: String?, esReimpresion: Boolean = false) {
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

            val printer = EscPosPrinter(connection, 203, 48f, 32)

            val texto = when (tipoTicket) {
                "Cliente" -> generarTicketCliente(printer, venta, detalles)
                "Preparacion" -> generarTicketCocina(venta, detalles, esReimpresion)
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

        val subtotal = detalles.sumOf { it.precio_unidad * it.cantidad }
        val descuento = venta.descuento_monto ?: 0.0
        val totalFinal = subtotal - descuento

        val maxAnchoLinea = 32
        var ticket = ""

        // LOGO
        try {
            val logoBitmap = getBitmapFromVectorDrawable(context, R.drawable.mundogrilldos)
            if (logoBitmap != null) {
                val logoHex = PrinterTextParserImg.bitmapToHexadecimalString(printer, logoBitmap)
                ticket += "[C]<img>$logoHex</img>\n"
            }
        } catch (e: Exception) {}

        // ENCABEZADO
        ticket += "[C]<u><font size='big'>MUNDO GRILL</font></u>\n" +
                "[C]POR QUE MERECES COMER BIEN\n" +
                "[C]--------------------------------\n" +
                "[L]<b>FECHA: ${dateFormat.format(now)}</b>\n" +
                "[L]<b>HORA: ${timeFormat.format(now)}</b>\n"

        // DIRECCIÓN
        if (venta.tipo_pedido == "Domicilio" && !venta.direccion.isNullOrBlank()) {
            ticket += "[L]<b>ENTREGA:</b>\n" +
                    "[L]${venta.direccion}\n"
        }

        // CLIENTE
        ticket += "[L]NUM: ${venta.identificador}\n" +
                "[C]--------------------------------\n"

        // PRODUCTOS
        detalles.forEach {
            val nombre = it.producto?.nombre ?: "Articulo"
            val precio = format.format(it.precio_unidad * it.cantidad)
            val textoProducto = "${it.cantidad}x $nombre"

            if ((textoProducto.length + precio.length + 1) > maxAnchoLinea) {
                ticket += "[L]$textoProducto\n[R]$precio\n"
            } else {
                ticket += "[L]$textoProducto[R]$precio\n"
            }
        }

        // SECCIÓN DE TOTALES CON DESCUENTO
        ticket += "[C]--------------------------------\n"

        if (descuento > 0) {
            ticket += "[R]Subtotal: ${format.format(subtotal)}\n" +
                    "[R]Descuento: -${format.format(descuento)}\n"
        }

        ticket += "[R]TOTAL: <font size='big'>${format.format(totalFinal)}</font>\n" +
                "[C]--------------------------------\n" +
                "[C]TICKET NECESARIO PARA LA ENTREGA\n"+
                "[C]--------------------------------\n" +
                "[C]SIGUENOS EN FACEBOOK:\n" +
                "[C]MUNDO GRILL\n" +
                "[L]\n" +
                "[L]\n" +
                "[C]AGRADECEMOS SU PROPINA\n" +
                "[L]\n" +
                "[L]\n[L]\n"

        return ticket
    }

    private fun generarTicketCocina(venta: Venta, detalles: List<DetalleVenta>, esReimpresion: Boolean): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Date()

        var ticket = ""

        if (esReimpresion) {
            ticket += "[C]<b><font size='big'>REIMPRESION</font></b>\n"
        }


        ticket += "[C]<b><font size='big'>MUNDO GRILL</font></b>\n" +
                "[L]<b>Tipo:</b> ${venta.tipo_pedido}\n" +
                "[L]<b>Id:</b> ${venta.identificador}\n" +
                "[L]<b>Fecha:</b> ${dateFormat.format(now)}\n" +
                "[L]<b>Hora:</b> ${timeFormat.format(now)}\n"

        if (venta.tipo_pedido == "Domicilio" && !venta.direccion.isNullOrBlank()) {
            ticket += "[L]<b>DIR:</b> ${venta.direccion}\n"
        }

        ticket += "[C]--------------------------------\n" +
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


    fun imprimirCorteCaja(
        fechaInicio: String,
        fechaFin: String,
        fondoInicial: Double,
        ventasEfectivo: Double,
        ventasTarjeta: Double,
        costos: Double,
        ganancia: Double,
        macAddress: String?,
        zonasNombres: String
    ) {
        if (macAddress.isNullOrEmpty()) {
            Toast.makeText(context, "No hay impresora de Caja configurada", Toast.LENGTH_SHORT).show()
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
            val printer = EscPosPrinter(connection, 203, 48f, 32)

            val texto = generarTextoCorte(
                printer, fechaInicio, fechaFin, fondoInicial,
                ventasEfectivo, ventasTarjeta, costos, ganancia, zonasNombres
            )

            printer.printFormattedText(texto)

        } catch (e: Exception) {
            Log.e("TicketPrinter", "Error imprimiendo corte", e)
            Toast.makeText(context, "Error al imprimir: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generarTextoCorte(
        printer: EscPosPrinter,
        inicio: String,
        fin: String,
        fondo: Double,
        efectivo: Double,
        tarjeta: Double,
        costos: Double,
        ganancia: Double,
        zonas: String
    ): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        val now = Date()

        val totalCaja = fondo + efectivo
        val granTotal = efectivo + tarjeta

        var ticket = ""
        try {
            val logoBitmap = getBitmapFromVectorDrawable(context, R.drawable.mundogrilldos)
            if (logoBitmap != null) {
                val logoHex = PrinterTextParserImg.bitmapToHexadecimalString(printer, logoBitmap)
                ticket += "[C]<img>$logoHex</img>\n"
            }
        } catch (e: Exception) {}

        ticket += "[C]<u><font size='big'>CORTE DE CAJA</font></u>\n" +
                "[C]Impreso: ${dateFormat.format(now)}\n" +
                "[C]--------------------------------\n" +
                "[L]<b>Zonas:</b> $zonas\n" +
                "[L]<b>Desde:</b> $inicio\n" +
                "[L]<b>Hasta:</b> $fin\n" +
                "[C]--------------------------------\n" +
                "[C]<font size='big'>BALANCE</font>\n" +
                "[L]Fondo Inicial:[R]${format.format(fondo)}\n" +
                "[L]Ventas Efectivo (+):[R]${format.format(efectivo)}\n" +
                "[C]--------------------------------\n" +
                "[L]<b>TOTAL EN CAJON:</b>[R]<b>${format.format(totalCaja)}</b>\n" +
                "[C]================================\n" +
                "[L]Ventas Tarjeta:[R]${format.format(tarjeta)}\n" +
                "[L]<b>VENTA TOTAL:</b>[R]<b>${format.format(granTotal)}</b>\n" +
                "[C]--------------------------------\n" +
                "[L]Costos Prod:[R]-${format.format(costos)}\n" +
                "[L]<b>UTILIDAD:</b>[R]<b>${format.format(ganancia)}</b>\n" +
                "[C]--------------------------------\n" +
                "[C]Firma Cajero\n\n\n[L]\n"

        return ticket
    }


    fun imprimirListaFaltantes(faltantes: List<com.example.proyectofinal.models.Faltante>, macAddress: String?) {
        if (macAddress.isNullOrEmpty()) {
            Toast.makeText(context, "Configure impresora de Caja", Toast.LENGTH_SHORT).show()
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val device = bluetoothAdapter.getRemoteDevice(macAddress)
            val connection = BluetoothConnection(device)
            val printer = EscPosPrinter(connection, 203, 48f, 32)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val now = Date()

            var ticket = "[C]<u><font size='big'>LISTA DE COMPRAS</font></u>\n" +
                    "[C]FALTANTES / REPOSICION\n" +
                    "[C]Fecha: ${dateFormat.format(now)}\n" +
                    "[C]--------------------------------\n"

            if (faltantes.isEmpty()) {
                ticket += "[C]No hay faltantes registrados.\n"
            } else {
                faltantes.forEach { item ->
                    ticket += "[L]<b>[ ] ${item.nombre}</b>\n" +
                            "[R]Cant: ${item.cantidad}\n" +
                            "[C]- - - - - - - - - - - - - - -\n"
                }
            }

            ticket += "[C]--------------------------------\n" +
                    "[C]Fin de la lista\n\n\n[L]\n"

            printer.printFormattedText(ticket)

        } catch (e: Exception) {
            Log.e("TicketPrinter", "Error lista faltantes", e)
            Toast.makeText(context, "Error impresión: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}