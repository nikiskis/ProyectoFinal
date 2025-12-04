package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.adapter.PedidosAdapter
import com.example.proyectofinal.models.VentaInsert
import com.example.proyectofinal.repositories.VentasRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Pedido : AppCompatActivity() {

    private lateinit var pedidosRecyclerView: RecyclerView
    private lateinit var pedidosAdapter: PedidosAdapter
    private val ventasRepo = VentasRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido)

        setupRecyclerView()
        setupButtons()
        loadPedidos()
    }

    override fun onResume() {
        super.onResume()
        loadPedidos()
    }

    private fun setupRecyclerView() {
        pedidosRecyclerView = findViewById(R.id.pedidosRecyclerView)
        pedidosAdapter = PedidosAdapter(
            mutableListOf(),
            onPedidoClick = { venta ->
                val intent = Intent(this, DetallePedido::class.java)
                intent.putExtra("EXTRA_VENTA_ID", venta.id)

                val tipoOriginal = venta.tipo_pedido ?: ""

                val tipoFormateado = if (tipoOriginal == "Para Llevar") "Llevar" else tipoOriginal

                val nombreTitulo = "$tipoFormateado ${venta.identificador ?: ""}"
                intent.putExtra("EXTRA_NOMBRE_PEDIDO", nombreTitulo)


                startActivity(intent)
            }
        )
        pedidosRecyclerView.adapter = pedidosAdapter
        pedidosRecyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnAgregarPedido).setOnClickListener {
            showAddPedidoDialog()
        }

        findViewById<Button>(R.id.btnRegresar).setOnClickListener {
            finish()
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

                if (identificador.isNotBlank()) {
                    createPedido(tipo, identificador)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createPedido(tipo: String, identificador: String) {
        lifecycleScope.launch {
            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val nuevaVenta = VentaInsert(
                fecha = fechaActual,
                id_estado = 1,
                tipo_pedido = tipo,
                identificador = identificador
            )
            ventasRepo.crearVenta(nuevaVenta)
            loadPedidos()
        }
    }
}