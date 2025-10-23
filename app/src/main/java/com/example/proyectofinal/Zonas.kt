package com.example.proyectofinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.adapter.Zona
import com.example.proyectofinal.adapter.ZonasAdapter
import com.example.proyectofinal.repositories.ZonasRepository
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class Zonas : AppCompatActivity() {

    private lateinit var zonasRecyclerView: RecyclerView
    private lateinit var zonasAdapter: ZonasAdapter
    private val zonasRepo = ZonasRepository()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zonas)


        setupRecyclerView()
        loadZonas()
        val botonAgregar = findViewById<Button>(R.id.btnAgregar)
        botonAgregar.setOnClickListener {
            showAddZoneDialog()
        }
        val botonRegresar = findViewById<Button>(R.id.btnRegresar)
       botonRegresar.setOnClickListener {
            val intent = Intent(this, Articulos::class.java)
            startActivity(intent)
        }
    }

    private fun showAddZoneDialog() {
        val editText = EditText(this).apply {
            hint = "Nombre de la nueva zona"
        }

        AlertDialog.Builder(this)
            .setTitle("Agregar Nueva Zona")
            .setView(editText)
            .setPositiveButton("Agregar") { _, _ ->
                val nombreZona = editText.text.toString()
                if (nombreZona.isNotBlank()) {
                    lifecycleScope.launch {
                        zonasRepo.crearZona(nombreZona)
                        loadZonas()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupRecyclerView() {
        zonasRecyclerView = findViewById(R.id.zonasRecyclerView)
        zonasAdapter = ZonasAdapter(mutableListOf(), this)
        zonasRecyclerView.adapter = zonasAdapter
        zonasRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadZonas() {
        lifecycleScope.launch {
            try {
                val response = SupabaseClient.client.postgrest["Zona_Produccion"].select()
                val zonasFromDB = response.decodeList<Zona>()
                zonasAdapter.updateData(zonasFromDB)
            } catch (e: Exception) {
            }
        }
    }


}