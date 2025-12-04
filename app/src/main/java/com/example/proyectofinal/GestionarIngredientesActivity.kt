package com.example.proyectofinal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.adapter.GestionarIngredientesAdapter
import com.example.proyectofinal.models.ArticuloIngredienteInsert
import com.example.proyectofinal.models.Ingrediente
import com.example.proyectofinal.repositories.ArticulosRepository
import com.example.proyectofinal.repositories.IngredientesRepository
import kotlinx.coroutines.launch

class GestionarIngredientesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ARTICULO_ID = "extra_articulo_id"
        const val EXTRA_ARTICULO_NOMBRE = "extra_articulo_nombre"
    }

    private var articuloId: Int = -1
    private lateinit var tvTituloArticulo: TextView
    private lateinit var rvIngredientesArticulo: RecyclerView
    private lateinit var gestionarAdapter: GestionarIngredientesAdapter

    private val articulosRepo = ArticulosRepository()
    private val ingredientesRepo = IngredientesRepository()

    private var listaIngredientesDisponibles: List<Ingrediente> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_ingredientes)

        articuloId = intent.getIntExtra(EXTRA_ARTICULO_ID, -1)
        val articuloNombre = intent.getStringExtra(EXTRA_ARTICULO_NOMBRE) ?: "Artículo Desconocido"

        if (articuloId == -1) {
            Toast.makeText(this, "Error: ID de artículo no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        tvTituloArticulo = findViewById(R.id.tvTituloArticulo)
        tvTituloArticulo.text = "Ingredientes de: $articuloNombre"
        rvIngredientesArticulo = findViewById(R.id.rvIngredientesArticulo)

        setupRecyclerView()
        setupButtons()

        loadIngredientesDelArticulo()
        loadIngredientesDisponibles()
    }

    private fun setupRecyclerView() {
        gestionarAdapter = GestionarIngredientesAdapter(mutableListOf()) { relacion ->
            showDeleteConfirmationDialog(relacion.id)
        }
        rvIngredientesArticulo.adapter = gestionarAdapter
        rvIngredientesArticulo.layoutManager = LinearLayoutManager(this)
    }

    private fun loadIngredientesDelArticulo() {
        lifecycleScope.launch {
            try {
                val articulos = articulosRepo.getArticulos()
                val articuloActual = articulos.find { it.id == articuloId }
                gestionarAdapter.updateData(articuloActual?.producto_ingrediente ?: emptyList())
            } catch (e: Exception) {
                Log.e("GestionarIngredientes", "Error cargando ingredientes del artículo", e)
            }
        }
    }

    private fun loadIngredientesDisponibles() {
        lifecycleScope.launch {
            try {
                listaIngredientesDisponibles = ingredientesRepo.getIngredientesActivos()
            } catch (e: Exception) {
                Log.e("GestionarIngredientes", "Error cargando ingredientes disponibles", e)
            }
        }
    }


    private fun setupButtons() {
        findViewById<Button>(R.id.btnAgregarIngrediente).setOnClickListener {
            showAddIngredienteDialog()
        }
        findViewById<Button>(R.id.btnVolverArticulos).setOnClickListener {
            finish()
        }
    }

    private fun showAddIngredienteDialog() {
        if (listaIngredientesDisponibles.isEmpty()) {
            Toast.makeText(this, "Cargando ingredientes disponibles...", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_articulo_ingrediente, null)
        val ingredienteSpinner = dialogView.findViewById<Spinner>(R.id.spinnerIngrediente)
        val cantidadEditText = dialogView.findViewById<EditText>(R.id.etCantidad)

        val nombresIngredientes = listaIngredientesDisponibles.map { it.nombre }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombresIngredientes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ingredienteSpinner.adapter = spinnerAdapter

        AlertDialog.Builder(this)
            .setTitle("Agregar Ingrediente")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val selectedNombre = ingredienteSpinner.selectedItem.toString()
                val cantidad = cantidadEditText.text.toString().toIntOrNull()
                val selectedIngrediente = listaIngredientesDisponibles.find { it.nombre == selectedNombre }

                if (cantidad != null && cantidad > 0 && selectedIngrediente != null) {
                    val nuevaRelacion = ArticuloIngredienteInsert(
                        cantidad = cantidad,
                        id_producto = articuloId,
                        id_ingrediente = selectedIngrediente.id
                    )
                    lifecycleScope.launch {
                        articulosRepo.addIngredienteToArticulo(nuevaRelacion)
                        loadIngredientesDelArticulo()
                    }
                } else {
                    Toast.makeText(this, "Selecciona un ingrediente e ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(idRelacion: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Ingrediente del Artículo")
            .setMessage("¿Estás seguro de quitar este ingrediente del artículo?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    articulosRepo.removeIngredienteFromArticulo(idRelacion)
                    loadIngredientesDelArticulo()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}