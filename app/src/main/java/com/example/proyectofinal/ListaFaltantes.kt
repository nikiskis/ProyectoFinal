package com.example.proyectofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.adapter.FaltantesAdapter
import com.example.proyectofinal.models.Faltante
import com.example.proyectofinal.models.FaltanteInsert
import com.example.proyectofinal.repositories.FaltantesRepository
import com.example.proyectofinal.utils.PrinterPreferences
import com.example.proyectofinal.utils.TicketPrinter
import kotlinx.coroutines.launch

class ListaFaltantes : AppCompatActivity() {

    private val faltantesRepo = FaltantesRepository()
    private lateinit var adapter: FaltantesAdapter
    private lateinit var rvFaltantes: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_faltantes)

        setupRecyclerView()
        setupButtons()
        cargarDatos()
    }

    private fun setupRecyclerView() {
        rvFaltantes = findViewById(R.id.rvFaltantes)
        adapter = FaltantesAdapter(mutableListOf()) { item ->
            confirmarEliminacion(item)
        }
        rvFaltantes.adapter = adapter
        rvFaltantes.layoutManager = LinearLayoutManager(this)
    }

    private fun setupButtons() {
        findViewById<android.view.View>(R.id.btnRegresar).setOnClickListener { finish() }

        findViewById<android.view.View>(R.id.btnAgregarManual).setOnClickListener {
            mostrarDialogoAgregar() // RQF41 y RQF43
        }

        findViewById<android.view.View>(R.id.btnPrintFaltantes).setOnClickListener {
            imprimirLista() // RQF42
        }

        findViewById<android.view.View>(R.id.btnLimpiarLista).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Limpiar Lista")
                .setMessage("¿Borrar todos los elementos de la lista?")
                .setPositiveButton("Sí") { _, _ ->
                    lifecycleScope.launch {
                        faltantesRepo.limpiarLista()
                        cargarDatos()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        findViewById<android.view.View>(R.id.btnIrAProveedores).setOnClickListener {
            val intent = android.content.Intent(this, Proveedores::class.java)
            startActivity(intent)
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            val lista = faltantesRepo.getFaltantes()
            adapter.updateData(lista)
        }
    }

    private fun mostrarDialogoAgregar() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_faltante, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreInsumo)
        val etCantidad = dialogView.findViewById<EditText>(R.id.etCantidadRequerida)

        AlertDialog.Builder(this)
            .setTitle("Agregar Faltante")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val nombre = etNombre.text.toString()
                val cantidad = etCantidad.text.toString()

                if (nombre.isNotBlank() && cantidad.isNotBlank()) {
                    lifecycleScope.launch {
                        faltantesRepo.agregarFaltante(FaltanteInsert(nombre, cantidad))
                        cargarDatos()
                        Toast.makeText(this@ListaFaltantes, "Agregado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Datos incompletos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminacion(item: Faltante) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Ya se compró o desea eliminar '${item.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    faltantesRepo.eliminarFaltante(item.id)
                    cargarDatos()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun imprimirLista() {
        lifecycleScope.launch {
            val lista = faltantesRepo.getFaltantes()
            if (lista.isEmpty()) {
                Toast.makeText(this@ListaFaltantes, "Lista vacía", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val prefs = PrinterPreferences(this@ListaFaltantes)
            val macCaja = prefs.obtenerImpresoraCaja()
            val printer = TicketPrinter(this@ListaFaltantes)

            printer.imprimirListaFaltantes(lista, macCaja)
        }
    }
}