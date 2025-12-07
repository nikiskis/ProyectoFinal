package com.example.proyectofinal

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.adapter.MenuExpandibleAdapter
import com.example.proyectofinal.adapter.TicketAdapter
import com.example.proyectofinal.models.Articulo
import com.example.proyectofinal.models.CategoriaExpandible
import com.example.proyectofinal.models.DetalleVenta
import com.example.proyectofinal.models.DetalleVentaInsert
import com.example.proyectofinal.repositories.ArticulosRepository
import com.example.proyectofinal.repositories.CategoriasRepository
import com.example.proyectofinal.repositories.VentasRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DetallePedido : AppCompatActivity() {

    private var ventaId: Int = -1
    private var nombrePedido: String = ""
    private lateinit var rvMenuExpandible: RecyclerView
    private lateinit var rvTicket: RecyclerView
    private lateinit var tvTotal: TextView
    private val categoriasRepo = CategoriasRepository()
    private val articulosRepo = ArticulosRepository()
    private val ventasRepo = VentasRepository()

    private lateinit var ticketAdapter: TicketAdapter
    private lateinit var menuAdapter: MenuExpandibleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_pedido)

        ventaId = intent.getIntExtra("EXTRA_VENTA_ID", -1)
        nombrePedido = intent.getStringExtra("EXTRA_NOMBRE_PEDIDO") ?: "Pedido"

        val tvTitulo = findViewById<TextView>(R.id.tvTituloMesa)
        tvTitulo.text = nombrePedido
        tvTotal = findViewById(R.id.tvTotal)

        setupRecyclerViews()
        setupListeners()
        loadData()
    }

    private fun setupRecyclerViews() {
        rvMenuExpandible = findViewById(R.id.rvMenuExpandible)
        menuAdapter = MenuExpandibleAdapter(emptyList()) { articulo ->
            agregarProductoATicket(articulo)
        }
        rvMenuExpandible.adapter = menuAdapter
        rvMenuExpandible.layoutManager = LinearLayoutManager(this)

        rvTicket = findViewById(R.id.rvTicket)
        ticketAdapter = TicketAdapter(mutableListOf()) { detalle ->
            showEditNotaDialog(detalle)
        }
        rvTicket.adapter = ticketAdapter
        rvTicket.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnAtras).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnPagar).setOnClickListener {
            showOpcionesDialog()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            val categorias = categoriasRepo.getCategorias()
            val todosLosProductos = articulosRepo.getArticulos()

            val listaExpandible = ArrayList<CategoriaExpandible>()

            for (cat in categorias) {
                val productosDeCategoria = todosLosProductos.filter {
                    it.id_categoria_producto == cat.id && it.id_estado == 1
                }

                if (productosDeCategoria.isNotEmpty()) {
                    listaExpandible.add(
                        CategoriaExpandible(
                            categoria = cat,
                            productos = productosDeCategoria,
                            isExpanded = false
                        )
                    )
                }
            }

            if (listaExpandible.isNotEmpty()) {
                listaExpandible[0].isExpanded = true
            }

            menuAdapter.updateData(listaExpandible)

            loadTicket()
        }
    }

    private fun loadTicket() {
        lifecycleScope.launch {
            val detalles = ventasRepo.getDetallesVenta(ventaId)
            ticketAdapter.updateData(detalles)

            // Calcula total (cantidad * precio)
            val total = detalles.sumOf { it.cantidad * it.precio_unidad }
            val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            tvTotal.text = "Total: ${format.format(total)}"
        }
    }

    // --- AQUÍ ESTABA EL ERROR, YA CORREGIDO ---
    private fun agregarProductoATicket(articulo: Articulo) {
        lifecycleScope.launch {
            // 1. Obtenemos lo que ya hay en el ticket
            val detallesActuales = ventasRepo.getDetallesVenta(ventaId)

            // 2. Buscamos si este producto ya existe comparando SOLAMENTE el ID del producto anidado
            // CORRECCIÓN: Se eliminó "|| it.id_producto == articulo.id" que causaba el error
            val detalleExistente = detallesActuales.find { it.producto?.id == articulo.id }

            if (detalleExistente != null) {
                // 3. Si existe, actualizamos la cantidad (+1)
                val nuevaCantidad = detalleExistente.cantidad + 1
                ventasRepo.updateCantidadDetalle(detalleExistente.id, nuevaCantidad)
            } else {
                // 4. Si no existe, lo creamos como nuevo
                val costoIngredientes = articulo.producto_ingrediente.sumOf {
                    it.ingrediente.costo * it.cantidad
                }

                val detalle = DetalleVentaInsert(
                    id_venta = ventaId,
                    id_producto = articulo.id,
                    cantidad = 1,
                    precio_unidad = articulo.precio,
                    costo_unidad = costoIngredientes,
                    notas = ""
                )
                ventasRepo.agregarDetalle(detalle)
            }
            // 5. Recargamos la lista visual
            loadTicket()
        }
    }

    private fun showEditNotaDialog(detalle: DetalleVenta) {
        val context = this
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        // Input para Cantidad
        val tvLabelCant = TextView(context)
        tvLabelCant.text = "Cantidad:"
        layout.addView(tvLabelCant)

        val inputCantidad = EditText(context)
        inputCantidad.inputType = InputType.TYPE_CLASS_NUMBER
        inputCantidad.setText(detalle.cantidad.toString())
        inputCantidad.gravity = Gravity.CENTER
        layout.addView(inputCantidad)

        // Input para Notas
        val tvLabelNota = TextView(context)
        tvLabelNota.text = "Notas:"
        tvLabelNota.setPadding(0, 20, 0, 0)
        layout.addView(tvLabelNota)

        val inputNota = EditText(context)
        inputNota.setText(detalle.notas)
        inputNota.hint = "Sin cebolla, extra salsa..."
        layout.addView(inputNota)

        AlertDialog.Builder(context)
            .setTitle(detalle.producto?.nombre ?: "Editar Producto")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaNota = inputNota.text.toString()
                val nuevaCantidad = inputCantidad.text.toString().toIntOrNull() ?: 0

                lifecycleScope.launch {
                    if (nuevaCantidad > 0) {
                        // Actualizamos cantidad y nota
                        ventasRepo.updateCantidadDetalle(detalle.id, nuevaCantidad)
                        ventasRepo.updateNotaDetalle(detalle.id, nuevaNota)
                    } else {
                        // Si pone 0, se borra
                        ventasRepo.eliminarDetalle(detalle.id)
                    }
                    loadTicket()
                }
            }
            .setNeutralButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    ventasRepo.eliminarDetalle(detalle.id)
                    loadTicket()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showOpcionesDialog() {
        val opciones = arrayOf("Pagar", "Cancelar Pedido")

        AlertDialog.Builder(this)
            .setTitle("Seleccione una acción")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> procederAlPago()
                    1 -> showPasswordDialog()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun showPasswordDialog() {
        val input = EditText(this)
        input.hint = "Contraseña Administrativa"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER

        AlertDialog.Builder(this)
            .setTitle("Confirmar Cancelación")
            .setMessage("Ingrese contraseña para cancelar el pedido:")
            .setView(input)
            .setPositiveButton("Aceptar") { dialog, _ ->
                val password = input.text.toString()
                if (password == "1234") {
                    lifecycleScope.launch {
                        ventasRepo.cancelarVenta(ventaId)
                        finish()
                    }
                } else {
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun procederAlPago() {
        lifecycleScope.launch {
            val detalles = ventasRepo.getDetallesVenta(ventaId)
            // Calculamos el total considerando cantidades
            val totalActual = detalles.sumOf { it.cantidad * it.precio_unidad }

            val intent = android.content.Intent(this@DetallePedido, Pagos::class.java)
            intent.putExtra("EXTRA_VENTA_ID", ventaId)
            intent.putExtra("EXTRA_TOTAL_ORIGINAL", totalActual)
            startActivity(intent)
        }
    }
}