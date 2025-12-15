package com.example.proyectofinal

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyectofinal.repositories.VentasRepository
import com.github.mikephil.charting.charts.BubbleChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.BubbleDataSet
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EstadisticaCompleja : AppCompatActivity() {

    private val ventasRepo = VentasRepository()
    private lateinit var chartPareto: CombinedChart
    private lateinit var chartHeatmap: BubbleChart
    private lateinit var btnRegresar: Button
    private lateinit var btnActualizar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadistica_compleja)

        chartPareto = findViewById(R.id.chartPareto)
        chartHeatmap = findViewById(R.id.chartHeatmap)
        btnRegresar = findViewById(R.id.btnRegresar)
        btnActualizar = findViewById(R.id.btnActualizar)

        btnRegresar.setOnClickListener { finish() }
        btnActualizar.setOnClickListener { cargarDatos() }

        setupParetoChart()
        setupHeatmapChart()
        cargarDatos()
    }

    private fun setupParetoChart() {
        chartPareto.description.isEnabled = false
        chartPareto.setBackgroundColor(Color.WHITE)
        chartPareto.setDrawGridBackground(false)
        chartPareto.setDrawBarShadow(false)
        chartPareto.isHighlightFullBarEnabled = false

        chartPareto.drawOrder = arrayOf(
            CombinedChart.DrawOrder.BAR,
            CombinedChart.DrawOrder.LINE
        )

        val rightAxis = chartPareto.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 105f

        val leftAxis = chartPareto.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f

        val xAxis = chartPareto.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = -0.5f
        xAxis.granularity = 1f
    }

    private fun setupHeatmapChart() {
        chartHeatmap.description.isEnabled = false
        chartHeatmap.setDrawGridBackground(false)

        val xAxis = chartHeatmap.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 8f
        val dias = listOf("", "Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "")
        xAxis.valueFormatter = IndexAxisValueFormatter(dias)

        val leftAxis = chartHeatmap.axisLeft
        leftAxis.granularity = 1f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 24f
        leftAxis.setLabelCount(12, false)

        chartHeatmap.axisRight.isEnabled = false
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                val ventas = ventasRepo.getVentasComplejas()

                if (ventas.isEmpty()) {
                    Toast.makeText(this@EstadisticaCompleja, "No hay datos suficientes", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                generarPareto(ventas)
                generarHeatmap(ventas)

            } catch (e: Exception) {
                Toast.makeText(this@EstadisticaCompleja, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generarPareto(ventas: com.example.proyectofinal.models.Venta) {
    }
    private fun generarPareto(ventas: List<com.example.proyectofinal.models.Venta>) {
        val ventasPorProducto = HashMap<String, Double>()

        ventas.forEach { v ->
            if (v.id_estado != 4) {
                v.detalles?.forEach { d ->
                    val nombre = d.producto?.nombre ?: "Desconocido"
                    val total = d.cantidad * d.precio_unidad
                    ventasPorProducto[nombre] = (ventasPorProducto[nombre] ?: 0.0) + total
                }
            }
        }

        if (ventasPorProducto.isEmpty()) return
        val sortedList = ventasPorProducto.toList().sortedByDescending { it.second }
        val topList = sortedList.take(15)
        val totalVentasMuestra = topList.sumOf { it.second }
        val barEntries = ArrayList<BarEntry>()
        val lineEntries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        var acumulado = 0.0

        topList.forEachIndexed { index, pair ->
            barEntries.add(BarEntry(index.toFloat(), pair.second.toFloat()))
            labels.add(pair.first)
            acumulado += pair.second
            val porcentaje = (acumulado / totalVentasMuestra) * 100
            lineEntries.add(Entry(index.toFloat(), porcentaje.toFloat()))
        }
        val data = CombinedData()
        val barDataSet = BarDataSet(barEntries, "Ventas ($)")
        barDataSet.color = Color.parseColor("#488079")
        barDataSet.valueTextSize = 10f
        barDataSet.axisDependency = YAxis.AxisDependency.LEFT
        val lineDataSet = LineDataSet(lineEntries, "% Acumulado")
        lineDataSet.color = Color.parseColor("#FF9800")
        lineDataSet.lineWidth = 2.5f
        lineDataSet.setCircleColor(Color.parseColor("#FF9800"))
        lineDataSet.circleRadius = 4f
        lineDataSet.axisDependency = YAxis.AxisDependency.RIGHT
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        data.setData(BarData(barDataSet))
        data.setData(LineData(lineDataSet))

        chartPareto.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartPareto.xAxis.labelCount = labels.size
        chartPareto.xAxis.labelRotationAngle = -45f

        chartPareto.data = data
        chartPareto.invalidate()
        chartPareto.animateY(1500)
    }

    private fun generarHeatmap(ventas: List<com.example.proyectofinal.models.Venta>) {
        val matrix = Array(8) { IntArray(24) { 0 } } // [Dia][Hora]

        val cal = Calendar.getInstance()
        val sdfISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val sdfSimple = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        ventas.forEach { v ->
            if (v.id_estado != 4) {
                try {
                    val date = if(v.fecha.contains("T")) sdfISO.parse(v.fecha) else sdfSimple.parse(v.fecha)
                    if (date != null) {
                        cal.time = date
                        val diaSemana = cal.get(Calendar.DAY_OF_WEEK)
                        val horaDia = cal.get(Calendar.HOUR_OF_DAY)

                        matrix[diaSemana][horaDia]++
                    }
                } catch (e: Exception) {}
            }
        }

        val entries = ArrayList<BubbleEntry>()

        for (d in 1..7) {
            for (h in 0..23) {
                val count = matrix[d][h]
                if (count > 0) {
                    entries.add(BubbleEntry(d.toFloat(), h.toFloat(), count.toFloat()))
                }
            }
        }

        if (entries.isEmpty()) return

        val dataSet = BubbleDataSet(entries, "Intensidad de Venta")
        dataSet.color = Color.parseColor("#E53935")
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 8f
        dataSet.isNormalizeSizeEnabled = true

        val bubbleData = BubbleData(dataSet)
        chartHeatmap.data = bubbleData
        chartHeatmap.invalidate()
        chartHeatmap.animateXY(1000, 1000)
    }
}