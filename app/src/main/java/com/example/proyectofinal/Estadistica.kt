package com.example.proyectofinal

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyectofinal.models.Venta
import com.example.proyectofinal.repositories.VentasRepository
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Estadistica : AppCompatActivity() {

    private val ventasRepo = VentasRepository()

    private lateinit var chartProductos: HorizontalBarChart
    private lateinit var chartCategorias: HorizontalBarChart
    private lateinit var chartZonas: HorizontalBarChart

    private lateinit var etFechaInicio: EditText
    private lateinit var etFechaFin: EditText
    private lateinit var btnFiltrar: Button

    private var listaZonasIds: ArrayList<Int> = ArrayList()
    private val calInicio = Calendar.getInstance()
    private val calFin = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadistica)

        initUI()
        configurarFechasIniciales()

        findViewById<Button>(R.id.btnRegresar).setOnClickListener { finish() }

        etFechaInicio.setOnClickListener { showDatePicker(calInicio, etFechaInicio) }
        etFechaFin.setOnClickListener { showDatePicker(calFin, etFechaFin) }

        btnFiltrar.setOnClickListener { cargarDatos() }

        findViewById<Button>(R.id.btnDetalleEstadistica).setOnClickListener {
            val inicio = etFechaInicio.text.toString() + " 00:00:00"
            val fin = etFechaFin.text.toString() + " 23:59:59"

            val intent = Intent(this, EstadisticaDetallada::class.java)
            intent.putExtra("EXTRA_FECHA_INICIO", inicio)
            intent.putExtra("EXTRA_FECHA_FIN", fin)
            intent.putIntegerArrayListExtra("EXTRA_ZONAS_IDS", listaZonasIds)
            startActivity(intent)
        }

        setupChartConfig(chartProductos)
        setupChartConfig(chartCategorias)
        setupChartConfig(chartZonas)

        cargarDatos()
    }

    private fun initUI() {
        etFechaInicio = findViewById(R.id.etEstadisticaInicio)
        etFechaFin = findViewById(R.id.etEstadisticaFin)
        btnFiltrar = findViewById(R.id.btnFiltrarEstadistica)

        chartProductos = findViewById(R.id.chartProductos)
        chartCategorias = findViewById(R.id.chartCategorias)
        chartZonas = findViewById(R.id.chartZonas)
    }

    private fun configurarFechasIniciales() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Date()
        val fechaHoyStr = dateFormat.format(today)

        val fechaInicioIntent = intent.getStringExtra("EXTRA_FECHA_INICIO")
        val fechaFinIntent = intent.getStringExtra("EXTRA_FECHA_FIN")

        val inicio = if (!fechaInicioIntent.isNullOrEmpty()) fechaInicioIntent.take(10) else fechaHoyStr
        val fin = if (!fechaFinIntent.isNullOrEmpty()) fechaFinIntent.take(10) else fechaHoyStr

        listaZonasIds = intent.getIntegerArrayListExtra("EXTRA_ZONAS_IDS") ?: ArrayList()

        etFechaInicio.setText(inicio)
        etFechaFin.setText(fin)
    }

    private fun setupChartConfig(chart: HorizontalBarChart) {
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.setDrawLabels(true)

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f

        chart.axisRight.isEnabled = false
        chart.setNoDataText("Cargando datos...")
    }

    private fun cargarDatos() {
        val queryInicio = etFechaInicio.text.toString() + " 00:00:00"
        val queryFin = etFechaFin.text.toString() + " 23:59:59"

        lifecycleScope.launch {
            try {
                val ventas = ventasRepo.getVentasPorRango(queryInicio, queryFin)
                procesarEstadisticas(ventas)
            } catch (e: Exception) {
                Toast.makeText(this@Estadistica, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun procesarEstadisticas(ventas: List<Venta>) {
        val conteoProductos = HashMap<String, Int>()
        val conteoCategorias = HashMap<String, Int>()
        val conteoZonas = HashMap<String, Int>()

        ventas.forEach { venta ->
            if (venta.id_estado != 4) {
                venta.detalles?.forEach { detalle ->
                    val zonaProd = detalle.producto?.id_zona_produccion ?: 0
                    val zonaNombre = detalle.producto?.zona_produccion?.nombre ?: "Sin Zona"
                    val catNombre = detalle.producto?.categoria_producto?.nombre ?: "Sin Cat"
                    val prodNombre = detalle.producto?.nombre ?: "Desconocido"

                    if (listaZonasIds.isEmpty() || listaZonasIds.contains(zonaProd)) {
                        val cant = detalle.cantidad

                        conteoProductos[prodNombre] = (conteoProductos[prodNombre] ?: 0) + cant
                        conteoCategorias[catNombre] = (conteoCategorias[catNombre] ?: 0) + cant
                        conteoZonas[zonaNombre] = (conteoZonas[zonaNombre] ?: 0) + cant
                    }
                }
            }
        }

        renderChart(chartProductos, conteoProductos, 20, "#488079")
        renderChart(chartCategorias, conteoCategorias, 20, "#FF9800")
        renderChart(chartZonas, conteoZonas, 20, "#2196F3")
    }

    private fun renderChart(chart: HorizontalBarChart, dataMap: Map<String, Int>, limit: Int, colorHex: String) {
        if (dataMap.isEmpty()) {
            chart.clear()
            return
        }

        val sortedData = dataMap.toList()
            .sortedByDescending { (_, value) -> value }
            .take(limit)

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        sortedData.reversed().forEachIndexed { index, (key, value) ->
            entries.add(BarEntry(index.toFloat(), value.toFloat()))
            labels.add(key)
        }

        val dataSet = BarDataSet(entries, "Datos")
        dataSet.color = Color.parseColor(colorHex)
        dataSet.valueTextSize = 14f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelCount = labels.size
        chart.data = barData
        chart.invalidate()
        chart.animateY(800)
    }

    private fun showDatePicker(calendar: Calendar, view: EditText) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                view.setText(format.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}