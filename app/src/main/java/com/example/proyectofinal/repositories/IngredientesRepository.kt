package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.models.Ingrediente
import com.example.proyectofinal.models.IngredienteInsert
import com.example.proyectofinal.models.IngredienteUpdate
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
            val response = client[tableName].select(
                columns = Columns.raw("*, estado:Estado(*)")
            )
            response.decodeList<Ingrediente>()
        } catch (e: Exception) {
            Log.e("IngredientesRepository", "Error al obtener ingredientes", e)
            emptyList()
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

    suspend fun getIngredientesActivos(): List<Ingrediente> {
        return try {

            val response = client[tableName].select(
                columns = Columns.raw("*, estado:Estado(*)")
            ) {
                filter { eq("id_estado", 1) }
            }
            response.decodeList<Ingrediente>()
        } catch (e: Exception) {
            Log.e("IngredientesRepository", "Error al obtener ingredientes activos", e)
            emptyList()
        }
    }
}