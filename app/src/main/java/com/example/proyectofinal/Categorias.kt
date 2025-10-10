package com.example.proyectofinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.Adapter.Categoria
import com.example.proyectofinal.Adapter.CategoriasAdapter
import com.example.proyectofinal.repositories.CategoriasRepository
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class Categorias : AppCompatActivity() { // Nombre de la clase cambiado

    private lateinit var categoriasRecyclerView: RecyclerView
    private lateinit var categoriasAdapter: CategoriasAdapter
    private val categoriasRepo = CategoriasRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        setupRecyclerView()
        loadCategorias()

        val botonAgregar = findViewById<Button>(R.id.btnAgregar)
        botonAgregar.setOnClickListener {
            showAddCategoriaDialog()
        }

        val botonRegresar = findViewById<Button>(R.id.btnRegresar)
        botonRegresar.setOnClickListener {
            val intent = Intent(this, Articulos::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        categoriasRecyclerView = findViewById(R.id.categoriasRecyclerView)
        categoriasAdapter = CategoriasAdapter(mutableListOf(), this)
        categoriasRecyclerView.adapter = categoriasAdapter
        categoriasRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadCategorias() {
        lifecycleScope.launch {
            try {

                val response = SupabaseClient.client.postgrest["Categoria_Producto"].select()
                val categoriasFromDB = response.decodeList<Categoria>()
                categoriasAdapter.updateData(categoriasFromDB)
            } catch (e: Exception) {
                Log.e("CategoriasActivity", "Error al cargar las categorías", e)
            }
        }
    }

    private fun showAddCategoriaDialog() {
        val editText = EditText(this).apply {
            hint = "Nombre de la nueva categoría"
        }

        AlertDialog.Builder(this)
            .setTitle("Agregar Nueva Categoría")
            .setView(editText)
            .setPositiveButton("Agregar") { _, _ ->
                val nombreCategoria = editText.text.toString()
                if (nombreCategoria.isNotBlank()) {
                    lifecycleScope.launch {
                        categoriasRepo.crearCategoria(nombreCategoria)
                        loadCategorias()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}