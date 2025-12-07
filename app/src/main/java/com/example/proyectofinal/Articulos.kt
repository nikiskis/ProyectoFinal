package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.adapter.AdminExpandibleAdapter
import com.example.proyectofinal.models.Articulo
import com.example.proyectofinal.models.ArticuloInsert
import com.example.proyectofinal.models.CategoriaExpandible
import com.example.proyectofinal.models.Estado
import com.example.proyectofinal.repositories.ArticulosRepository
import com.example.proyectofinal.repositories.CategoriasRepository
import com.example.proyectofinal.repositories.ZonasRepository
import kotlinx.coroutines.launch

class Articulos : AppCompatActivity() {

    private lateinit var articulosRecyclerView: RecyclerView
    private lateinit var adminAdapter: AdminExpandibleAdapter

    private val articulosRepo = ArticulosRepository()
    private val zonasRepo = ZonasRepository()
    private val categoriasRepo = CategoriasRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articulos)

        setupRecyclerView()
        setupButtons()
        loadArticulos()
    }

    private fun setupRecyclerView() {
        articulosRecyclerView = findViewById(R.id.articulosRecyclerView)

        adminAdapter = AdminExpandibleAdapter(
            emptyList(),
            onEditClick = { articulo -> showAddOrEditArticuloDialog(articulo) },
            onDeleteClick = { articulo -> showDeleteConfirmationDialog(articulo) },
            onIngredientsClick = { articulo ->
                val intent = Intent(this, GestionarIngredientesActivity::class.java)
                intent.putExtra(GestionarIngredientesActivity.EXTRA_ARTICULO_ID, articulo.id)
                intent.putExtra(GestionarIngredientesActivity.EXTRA_ARTICULO_NOMBRE, articulo.nombre)
                startActivity(intent)
            }
        )

        articulosRecyclerView.adapter = adminAdapter
        articulosRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadArticulos() {
        lifecycleScope.launch {
            try {
                val articulosFromDB = articulosRepo.getArticulos()
                val categoriasFromDB = categoriasRepo.getCategorias()

                val listaExpandible = ArrayList<CategoriaExpandible>()

                for (cat in categoriasFromDB) {
                    val productosDeCategoria = articulosFromDB.filter {
                        it.id_categoria_producto == cat.id
                    }

                    if (productosDeCategoria.isNotEmpty()) {
                        val productosOrdenados = productosDeCategoria.sortedWith(
                            compareBy<Articulo> { it.id_estado }.thenBy { it.nombre }
                        )

                        listaExpandible.add(
                            CategoriaExpandible(
                                categoria = cat,
                                productos = productosOrdenados,
                                isExpanded = false
                            )
                        )
                    }
                }

                adminAdapter.updateData(listaExpandible)

            } catch (e: Exception) {
                Log.e("ArticulosActivity", "Error al cargar artículos", e)
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnAgregar).setOnClickListener {
            showAddOrEditArticuloDialog(null)
        }

        findViewById<Button>(R.id.btnZonas).setOnClickListener {
            val intent = Intent(this, Zonas::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnCategorias).setOnClickListener {
            val intent = Intent(this, Categorias::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnRegresar).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnIngredientes).setOnClickListener {
            val intent = Intent(this, Ingredientes::class.java)
            startActivity(intent)
        }
    }

    private fun showAddOrEditArticuloDialog(articulo: Articulo?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_articulo, null)
        val nombreEditText = dialogView.findViewById<EditText>(R.id.nombreEditText)
        val precioEditText = dialogView.findViewById<EditText>(R.id.precioEditText)
        val estadoSpinner = dialogView.findViewById<Spinner>(R.id.estadoSpinner)
        val zonaSpinner = dialogView.findViewById<Spinner>(R.id.zonaSpinner)
        val categoriaSpinner = dialogView.findViewById<Spinner>(R.id.categoriaSpinner)

        val dialogTitle = if (articulo == null) "Agregar Nuevo Artículo" else "Editar Artículo"

        lifecycleScope.launch {
            val estadosList = listOf(
                Estado(id = 1, estado = "Activo"),
                Estado(id = 2, estado = "Inactivo")
            )

            val zonasList = zonasRepo.getZonas()
            val categoriasList = categoriasRepo.getCategorias()

            setupSpinner(estadoSpinner, estadosList.map { it.estado })
            setupSpinner(zonaSpinner, zonasList.map { it.nombre })
            setupSpinner(categoriaSpinner, categoriasList.map { it.nombre })

            if (articulo != null) {
                nombreEditText.setText(articulo.nombre)
                precioEditText.setText(articulo.precio.toString())
                estadoSpinner.setSelection(estadosList.indexOfFirst { it.id == articulo.id_estado })
                zonaSpinner.setSelection(zonasList.indexOfFirst { it.id == articulo.id_zona_produccion })
                categoriaSpinner.setSelection(categoriasList.indexOfFirst { it.id == articulo.id_categoria_producto })
            }

            AlertDialog.Builder(this@Articulos)
                .setTitle(dialogTitle)
                .setView(dialogView)
                .setPositiveButton(if (articulo == null) "Agregar" else "Guardar") { _, _ ->
                    val nombre = nombreEditText.text.toString()
                    val precio = precioEditText.text.toString().toDoubleOrNull()

                    val selectedEstado = estadosList.find { it.estado == estadoSpinner.selectedItem.toString() }
                    val selectedZona = zonasList.find { it.nombre == zonaSpinner.selectedItem.toString() }
                    val selectedCategoria = categoriasList.find { it.nombre == categoriaSpinner.selectedItem.toString() }

                    if (nombre.isNotBlank() && precio != null && selectedEstado != null && selectedZona != null && selectedCategoria != null) {
                        val articuloData = ArticuloInsert(
                            nombre = nombre,
                            precio = precio,
                            id_estado = selectedEstado.id,
                            id_zona_produccion = selectedZona.id,
                            id_categoria_producto = selectedCategoria.id
                        )

                        lifecycleScope.launch {
                            if (articulo == null) {
                                articulosRepo.crearArticulo(articuloData)
                            } else {
                                articulosRepo.updateArticulo(articulo.id, articuloData)
                            }
                            loadArticulos()
                        }
                    } else {
                        Toast.makeText(this@Articulos, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun setupSpinner(spinner: Spinner, data: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun showDeleteConfirmationDialog(articulo: Articulo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Artículo")
            .setMessage("¿Estás seguro de que quieres eliminar '${articulo.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    articulosRepo.deleteArticulo(articulo.id)
                    loadArticulos()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}