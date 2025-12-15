package com.example.proyectofinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.proyectofinal.adapter.ProveedoresAdapter
import com.example.proyectofinal.models.Proveedor
import com.example.proyectofinal.models.ProveedorInsert
import com.example.proyectofinal.repositories.IngredientesRepository
import com.example.proyectofinal.repositories.ProveedoresRepository
import kotlinx.coroutines.launch

class Proveedores : AppCompatActivity() {

    private val proveedoresRepo = ProveedoresRepository()
    private val ingredientesRepo = IngredientesRepository()
    private lateinit var adapter: ProveedoresAdapter
    private lateinit var rvProveedores: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proveedores)

        setupRecycler()
        cargarDatos()

        findViewById<Button>(R.id.btnRegresarProv).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnAgregarProv).setOnClickListener {
            showDialogoGestion(null)
        }
    }

    private fun setupRecycler() {
        rvProveedores = findViewById(R.id.rvProveedores)
        adapter = ProveedoresAdapter(mutableListOf(),
            onEdit = { prov -> showDialogoGestion(prov) },
            onDelete = { prov -> showDialogoEliminar(prov) },
            onLink = { prov -> showDialogoLink(prov) },
            onCall = { telefono -> realizarLlamada(telefono) }
        )
        rvProveedores.adapter = adapter
        rvProveedores.layoutManager = LinearLayoutManager(this)
    }

    private fun realizarLlamada(telefono: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$telefono")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir el teléfono", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            val lista = proveedoresRepo.getProveedores()
            adapter.updateData(lista)
        }
    }

    private fun showDialogoGestion(proveedor: Proveedor?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_proveedor, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombreProv)
        val etEmpresa = view.findViewById<EditText>(R.id.etEmpresaProv)
        val etTel = view.findViewById<EditText>(R.id.etTelProv)

        if (proveedor != null) {
            etNombre.setText(proveedor.nombre)
            etEmpresa.setText(proveedor.empresa)
            etTel.setText(proveedor.telefono)
        }

        AlertDialog.Builder(this)
            .setTitle(if (proveedor == null) "Nuevo Proveedor" else "Editar Proveedor")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nom = etNombre.text.toString()
                val emp = etEmpresa.text.toString().ifBlank { null }
                val tel = etTel.text.toString()

                if (nom.isNotBlank() && tel.isNotBlank()) {
                    lifecycleScope.launch {
                        val obj = ProveedorInsert(nom, emp, tel)
                        if (proveedor == null) {
                            proveedoresRepo.crearProveedor(obj)
                        } else {
                            proveedoresRepo.updateProveedor(proveedor.id, obj)
                        }
                        cargarDatos()
                        Toast.makeText(this@Proveedores, "Guardado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Nombre y Teléfono requeridos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDialogoEliminar(prov: Proveedor) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Proveedor")
            .setMessage("¿Eliminar a ${prov.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    proveedoresRepo.deleteProveedor(prov.id)
                    cargarDatos()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }


    private fun showDialogoLink(prov: Proveedor) {
        lifecycleScope.launch {
            val todosIngredientes = ingredientesRepo.getIngredientesActivos()
            if (todosIngredientes.isEmpty()) {
                Toast.makeText(this@Proveedores, "No hay ingredientes registrados", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val nombresArray = todosIngredientes.map { it.nombre }.toTypedArray()
            val checkedItems = BooleanArray(todosIngredientes.size)
            val selectedIds = ArrayList<Int>()

            val actualesIds = prov.relaciones?.map { it.ingrediente.id } ?: emptyList()

            todosIngredientes.forEachIndexed { index, ing ->
                if (actualesIds.contains(ing.id)) {
                    checkedItems[index] = true
                    selectedIds.add(ing.id)
                }
            }

            AlertDialog.Builder(this@Proveedores)
                .setTitle("Ingredientes de ${prov.nombre}")
                .setMultiChoiceItems(nombresArray, checkedItems) { _, which, isChecked ->
                    val idIng = todosIngredientes[which].id
                    if (isChecked) {
                        selectedIds.add(idIng)
                    } else {
                        selectedIds.remove(idIng)
                    }
                }
                .setPositiveButton("Guardar") { _, _ ->
                    lifecycleScope.launch {
                        proveedoresRepo.syncIngredientes(prov.id, selectedIds)

                        cargarDatos() // Recargar lista para ver cambios
                        Toast.makeText(this@Proveedores, "Lista actualizada", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}