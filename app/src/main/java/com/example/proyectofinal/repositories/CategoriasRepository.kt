package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class CategoriasRepository {
    private val client = SupabaseClient.client

    suspend fun crearCategoria(nombre: String) {
        try {
            val nuevaCategoria = mapOf("nombre" to nombre)

            client.postgrest["Categoria_Producto"].insert(nuevaCategoria)
            Log.i("CategoriasRepository", "Categoria '$nombre' creada exitosamente.")
        } catch (e: Exception) {
            Log.e("CategoriasRepository", "Error al crear la categoria", e)

        }
    }
    suspend fun updateCategoria(id: Int, nuevoNombre: String) {
        client.postgrest["Categoria_Producto"].update(mapOf("nombre" to nuevoNombre)) { filter { eq("id", id) } }
    }
    suspend fun deleteCategoria(id: Int) {
        client.postgrest["Categoria_Producto"].delete { filter { eq("id", id) } }
    }
}