package com.example.proyectofinal.repositories

import android.util.Log
// Asegúrate de importar tu data class CategoriaProducto
import com.example.proyectofinal.models.CategoriaProducto
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest // O el import correcto
import io.github.jan.supabase.postgrest.query.Columns // O el import correcto

class CategoriasRepository {
    private val client = SupabaseClient.client.postgrest
    private val tableName = "Categoria_Producto"

    suspend fun getCategorias(): List<CategoriaProducto> {
        return try {
            val response = client[tableName].select()
            response.decodeList<CategoriaProducto>()
        } catch (e: Exception) {
            Log.e("CategoriasRepository", "Error al obtener categorías", e)
            emptyList()
        }
    }

    suspend fun crearCategoria(nombre: String) {
        try {
            val nuevaCategoria = mapOf("nombre" to nombre)
            client[tableName].insert(nuevaCategoria) // Usa tableName
            Log.i("CategoriasRepository", "Categoria '$nombre' creada exitosamente.")
        } catch (e: Exception) {
            Log.e("CategoriasRepository", "Error al crear la categoria", e)
        }
    }

    suspend fun updateCategoria(id: Int, nuevoNombre: String) {
        try {
            client[tableName].update(mapOf("nombre" to nuevoNombre)) {
                filter { eq("id", id) }
            }
            Log.i("CategoriasRepository", "Categoría ID $id actualizada.")
        } catch (e: Exception) {
            Log.e("CategoriasRepository", "Error al actualizar categoría", e)
        }
    }

    suspend fun deleteCategoria(id: Int) {
        try {
            client[tableName].delete {
                filter { eq("id", id) }
            }
            Log.i("CategoriasRepository", "Categoría ID $id eliminada.")
        } catch (e: Exception) {
            Log.e("CategoriasRepository", "Error al eliminar categoría", e)
        }
    }
}