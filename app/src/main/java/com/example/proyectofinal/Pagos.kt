package com.example.proyectofinal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyectofinal.models.DetalleVenta
import com.example.proyectofinal.models.Venta
import com.example.proyectofinal.models.VentaPagoInsert
import com.example.proyectofinal.repositories.VentasRepository
import com.example.proyectofinal.utils.PrinterPreferences
import com.example.proyectofinal.utils.TicketPrinter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class Pagos : AppCompatActivity() {

    private var ventaId: Int = -1
    private var subtotalOriginal: Double = 0.0
    private var descuentoMonto: Double = 0.0
    private var descuentoPorcentaje: Int = 0
    private var totalFinal: Double = 0.0
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDescuento: TextView
    private lateinit var tvTotalFinal: TextView
    private lateinit var etPropina: EditText
    private lateinit var rgMetodoPropina: RadioGroup
    private lateinit var etPagoEfectivo: EditText
    private lateinit var etPagoTarjeta: EditText
    private lateinit var tvRestante: TextView
    private lateinit var btnCobrar: Button
    private lateinit var btnAplicarDescuento: Button

    private val ventasRepo = VentasRepository()
    private lateinit var ticketPrinter: TicketPrinter
    private lateinit var printerPrefs: PrinterPreferences

    private var ventaActual: Venta? = null
    private var detallesActuales: List<DetalleVenta> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        ventaId = intent.getIntExtra("EXTRA_VENTA_ID", -1)
        subtotalOriginal = intent.getDoubleExtra("EXTRA_TOTAL_ORIGINAL", 0.0)
        totalFinal = subtotalOriginal

        ticketPrinter = TicketPrinter(this)
        printerPrefs = PrinterPreferences(this)

        initViews()
        setupListeners()
        updateUI()

        loadVentaData()
    }

    private fun initViews() {
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvDescuento = findViewById(R.id.tvDescuento)
        tvTotalFinal = findViewById(R.id.tvTotalFinal)
        etPropina = findViewById(R.id.etPropina)
        rgMetodoPropina = findViewById(R.id.rgMetodoPropina)
        etPagoEfectivo = findViewById(R.id.etPagoEfectivo)
        etPagoTarjeta = findViewById(R.id.etPagoTarjeta)
        tvRestante = findViewById(R.id.tvRestante)
        btnCobrar = findViewById(R.id.btnCobrar)
        btnAplicarDescuento = findViewById(R.id.btnAplicarDescuento)
    }

    private fun loadVentaData() {
        lifecycleScope.launch {
            ventaActual = ventasRepo.getVentaById(ventaId)
            detallesActuales = ventasRepo.getDetallesVenta(ventaId)

            if (ventaActual != null) {
                descuentoMonto = ventaActual?.descuento_monto ?: 0.0
                descuentoPorcentaje = ventaActual?.descuento_porcentaje ?: 0
                recalcularTotal()
            }
        }
    }

    private fun setupListeners() {
        btnAplicarDescuento.setOnClickListener {
            showPasswordDialog()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularRestante()
            }
        }

        etPagoEfectivo.addTextChangedListener(textWatcher)
        etPagoTarjeta.addTextChangedListener(textWatcher)

        btnCobrar.setOnClickListener {
            finalizarVenta()
        }
    }

    private fun showPasswordDialog() {
        val input = EditText(this)
        input.hint = "Contraseña Administrativa"
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.gravity = Gravity.CENTER

        AlertDialog.Builder(this)
            .setTitle("Autorización Requerida")
            .setMessage("Ingrese contraseña para aplicar descuento:")
            .setView(input)
            .setPositiveButton("Aceptar") { dialog, _ ->
                val password = input.text.toString()
                if (password == "1234") { // Contraseña quemada, igual que en cancelación
                    showDescuentoDialog()
                } else {
                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDescuentoDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_descuento, null)
        val etMonto = dialogView.findViewById<EditText>(R.id.etDescuentoMonto)
        val etPorcentaje = dialogView.findViewById<EditText>(R.id.etDescuentoPorcentaje)

        AlertDialog.Builder(this)
            .setTitle("Aplicar Descuento")
            .setView(dialogView)
            .setPositiveButton("Aplicar") { _, _ ->
                val montoStr = etMonto.text.toString()
                val porcStr = etPorcentaje.text.toString()

                if (porcStr.isNotEmpty()) {
                    val porcentaje = porcStr.toIntOrNull() ?: 0
                    descuentoPorcentaje = porcentaje
                    descuentoMonto = subtotalOriginal * (porcentaje / 100.0)
                } else if (montoStr.isNotEmpty()) {
                    descuentoMonto = montoStr.toDoubleOrNull() ?: 0.0
                    descuentoPorcentaje = 0
                } else {
                    descuentoMonto = 0.0
                    descuentoPorcentaje = 0
                }

                guardarYImprimirDescuento()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarYImprimirDescuento() {
        lifecycleScope.launch {
            try {
                ventasRepo.updateDescuentoVenta(ventaId, descuentoMonto, descuentoPorcentaje)

                recalcularTotal()
                ventaActual = ventasRepo.getVentaById(ventaId)

                checkAndPrintTicket()

                Toast.makeText(this@Pagos, "Descuento aplicado e imprimiendo...", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@Pagos, "Error al guardar descuento: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndPrintTicket() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 101)
                return
            }
        }

        val macCaja = printerPrefs.obtenerImpresoraCaja()
        if (ventaActual != null && detallesActuales.isNotEmpty()) {
            if (!macCaja.isNullOrEmpty()) {
                ticketPrinter.imprimir(ventaActual!!, detallesActuales, "Cliente", macCaja)
            } else {
                Toast.makeText(this, "No hay impresora de caja configurada", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun recalcularTotal() {
        totalFinal = subtotalOriginal - descuentoMonto
        if (totalFinal < 0) totalFinal = 0.0
        updateUI()
        calcularRestante()
    }

    private fun updateUI() {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        tvSubtotal.text = format.format(subtotalOriginal)
        tvDescuento.text = "-${format.format(descuentoMonto)}"
        tvTotalFinal.text = format.format(totalFinal)
    }

    private fun calcularRestante() {
        val efectivo = etPagoEfectivo.text.toString().toDoubleOrNull() ?: 0.0
        val tarjeta = etPagoTarjeta.text.toString().toDoubleOrNull() ?: 0.0

        val pagado = efectivo + tarjeta
        val restante = totalFinal - pagado

        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        if (restante > 0.01) {
            tvRestante.text = "Faltante: ${format.format(restante)}"
            tvRestante.setTextColor(getColor(android.R.color.holo_red_dark))
            btnCobrar.isEnabled = false
            btnCobrar.alpha = 0.5f
        } else {
            val cambio = pagado - totalFinal
            tvRestante.text = "Cambio: ${format.format(cambio)}"
            tvRestante.setTextColor(getColor(android.R.color.holo_green_dark))
            btnCobrar.isEnabled = true
            btnCobrar.alpha = 1.0f
        }
    }

    private fun finalizarVenta() {
        val propina = etPropina.text.toString().toDoubleOrNull() ?: 0.0
        val idMetodoPropina = if (rgMetodoPropina.checkedRadioButtonId == R.id.rbPropinaEfectivo) 1 else 2

        val pagoEfectivo = etPagoEfectivo.text.toString().toDoubleOrNull() ?: 0.0
        val pagoTarjeta = etPagoTarjeta.text.toString().toDoubleOrNull() ?: 0.0

        val listaPagos = mutableListOf<VentaPagoInsert>()
        if (pagoEfectivo > 0) listaPagos.add(VentaPagoInsert(ventaId, 1, pagoEfectivo))
        if (pagoTarjeta > 0) listaPagos.add(VentaPagoInsert(ventaId, 2, pagoTarjeta))

        lifecycleScope.launch {
            try {
                ventasRepo.finalizarVentaCompleta(
                    idVenta = ventaId,
                    descuentoMonto = descuentoMonto,
                    descuentoPorcentaje = descuentoPorcentaje,
                    totalFinal = totalFinal,
                    propina = propina,
                    idMetodoPropina = idMetodoPropina,
                    pagos = listaPagos
                )

                Toast.makeText(this@Pagos, "Venta Finalizada", Toast.LENGTH_LONG).show()

                val intent = android.content.Intent(this@Pagos, MainActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@Pagos, "Error al finalizar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}