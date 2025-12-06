package com.example.proyectofinal

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.adapter.PedidosAdapter
import com.example.proyectofinal.models.Venta
import com.example.proyectofinal.models.VentaInsert
import com.example.proyectofinal.repositories.VentasRepository
import com.example.proyectofinal.repositories.ZonasRepository
import com.example.proyectofinal.utils.PrinterPreferences
import com.example.proyectofinal.utils.TicketPrinter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Pedido : AppCompatActivity() {

    private lateinit var pedidosRecyclerView: RecyclerView
    private lateinit var pedidosAdapter: PedidosAdapter

    private val ventasRepo = VentasRepository()
    private val zonasRepo = ZonasRepository()

    private lateinit var ticketPrinter: TicketPrinter
    private lateinit var printerPrefs: PrinterPreferences

    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido)

        ticketPrinter = TicketPrinter(this)
        printerPrefs = PrinterPreferences(this)

        setupRecyclerView()
        setupButtons()
        checkAndRequestPermissions()
        loadPedidos()
    }

    override fun onResume() {
        super.onResume()
        loadPedidos()
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsNeeded = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
            return false
        }
        return true
    }

    private fun setupRecyclerView() {
        pedidosRecyclerView = findViewById(R.id.pedidosRecyclerView)
        pedidosAdapter = PedidosAdapter(
            mutableListOf(),
            onPedidoClick = { venta ->
                val intent = Intent(this, DetallePedido::class.java)
                intent.putExtra("EXTRA_VENTA_ID", venta.id)
                val tipo = if (venta.tipo_pedido == "Para Llevar") "Llevar" else venta.tipo_pedido ?: ""
                intent.putExtra("EXTRA_NOMBRE_PEDIDO", "$tipo ${venta.identificador ?: ""}")
                startActivity(intent)
            },
            onPrintClick = { venta ->
                if (checkAndRequestPermissions()) {
                    showPrintOptionsDialog(venta)
                } else {
                    Toast.makeText(this, "Acepta los permisos para imprimir", Toast.LENGTH_SHORT).show()
                }
            }
        )
        pedidosRecyclerView.adapter = pedidosAdapter
        pedidosRecyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnAgregarPedido).setOnClickListener { showAddPedidoDialog() }
        findViewById<Button>(R.id.btnRegresar).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnConfiguracion).setOnClickListener {
            if (checkAndRequestPermissions()) {
                showConfiguracionImpresoras()
            } else {
                Toast.makeText(this, "Permisos requeridos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPedidos() {
        lifecycleScope.launch {
            try {
                val ventasActivas = ventasRepo.getVentasActivas()
                pedidosAdapter.updateData(ventasActivas)
            } catch (e: Exception) {
                Log.e("Pedido", "Error loading pedidos", e)
            }
        }
    }

    private fun showConfiguracionImpresoras() {
        lifecycleScope.launch {
            val zonas = zonasRepo.getZonas()
            val bluetoothDevices = getPairedPrinters()
            val deviceNames = bluetoothDevices.map { "${it.name} (${it.address})" }
            val spinnerItems = mutableListOf("Ninguna")
            spinnerItems.addAll(deviceNames)

            val context = this@Pedido
            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 40, 50, 40)

            val spinnersMap = mutableMapOf<Int, Spinner>()
            addConfigRow(layout, spinnersMap, 0, "Caja Principal (Ticket Cliente)", spinnerItems, bluetoothDevices)
            zonas.forEach { zona ->
                addConfigRow(layout, spinnersMap, zona.id, "Zona: ${zona.nombre}", spinnerItems, bluetoothDevices)
            }

            AlertDialog.Builder(context)
                .setTitle("Configurar Impresoras")
                .setView(layout)
                .setPositiveButton("Guardar") { _, _ ->
                    savePrinterConfig(spinnersMap, bluetoothDevices)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun addConfigRow(layout: LinearLayout, spinnersMap: MutableMap<Int, Spinner>, zonaId: Int, titulo: String, spinnerItems: List<String>, bluetoothDevices: List<android.bluetooth.BluetoothDevice>) {
        val tv = TextView(this)
        tv.text = titulo
        tv.textSize = 16f
        tv.setPadding(0, 20, 0, 10)
        layout.addView(tv)
        val spinner = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        layout.addView(spinner)
        val savedMac = printerPrefs.obtenerImpresoraZona(zonaId)
        if (savedMac != null) {
            val index = bluetoothDevices.indexOfFirst { it.address == savedMac }
            if (index != -1) spinner.setSelection(index + 1)
        }
        spinnersMap[zonaId] = spinner
    }

    private fun savePrinterConfig(spinnersMap: Map<Int, Spinner>, bluetoothDevices: List<android.bluetooth.BluetoothDevice>) {
        // CORRECCIÓN 1: Usamos 'for' normal para evitar error de inferencia
        for (entry in spinnersMap) {
            val zonaId = entry.key
            val spinner = entry.value
            val position = spinner.selectedItemPosition
            if (position > 0) {
                val device = bluetoothDevices[position - 1]
                printerPrefs.guardarImpresoraZona(zonaId, device.address)
            } else {
                printerPrefs.guardarImpresoraZona(zonaId, "")
            }
        }
        Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
    }

    private fun getPairedPrinters(): List<android.bluetooth.BluetoothDevice> {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        val devices = mutableListOf<android.bluetooth.BluetoothDevice>()
        if (adapter != null && adapter.isEnabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                devices.addAll(adapter.bondedDevices)
            }
        }
        return devices
    }

    private fun showPrintOptionsDialog(venta: Venta) {
        val options = arrayOf("Cliente (Ticket de Cobro)", "Preparación (Cocina/Barra)", "Reimpresión (Copia)")
        AlertDialog.Builder(this)
            .setTitle("Imprimir Ticket - ${venta.tipo_pedido} ${venta.identificador}")
            .setItems(options) { _, which ->
                val tipo = when (which) {
                    0 -> "Cliente"
                    1 -> "Preparacion"
                    2 -> "Reimpresion"
                    else -> ""
                }
                executePrint(venta, tipo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun executePrint(venta: Venta, tipo: String) {
        lifecycleScope.launch {
            try {
                // 1. Obtener detalles
                val detalles = ventasRepo.getDetallesVenta(venta.id)

                if (detalles.isEmpty()) {
                    Toast.makeText(this@Pedido, "Pedido vacío", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (tipo == "Preparacion") {
                    // Agrupar por zona
                    val detallesPorZona = detalles.groupBy { it.producto?.id_zona_produccion }
                    var impresionesEnviadas = 0

                    // CORRECCIÓN 2: Usamos 'for' normal para evitar error de inferencia
                    for (entry in detallesPorZona) {
                        val zonaId = entry.key
                        val listaProductos = entry.value

                        if (zonaId != null) {
                            val macZona = printerPrefs.obtenerImpresoraZona(zonaId)
                            if (!macZona.isNullOrEmpty()) {
                                Toast.makeText(this@Pedido, "Imprimiendo zona ID $zonaId...", Toast.LENGTH_SHORT).show()
                                ticketPrinter.imprimir(venta, listaProductos, "Preparacion", macZona)
                                impresionesEnviadas++
                                delay(2000)
                            } else {
                                Log.w("Pedido", "Sin impresora para zona $zonaId")
                            }
                        }
                    }

                    if (impresionesEnviadas == 0) {
                        Toast.makeText(this@Pedido, "No hay impresoras de zona configuradas", Toast.LENGTH_LONG).show()
                    }

                } else {
                    // Lógica de Cliente / Caja
                    val macCaja = printerPrefs.obtenerImpresoraCaja()
                    if (!macCaja.isNullOrEmpty()) {
                        Toast.makeText(this@Pedido, "Imprimiendo Ticket...", Toast.LENGTH_SHORT).show()
                        ticketPrinter.imprimir(venta, detalles, tipo, macCaja)
                    } else {
                        Toast.makeText(this@Pedido, "Configura impresora de Caja", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Pedido", "Error impresión", e)
                Toast.makeText(this@Pedido, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddPedidoDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_pedido, null)
        val spinnerTipo = dialogView.findViewById<Spinner>(R.id.spinnerTipoPedido)
        val etIdentificador = dialogView.findViewById<EditText>(R.id.etIdentificador)
        val tipos = listOf("Mesa", "Para Llevar", "Terraza", "Domicilio")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = adapter
        AlertDialog.Builder(this)
            .setTitle("Nuevo Pedido")
            .setView(dialogView)
            .setPositiveButton("Crear") { _, _ ->
                val tipo = spinnerTipo.selectedItem.toString()
                val identificador = etIdentificador.text.toString()
                if (identificador.isNotBlank()) createPedido(tipo, identificador)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createPedido(tipo: String, identificador: String) {
        lifecycleScope.launch {
            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val nuevaVenta = VentaInsert(fecha = fechaActual, id_estado = 1, tipo_pedido = tipo, identificador = identificador)
            ventasRepo.crearVenta(nuevaVenta)
            loadPedidos()
        }
    }
}