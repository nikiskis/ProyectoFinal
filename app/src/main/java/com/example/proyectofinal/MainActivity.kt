package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val botonArticulos = findViewById<Button>(R.id.btnArticulos)
        botonArticulos.setOnClickListener {
            val intent = Intent(this, Articulos::class.java)
            startActivity(intent)
        }

        val botonPedidos = findViewById<Button>(R.id.btnPedidos)
        botonPedidos.setOnClickListener {
            val intent = Intent(this, Pedido::class.java)
            startActivity(intent)
        }

        val botonCorteCaja = findViewById<Button>(R.id.btnCorteCaja)
        botonCorteCaja.setOnClickListener {
            val intent = Intent(this, CorteCaja::class.java)
            startActivity(intent)
        }
    }
}