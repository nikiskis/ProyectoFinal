package com.example.proyectofinal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyectofinal.models.ZonaProduccion
import com.example.proyectofinal.repositories.ZonasRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CorteCaja : AppCompatActivity() {

    private lateinit var etFechaInicio: EditText
    private lateinit var etHoraInicio: EditText
    private lateinit var etFechaFin: EditText
    private lateinit var etHoraFin: EditText
    private lateinit var etMontoInicial: EditText
    private lateinit var etSelectorZonas: EditText
    private lateinit var btnGenerar: Button
    private lateinit var btnRegresar: Button

    private val calInicio = Calendar.getInstance()
    private val calFin = Calendar.getInstance()

    private val zonasRepo = ZonasRepository()

    private var listaZonasCompleta: List<ZonaProduccion> = emptyList()
    private var nombresZonasArray: Array<String> = emptyArray()
    private var zonasSeleccionadasBoolean: BooleanArray = booleanArrayOf()
    private val idsZonasSeleccionadas = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_corte_caja)

        initViews()
        setupListeners()
        loadZonas()

        updateDisplay(etFechaInicio, etHoraInicio, calInicio)
        updateDisplay(etFechaFin, etHoraFin, calFin)
    }

    private fun initViews() {
        etFechaInicio = findViewById(R.id.etFechaInicio)
        etHoraInicio = findViewById(R.id.etHoraInicio)
        etFechaFin = findViewById(R.id.etFechaFin)
        etHoraFin = findViewById(R.id.etHoraFin)
        etMontoInicial = findViewById(R.id.etMontoInicial)
        etSelectorZonas = findViewById(R.id.etSelectorZonas)
        btnGenerar = findViewById(R.id.btnGenerarCorte)
        btnRegresar = findViewById(R.id.btnRegresar)

        calInicio.set(Calendar.HOUR_OF_DAY, 0)
        calInicio.set(Calendar.MINUTE, 0)
    }

    private fun loadZonas() {
        lifecycleScope.launch {
            try {
                val zonasDB = zonasRepo.getZonas()
                listaZonasCompleta = zonasDB
                nombresZonasArray = zonasDB.map { it.nombre }.toTypedArray()
                zonasSeleccionadasBoolean = BooleanArray(nombresZonasArray.size)

            } catch (e: Exception) {
                Toast.makeText(this@CorteCaja, "Error cargando zonas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        btnRegresar.setOnClickListener { finish() }

        etFechaInicio.setOnClickListener { showDatePicker(calInicio, etFechaInicio) }
        etHoraInicio.setOnClickListener { showTimePicker(calInicio, etHoraInicio) }
        etFechaFin.setOnClickListener { showDatePicker(calFin, etFechaFin) }
        etHoraFin.setOnClickListener { showTimePicker(calFin, etHoraFin) }
        etSelectorZonas.setOnClickListener {
            showMultiSelectZonasDialog()
        }

        btnGenerar.setOnClickListener {
            if (calInicio.after(calFin)) {
                Toast.makeText(this, "La fecha de inicio debe ser anterior a la final", Toast.LENGTH_SHORT).show()
            } else {
                generarReporte()
            }
        }
    }

    private fun showMultiSelectZonasDialog() {
        if (nombresZonasArray.isEmpty()) {
            Toast.makeText(this, "No hay zonas cargadas", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona Zonas")
        builder.setCancelable(false)

        builder.setMultiChoiceItems(nombresZonasArray, zonasSeleccionadasBoolean) { _, position, isChecked ->
            zonasSeleccionadasBoolean[position] = isChecked
        }

        builder.setPositiveButton("Aceptar") { _, _ ->
            idsZonasSeleccionadas.clear()
            val nombresSeleccionados = StringBuilder()

            for (i in zonasSeleccionadasBoolean.indices) {
                if (zonasSeleccionadasBoolean[i]) {
                    idsZonasSeleccionadas.add(listaZonasCompleta[i].id)

                    if (nombresSeleccionados.isNotEmpty()) {
                        nombresSeleccionados.append(", ")
                    }
                    nombresSeleccionados.append(nombresZonasArray[i])
                }
            }

            if (idsZonasSeleccionadas.isEmpty()) {
                etSelectorZonas.setText("")
                etSelectorZonas.hint = "Ninguna seleccionada"
            } else {
                etSelectorZonas.setText(nombresSeleccionados.toString())
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.setNeutralButton("Todas") { _, _ ->
            idsZonasSeleccionadas.clear()
            for (i in zonasSeleccionadasBoolean.indices) {
                zonasSeleccionadasBoolean[i] = true
                idsZonasSeleccionadas.add(listaZonasCompleta[i].id)
            }
            etSelectorZonas.setText("Todas las zonas")
        }

        builder.show()
    }

    private fun showDatePicker(calendar: Calendar, view: EditText) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateDisplay(view, null, calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar, view: EditText) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                updateDisplay(null, view, calendar)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun updateDisplay(dateView: EditText?, timeView: EditText?, calendar: Calendar) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        dateView?.setText(dateFormat.format(calendar.time))
        timeView?.setText(timeFormat.format(calendar.time))
    }

    private fun generarReporte() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fechaInicioStr = dateFormat.format(calInicio.time)
        val fechaFinStr = dateFormat.format(calFin.time)
        val montoInicial = etMontoInicial.text.toString().toDoubleOrNull() ?: 0.0

        if (idsZonasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona al menos una zona", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = android.content.Intent(this, ReporteCorte::class.java)
        intent.putExtra("EXTRA_FECHA_INICIO", fechaInicioStr)
        intent.putExtra("EXTRA_FECHA_FIN", fechaFinStr)
        intent.putExtra("EXTRA_MONTO_INICIAL", montoInicial)
        intent.putIntegerArrayListExtra("EXTRA_ZONAS_IDS", idsZonasSeleccionadas)
        startActivity(intent)
    }
}