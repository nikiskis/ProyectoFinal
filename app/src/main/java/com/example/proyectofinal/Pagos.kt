package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyectofinal.models.VentaPagoInsert
import com.example.proyectofinal.repositories.VentasRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class Pagos : AppCompatActivity() {

    private var ventaId: Int = -1
    private var totalOriginal: Double = 0.0
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

    private val ventasRepo = VentasRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        ventaId = intent.getIntExtra("EXTRA_VENTA_ID", -1)
        totalOriginal = intent.getDoubleExtra("EXTRA_TOTAL_ORIGINAL", 0.0)

        initViews()
        calcularTotales()
        setupListeners()
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
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnAplicarDescuento).setOnClickListener {
            showPasswordDialog()
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = validarMontosPago()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etPagoEfectivo.addTextChangedListener(textWatcher)
        etPagoTarjeta.addTextChangedListener(textWatcher)

        btnCobrar.setOnClickListener {
            procesarCobro()
        }
    }

    private fun validarMontosPago() {
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
        } else if (restante < -0.01) {
            tvRestante.text = "Excede por: ${format.format(Math.abs(restante))}"
            tvRestante.setTextColor(getColor(android.R.color.holo_red_dark))
            btnCobrar.isEnabled = false
            btnCobrar.alpha = 0.5f
        } else {
            tvRestante.text = "Completo"
            tvRestante.setTextColor(getColor(android.R.color.holo_green_dark))
            btnCobrar.isEnabled = true
            btnCobrar.alpha = 1.0f
        }
    }

    private fun procesarCobro() {
        val efectivo = etPagoEfectivo.text.toString().toDoubleOrNull() ?: 0.0
        val tarjeta = etPagoTarjeta.text.toString().toDoubleOrNull() ?: 0.0
        val propina = etPropina.text.toString().toDoubleOrNull() ?: 0.0

        val idMetodoPropina = if (findViewById<android.widget.RadioButton>(R.id.rbPropinaEfectivo).isChecked) 1 else 2

        lifecycleScope.launch {
            val listaPagos = mutableListOf<VentaPagoInsert>()

            if (efectivo > 0) {
                listaPagos.add(VentaPagoInsert(ventaId, 1, efectivo))
            }
            if (tarjeta > 0) {
                listaPagos.add(VentaPagoInsert(ventaId, 2, tarjeta))
            }

            try {
                ventasRepo.finalizarVentaCompleta(
                    ventaId,
                    descuentoMonto,
                    descuentoPorcentaje,
                    totalFinal,
                    propina,
                    idMetodoPropina,
                    listaPagos
                )
                Toast.makeText(this@Pagos, "Venta Finalizada", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@Pagos, Pedido::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@Pagos, "Error al finalizar venta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calcularTotales() {
        val descuentoPorMonto = descuentoMonto
        val descuentoPorPorcentaje = totalOriginal * (descuentoPorcentaje / 100.0)
        var totalDescuento = descuentoPorMonto + descuentoPorPorcentaje

        if (totalDescuento > totalOriginal) totalDescuento = totalOriginal
        totalFinal = totalOriginal - totalDescuento

        actualizarUI(totalDescuento)
        validarMontosPago()
    }

    private fun actualizarUI(totalDescuento: Double) {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        tvSubtotal.text = format.format(totalOriginal)
        tvDescuento.text = "-${format.format(totalDescuento)} ($descuentoPorcentaje%)"
        tvTotalFinal.text = format.format(totalFinal)
    }

    private fun showPasswordDialog() {
        val input = EditText(this)
        input.hint = "Contraseña"
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER

        val dialog = AlertDialog.Builder(this)
            .setTitle("Autorización Requerida")
            .setView(input)
            .setPositiveButton("Aceptar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (input.text.toString() == "1234") {
                dialog.dismiss()
                showDescuentoInputDialog()
            } else {
                Toast.makeText(this, "Contraseña Incorrecta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDescuentoInputDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_descuento, null)
        val etMonto = dialogView.findViewById<EditText>(R.id.etDescuentoMonto)
        val etPorcentaje = dialogView.findViewById<EditText>(R.id.etDescuentoPorcentaje)

        if (descuentoMonto > 0) etMonto.setText(descuentoMonto.toString())
        if (descuentoPorcentaje > 0) etPorcentaje.setText(descuentoPorcentaje.toString())

        AlertDialog.Builder(this)
            .setTitle("Configurar Descuento")
            .setView(dialogView)
            .setPositiveButton("Aplicar") { _, _ ->
                val nuevoMonto = etMonto.text.toString().toDoubleOrNull() ?: 0.0
                val nuevoPorcentaje = etPorcentaje.text.toString().toIntOrNull() ?: 0
                val descCalc = nuevoMonto + (totalOriginal * (nuevoPorcentaje / 100.0))

                if (descCalc > totalOriginal) {
                    Toast.makeText(this, "Descuento inválido", Toast.LENGTH_SHORT).show()
                } else {
                    descuentoMonto = nuevoMonto
                    descuentoPorcentaje = nuevoPorcentaje
                    calcularTotales()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}