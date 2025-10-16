package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.Models.Ingrediente
import com.example.proyectofinal.Models.IngredienteInsert
import com.example.proyectofinal.Models.IngredienteUpdate
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns


class IngredientesRepository {

    private val client = SupabaseClient.client.postgrest
    private val tableName = "Ingrediente"

    suspend fun crearIngrediente(nuevoIngrediente: IngredienteInsert) {
        try {
            client[tableName].insert(nuevoIngrediente)
            Log.i("IngredientesRepository", "Ingrediente '${nuevoIngrediente.nombre}' creado exitosamente.")
        } catch (e: Exception) {
            Log.e("IngredientesRepository", "Error al crear el ingrediente", e)
        }
    }

    suspend fun getIngredientes(): List<Ingrediente> {
        return try {
            val response = client["Ingrediente"].select(
                columns = Columns.raw("*, estado:Estado(*)")
            )
            response.decodeList<Ingrediente>()
        } catch (e: Exception) {
            Log.e("IngredientesRepository", "Error al obtener ingredientes", e)
            emptyList()
        }
    }

    suspend fun updateIngrediente(id: Int, nombre: String, costo: Double, id_estado: Int) {
        try {
            val ingredienteActualizado = mapOf(
                "nombre" to nombre,
                "costo" to costo,
                "id_estado" to id_estado
            )
            client[tableName].update(ingredienteActualizado) {
                filter { eq("id", id) }
            }
            Log.i("IngredientesRepository", "Ingrediente ID $id actualizado.")
        } catch (e: Exception) {
            Log.e("IngredientesRepository", "Error al actualizar el ingrediente", e)
        }
    }

    suspend fun deleteIngrediente(id: Int) {
        try {
            client[tableName].delete {
                filter { eq("id", id) }
            }
            Log.i("IngredientesRepository", "Ingrediente ID $id eliminado.")
        } catch (e: Exception) {
            Log.e("IngredientesRepository", "Error al eliminar el ingrediente", e)
        }
    }
    suspend fun updateIngrediente(id: Int, ingredienteActualizado: IngredienteUpdate) {
        try {
            client[tableName].update(ingredienteActualizado) {
                filter { eq("id", id) }
            }
            Log.i("IngredientesRepository", "Ingrediente ID $id actualizado.")
        } catch (e: Exception) {
            Log.e("IngredientesRepository", "Error al actualizar el ingrediente", e)
        }
    }
}