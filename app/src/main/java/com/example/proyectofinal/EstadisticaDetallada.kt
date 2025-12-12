package com.example.proyectofinal

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyectofinal.models.Articulo
import com.example.proyectofinal.models.CategoriaProducto
import com.example.proyectofinal.models.Venta
import com.example.proyectofinal.repositories.ArticulosRepository
import com.example.proyectofinal.repositories.CategoriasRepository
import com.example.proyectofinal.repositories.VentasRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EstadisticaDetallada : AppCompatActivity() {

    private val categoriasRepo = CategoriasRepository()
    private val articulosRepo = ArticulosRepository()
    private val ventasRepo = VentasRepository()

    private var listaCategorias: List<CategoriaProducto> = emptyList()
    private var listaTodosArticulos: List<Articulo> = emptyList()
    private var ventasGlobales: List<Venta> = emptyList()

    private var idCategoriaSeleccionada: Int = -1
    private var idProductoSeleccionado: Int = -1
    private var metricaTiempoSeleccionada: Int = 0

    private val opcionesTiempo = listOf(
        "Día de la Semana (Dom-Sáb)",
        "Día del Mes (1-31)",
        "Día del Año (1-365)",
        "Semana del Mes (1-5)",
        "Semana del Año (1-52)",
        "Mes del Año (Ene-Dic)"
    )

    private lateinit var rgTipoReporte: RadioGroup
    private lateinit var layoutSelectores: LinearLayout
    private lateinit var layoutMetrica: LinearLayout
    private lateinit var layoutResultados: LinearLayout

    private lateinit var spCategorias: Spinner
    private lateinit var spProductos: Spinner
    private lateinit var spMetrica: Spinner

    private lateinit var btnConsultar: Button
    private lateinit var tvResultadoTiempo: TextView
    private lateinit var tvTituloGrafica: TextView
    private lateinit var chartComparativa: BarChart
    private lateinit var chartTiempo: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadistica_detallada)

        initViews()
        cargarDatosIniciales()
        setupListeners()
    }

    private fun initViews() {
        rgTipoReporte = findViewById(R.id.rgTipoReporte)
        layoutSelectores = findViewById(R.id.layoutSelectores)
        layoutMetrica = findViewById(R.id.layoutMetricaTiempo)
        layoutResultados = findViewById(R.id.layoutResultados)

        spCategorias = findViewById(R.id.spinnerCategoriaDetalle)
        spProductos = findViewById(R.id.spinnerProductoDetalle)
        spMetrica = findViewById(R.id.spinnerMetricaTiempo)

        btnConsultar = findViewById(R.id.btnConsultarDetalle)
        tvResultadoTiempo = findViewById(R.id.tvResultadoTiempo)
        tvTituloGrafica = findViewById(R.id.tvTituloGrafica)
        chartComparativa = findViewById(R.id.chartComparativa)
        chartTiempo = findViewById(R.id.chartTiempo)

        val adapterTiempo = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesTiempo)
        adapterTiempo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMetrica.adapter = adapterTiempo
    }

    private fun cargarDatosIniciales() {
        val fechaInicio = intent.getStringExtra("EXTRA_FECHA_INICIO") ?: ""
        val fechaFin = intent.getStringExtra("EXTRA_FECHA_FIN") ?: ""

        lifecycleScope.launch {
            try {
                listaCategorias = categoriasRepo.getCategorias()
                listaTodosArticulos = articulosRepo.getArticulos()
                setupSpinnerCategorias()

                if (fechaInicio.isNotEmpty()) {
                    ventasGlobales = ventasRepo.getVentasPorRango(fechaInicio, fechaFin)
                }
            } catch (e: Exception) {
                Toast.makeText(this@EstadisticaDetallada, "Error cargando datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinnerCategorias() {
        val nombres = listaCategorias.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategorias.adapter = adapter
    }

    private fun setupListeners() {
        rgTipoReporte.setOnCheckedChangeListener { _, checkedId ->
            layoutSelectores.visibility = View.VISIBLE
            layoutMetrica.visibility = View.VISIBLE
            layoutResultados.visibility = View.GONE

            when (checkedId) {
                R.id.rbPorCategoria -> spProductos.visibility = View.GONE
                R.id.rbPorProducto -> {
                    spProductos.visibility = View.VISIBLE
                    actualizarSpinnerProductos()
                }
            }
        }

        spCategorias.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (listaCategorias.isNotEmpty()) {
                    idCategoriaSeleccionada = listaCategorias[position].id
                    if (rgTipoReporte.checkedRadioButtonId == R.id.rbPorProducto) {
                        actualizarSpinnerProductos()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spProductos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val nombre = parent?.getItemAtPosition(position) as? String
                val prod = listaTodosArticulos.find { it.nombre == nombre }
                if (prod != null) idProductoSeleccionado = prod.id
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spMetrica.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                metricaTiempoSeleccionada = position
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnConsultar.setOnClickListener { procesarConsulta() }
    }

    private fun actualizarSpinnerProductos() {
        val filtrados = listaTodosArticulos.filter { it.id_categoria_producto == idCategoriaSeleccionada }
        if (filtrados.isNotEmpty()) {
            val nombres = filtrados.map { it.nombre }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spProductos.adapter = adapter
            idProductoSeleccionado = filtrados[0].id
        } else {
            spProductos.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Sin productos"))
            idProductoSeleccionado = -1
        }
    }

    private fun procesarConsulta() {
        if (ventasGlobales.isEmpty()) {
            Toast.makeText(this, "No hay ventas en el rango seleccionado", Toast.LENGTH_SHORT).show()
            return
        }

        val esPorCategoria = (rgTipoReporte.checkedRadioButtonId == R.id.rbPorCategoria)

        val ventasFiltradas = ventasGlobales.filter { venta ->
            if (venta.id_estado == 4) return@filter false

            venta.detalles?.any { det ->
                if (esPorCategoria) {
                    det.producto?.id_categoria_producto == idCategoriaSeleccionada
                } else {
                    det.producto?.id == idProductoSeleccionado
                }
            } ?: false
        }

        if (ventasFiltradas.isEmpty()) {
            Toast.makeText(this, "No se encontraron ventas con este criterio", Toast.LENGTH_SHORT).show()
            layoutResultados.visibility = View.GONE
            return
        }

        calcularMejorTiempo(ventasFiltradas)
        generarGraficaComparativa(esPorCategoria)

        layoutResultados.visibility = View.VISIBLE
    }

    private fun calcularMejorTiempo(ventas: List<Venta>) {
        val conteoTiempo = HashMap<Int, Double>()
        val calendar = Calendar.getInstance()

        val formatoISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatoSimple = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        ventas.forEach { venta ->
            try {
                var fecha = if (venta.fecha.contains("T")) formatoISO.parse(venta.fecha) else formatoSimple.parse(venta.fecha)

                if (fecha != null) {
                    calendar.time = fecha

                    val index = when (metricaTiempoSeleccionada) {
                        0 -> calendar.get(Calendar.DAY_OF_WEEK) // 1 (Dom) - 7 (Sab)
                        1 -> calendar.get(Calendar.DAY_OF_MONTH) // 1 - 31
                        2 -> calendar.get(Calendar.DAY_OF_YEAR) // 1 - 366
                        3 -> calendar.get(Calendar.WEEK_OF_MONTH) // 1 - 5
                        4 -> calendar.get(Calendar.WEEK_OF_YEAR) // 1 - 53
                        5 -> calendar.get(Calendar.MONTH) // 0 (Ene) - 11 (Dic)
                        else -> -1
                    }

                    if (index != -1) {
                        val montoVenta = venta.total_final ?: 0.0
                        conteoTiempo[index] = (conteoTiempo[index] ?: 0.0) + montoVenta
                    }
                }
            } catch (e: Exception) { }
        }

        val ganador = conteoTiempo.maxByOrNull { it.value }

        if (ganador != null) {
            val etiquetaGanadora = obtenerEtiquetaTiempo(metricaTiempoSeleccionada, ganador.key)
            val formatoDinero = java.text.NumberFormat.getCurrencyInstance(Locale("es", "MX"))

            tvResultadoTiempo.text = "El momento con mayores ventas es:\n$etiquetaGanadora\n(Total: ${formatoDinero.format(ganador.value)})"

            generarGraficaTiempoCompleta(conteoTiempo, metricaTiempoSeleccionada)
        } else {
            tvResultadoTiempo.text = "No hay datos suficientes."
            chartTiempo.clear()
        }
    }

    private fun generarGraficaTiempoCompleta(datosReales: Map<Int, Double>, metrica: Int) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        val (min, max) = when (metrica) {
            0 -> Pair(1, 7)
            1 -> Pair(1, 31)
            2 -> Pair(1, 365)
            3 -> Pair(1, 5)
            4 -> Pair(1, 52)
            5 -> Pair(0, 11)
            else -> Pair(1, 10)
        }

        var indexX = 0f

        for (i in min..max) {
            val valor = datosReales[i] ?: 0.0
            entries.add(Entry(indexX, valor.toFloat()))

            labels.add(obtenerEtiquetaTiempo(metrica, i))
            indexX++
        }

        val dataSet = LineDataSet(entries, "Ventas")
        dataSet.color = Color.parseColor("#009688")
        dataSet.valueTextSize = 8f
        dataSet.setDrawCircles(false)

        if (max - min < 20) {
            dataSet.setDrawCircles(true)
            dataSet.setCircleColor(Color.parseColor("#00796B"))
            dataSet.circleRadius = 4f
        } else {
            dataSet.setDrawValues(false)
        }

        dataSet.lineWidth = 2f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#B2DFDB")

        val lineData = LineData(dataSet)
        chartTiempo.data = lineData

        chartTiempo.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartTiempo.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartTiempo.xAxis.setDrawGridLines(false)

        if (max - min > 30) {
            chartTiempo.xAxis.labelCount = 5
        } else {
            chartTiempo.xAxis.labelCount = labels.size
        }

        chartTiempo.description.isEnabled = false
        chartTiempo.axisRight.isEnabled = false
        chartTiempo.invalidate()
        chartTiempo.animateX(1000)
    }

    private fun obtenerEtiquetaTiempo(metrica: Int, valor: Int): String {
        return when (metrica) {
            0 -> when(valor) {
                Calendar.SUNDAY -> "Domingo"; Calendar.MONDAY -> "Lunes"; Calendar.TUESDAY -> "Martes"
                Calendar.WEDNESDAY -> "Miercoles"; Calendar.THURSDAY -> "Jueves"; Calendar.FRIDAY -> "Viernes"; Calendar.SATURDAY -> "Sabado"
                else -> "?"
            }
            1 -> "$valor"
            2 -> "Día $valor"
            3 -> "Sem $valor"
            4 -> "Sem $valor"
            5 -> when(valor) {
                0 -> "Ene"; 1 -> "Feb"; 2 -> "Mar"; 3 -> "Abr"; 4 -> "May"; 5 -> "Jun"
                6 -> "Jul"; 7 -> "Ago"; 8 -> "Sep"; 9 -> "Oct"; 10 -> "Nov"; 11 -> "Dic"
                else -> "?"
            }
            else -> "$valor"
        }
    }

    private fun generarGraficaComparativa(esPorCategoria: Boolean) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val conteo = HashMap<String, Double>()

        if (esPorCategoria) {
            tvTituloGrafica.text = "Comparativa: Esta categoría contra otras"
            ventasGlobales.forEach { v ->
                if (v.id_estado != 4) {
                    v.detalles?.forEach { d ->
                        val catNombre = d.producto?.categoria_producto?.nombre ?: "Sin Cat"
                        val totalLinea = d.cantidad * d.precio_unidad
                        conteo[catNombre] = (conteo[catNombre] ?: 0.0) + totalLinea
                    }
                }
            }
        } else {
            tvTituloGrafica.text = "Comparativa: este producto contra otros (misma categoria)"
            ventasGlobales.forEach { v ->
                if (v.id_estado != 4) {
                    v.detalles?.forEach { d ->
                        if (d.producto?.id_categoria_producto == idCategoriaSeleccionada) {
                            val prodNombre = d.producto?.nombre ?: "Desc"
                            val totalLinea = d.cantidad * d.precio_unidad
                            conteo[prodNombre] = (conteo[prodNombre] ?: 0.0) + totalLinea
                        }
                    }
                }
            }
        }

        val sorted = conteo.toList().sortedByDescending { it.second }.take(7)
        sorted.forEachIndexed { index, (label, value) ->
            entries.add(BarEntry(index.toFloat(), value.toFloat()))
            labels.add(label)
        }

        val dataSet = BarDataSet(entries, "Ventas")
        dataSet.color = Color.parseColor("#FF9800")

        val barData = BarData(dataSet)
        chartComparativa.data = barData
        chartComparativa.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartComparativa.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartComparativa.description.isEnabled = false
        chartComparativa.axisRight.isEnabled = false
        chartComparativa.invalidate()
    }
}