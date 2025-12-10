package com.example.proyectofinal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.adapter.ReporteAdapter
import com.example.proyectofinal.models.Venta
import com.example.proyectofinal.repositories.VentasRepository
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ReporteCorte : AppCompatActivity() {

    private val ventasRepo = VentasRepository()

    private var fechaInicio: String = ""
    private var fechaFin: String = ""
    private var montoInicial: Double = 0.0
    private var listaZonasIds: ArrayList<Int> = ArrayList()
    private lateinit var tvFondoInicial: TextView
    private lateinit var tvTotalEfectivo: TextView
    private lateinit var tvTotalCaja: TextView
    private lateinit var tvTotalTarjeta: TextView
    private lateinit var tvGranTotalVentas: TextView
    private lateinit var tvCostoProduccion: TextView
    private lateinit var tvGanancia: TextView

    private lateinit var rvDescuentos: RecyclerView
    private lateinit var rvCancelados: RecyclerView
    private lateinit var rvTodos: RecyclerView
    private lateinit var switchDesglose: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_corte)

        fechaInicio = intent.getStringExtra("EXTRA_FECHA_INICIO") ?: ""
        fechaFin = intent.getStringExtra("EXTRA_FECHA_FIN") ?: ""
        montoInicial = intent.getDoubleExtra("EXTRA_MONTO_INICIAL", 0.0)
        listaZonasIds = intent.getIntegerArrayListExtra("EXTRA_ZONAS_IDS") ?: ArrayList()

        initViews()
        generarReporte()
    }

    private fun initViews() {
        tvFondoInicial = findViewById(R.id.tvFondoInicial)
        tvTotalEfectivo = findViewById(R.id.tvTotalEfectivo)
        tvTotalCaja = findViewById(R.id.tvTotalCaja)
        tvTotalTarjeta = findViewById(R.id.tvTotalTarjeta)

        tvGranTotalVentas = findViewById(R.id.tvGranTotalVentas)
        tvCostoProduccion = findViewById(R.id.tvCostoProduccion)
        tvGanancia = findViewById(R.id.tvGanancia)

        rvDescuentos = findViewById(R.id.rvDescuentos)
        rvDescuentos.layoutManager = LinearLayoutManager(this)

        rvCancelados = findViewById(R.id.rvCancelados)
        rvCancelados.layoutManager = LinearLayoutManager(this)

        rvTodos = findViewById(R.id.rvTodosLosPedidos)
        rvTodos.layoutManager = LinearLayoutManager(this)

        switchDesglose = findViewById(R.id.switchDesglose)
        switchDesglose.setOnCheckedChangeListener { _, isChecked ->
            rvTodos.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        findViewById<Button>(R.id.btnCerrar).setOnClickListener { finish() }
    }

    private fun generarReporte() {
        lifecycleScope.launch {
            val ventasBrutas = ventasRepo.getVentasPorRango(fechaInicio, fechaFin)

            val ventasFiltradas = if (listaZonasIds.isEmpty()) {
                ventasBrutas
            } else {
                ventasBrutas.filter { venta ->
                    venta.detalles?.any { detalle ->
                        val zonaProd = detalle.producto?.id_zona_produccion ?: 0
                        listaZonasIds.contains(zonaProd)
                    } ?: false
                }
            }

            calcularTotales(ventasFiltradas)
        }
    }

    private fun calcularTotales(ventas: List<Venta>) {
        var sumaEfectivoVentas = 0.0
        var sumaTarjetaVentas = 0.0
        var sumaCostoProduccion = 0.0

        val listaDescuentos = ArrayList<Venta>()
        val listaCancelados = ArrayList<Venta>()

        for (venta in ventas) {
            if (venta.id_estado == 4) {
                listaCancelados.add(venta)
                continue
            }

            if ((venta.descuento_monto ?: 0.0) > 0) {
                listaDescuentos.add(venta)
            }

            if (venta.pagos.isNullOrEmpty()) continue

            val factor: Double
            if (listaZonasIds.isEmpty() || (venta.descuento_monto ?: 0.0) > 0) {
                factor = 1.0
            } else {
                val totalVentaBruto = venta.detalles?.sumOf { it.precio_unidad * it.cantidad } ?: 1.0
                val totalZona = venta.detalles?.filter {
                    listaZonasIds.contains(it.producto?.id_zona_produccion ?: 0)
                }?.sumOf { it.precio_unidad * it.cantidad } ?: 0.0

                factor = if (totalVentaBruto > 0) totalZona / totalVentaBruto else 0.0
            }

            venta.pagos.forEach { pago ->
                val montoAjustado = pago.monto * factor
                if (pago.id_metodo_pago == 1) {
                    sumaEfectivoVentas += montoAjustado
                } else if (pago.id_metodo_pago == 2) {
                    sumaTarjetaVentas += montoAjustado
                }
            }

            venta.detalles?.forEach { detalle ->
                val zonaProd = detalle.producto?.id_zona_produccion ?: 0

                if (listaZonasIds.isEmpty() || listaZonasIds.contains(zonaProd)) {
                    sumaCostoProduccion += (detalle.costo_unidad * detalle.cantidad)
                }
            }
        }

        val totalEnCaja = montoInicial + sumaEfectivoVentas

        val granTotalVentas = sumaEfectivoVentas + sumaTarjetaVentas
        val ganancia = granTotalVentas - sumaCostoProduccion

        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        tvFondoInicial.text = "Fondo Inicial: ${format.format(montoInicial)}"
        tvTotalEfectivo.text = "Ventas Efectivo: ${format.format(sumaEfectivoVentas)}"
        tvTotalCaja.text = "TOTAL EN CAJA: ${format.format(totalEnCaja)}"
        tvTotalTarjeta.text = "Ventas Tarjeta: ${format.format(sumaTarjetaVentas)}"

        tvGranTotalVentas.text = "Total (Efectivo + Tarjeta): ${format.format(granTotalVentas)}"
        tvCostoProduccion.text = "Costos de Producción: ${format.format(sumaCostoProduccion)}"
        tvGanancia.text = "Ganancias: ${format.format(ganancia)}"

        rvDescuentos.adapter = ReporteAdapter(listaDescuentos, "DESCUENTO", listaZonasIds)
        rvCancelados.adapter = ReporteAdapter(listaCancelados, "CANCELADO", listaZonasIds)
        rvTodos.adapter = ReporteAdapter(ventas, "NORMAL", listaZonasIds)
    }
}