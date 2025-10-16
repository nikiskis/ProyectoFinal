package com.example.proyectofinal


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.Adapter.IngredientesAdapter
import com.example.proyectofinal.Models.Ingrediente
import com.example.proyectofinal.Models.IngredienteInsert
import com.example.proyectofinal.Models.IngredienteUpdate
import com.example.proyectofinal.repositories.IngredientesRepository
import kotlinx.coroutines.launch

class Ingredientes : AppCompatActivity() {

    private lateinit var ingredientesRecyclerView: RecyclerView
    private lateinit var ingredientesAdapter: IngredientesAdapter
    private val ingredientesRepo = IngredientesRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredientes)

        setupRecyclerView()
        loadIngredientes()
        setupButtons()
    }


    private fun setupRecyclerView() {
        ingredientesRecyclerView = findViewById(R.id.ingredientesRecyclerView)
        ingredientesAdapter = IngredientesAdapter(
            mutableListOf(),
            onEditClick = { ingrediente -> showAddOrEditIngredienteDialog(ingrediente) },
            onDeleteClick = { ingrediente -> showDeleteConfirmationDialog(ingrediente) }
        )

        ingredientesRecyclerView.adapter = ingredientesAdapter
        ingredientesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadIngredientes() {
        lifecycleScope.launch {
            try {
                val ingredientesFromDB = ingredientesRepo.getIngredientes()
                ingredientesAdapter.updateData(ingredientesFromDB)
            } catch (e: Exception) {
                Log.e("IngredientesActivity", "Error al cargar ingredientes", e)
            }
        }
    }

    private fun setupButtons() {


        val botonRegresar = findViewById<Button>(R.id.btnRegresar)
        botonRegresar.setOnClickListener {
            val intent = Intent(this, Articulos::class.java)
            startActivity(intent)
        }

        val botonAgregar = findViewById<Button>(R.id.btnAgregar)

        botonAgregar.setOnClickListener {
            showAddOrEditIngredienteDialog(null)
        }
    }

    private fun showAddOrEditIngredienteDialog(ingrediente: Ingrediente?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_ingrediente, null)

        val nombreEditText = dialogView.findViewById<EditText>(R.id.nombreEditText)
        val costoEditText = dialogView.findViewById<EditText>(R.id.costoEditText)
        val estadoSpinner = dialogView.findViewById<Spinner>(R.id.estadoSpinner)

        val estados = listOf("Activo", "Inactivo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = adapter

        val dialogTitle: String
        if (ingrediente != null) {
            dialogTitle = "Editar Ingrediente"
            nombreEditText.setText(ingrediente.nombre)
            costoEditText.setText(ingrediente.costo.toString())
            val estadoPosition = if (ingrediente.id_estado == 1) 0 else 1
            estadoSpinner.setSelection(estadoPosition)
        } else {
            dialogTitle = "Agregar Nuevo Ingrediente"
        }

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(if (ingrediente == null) "Agregar" else "Guardar") { _, _ ->
                val nombre = nombreEditText.text.toString()
                val costo = costoEditText.text.toString().toDoubleOrNull()
                val estadoSeleccionado = estadoSpinner.selectedItem.toString()
                val estadoId = if (estadoSeleccionado == "Activo") 1 else 2

                if (nombre.isNotBlank() && costo != null) {
                    lifecycleScope.launch {
                        if (ingrediente == null) {
                            val nuevoIngrediente = IngredienteInsert(nombre, costo, estadoId)
                            ingredientesRepo.crearIngrediente(nuevoIngrediente)
                        } else {
                            val ingredienteActualizado = IngredienteUpdate(nombre, costo, estadoId)
                            ingredientesRepo.updateIngrediente(ingrediente.id, ingredienteActualizado)
                        }
                        loadIngredientes()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(ingrediente: Ingrediente) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Ingrediente")
            .setMessage("¿Estás seguro de que quieres eliminar '${ingrediente.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    ingredientesRepo.deleteIngrediente(ingrediente.id)
                    loadIngredientes()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}