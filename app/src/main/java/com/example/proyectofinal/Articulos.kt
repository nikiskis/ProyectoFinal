package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectofinal.Adapter.Categoria

class Articulos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_articulos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val botonArticulos = findViewById<Button>(R.id.btnZonas)
        botonArticulos.setOnClickListener {
            val intent = Intent(this, Zonas::class.java)
            startActivity(intent)
        }

        val botonCategorias = findViewById<Button>(R.id.btnCategorias)
        botonCategorias.setOnClickListener {
            val intent = Intent(this, Categorias::class.java)
            startActivity(intent)
        }

        val botonRegresar = findViewById<Button>(R.id.btnRegresar)
        botonRegresar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}