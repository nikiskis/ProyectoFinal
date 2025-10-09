package com.example.proyectofinal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.proyectofinal.models.CategoriaProducto
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Declara una variable para nuestro TextView
    private lateinit var resultsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Conecta esta Activity con su archivo de diseño XML
        setContentView(R.layout.activity_main)

        // Busca el TextView en el layout usando su ID y lo asigna a nuestra variable
        resultsTextView = findViewById(R.id.resultsTextView)

        // Llamamos a la función para probar la conexión
        probarConexionSupabase()
    }

    private fun probarConexionSupabase() {
        lifecycleScope.launch {
            try {
                val categorias = SupabaseClient.client.from("Categoria_Producto")
                    .select()
                    .decodeList<CategoriaProducto>()

                val textoResultado = if (categorias.isNotEmpty()) {
                    "¡Conexión Exitosa!\n\n" + categorias.joinToString("\n") { "• ${it.nombre}" }
                } else {
                    "Conexión exitosa, pero no hay categorías para mostrar."
                }

                // En lugar de un Toast, ahora actualizamos el texto del TextView
                resultsTextView.text = textoResultado
                Log.d("SupabaseTest", textoResultado)

            } catch (e: Exception) {
                val textoError = "Error de conexión:\n\n${e.message}"
                // También mostramos el error en el TextView
                resultsTextView.text = textoError
                Log.e("SupabaseTest", textoError, e)
            }
        }
    }
}