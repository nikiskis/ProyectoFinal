package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.models.Faltante
import com.example.proyectofinal.models.FaltanteInsert
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class FaltantesRepository {

    private val client = SupabaseClient.client.postgrest
    private val tableName = "Faltante"

    suspend fun getFaltantes(): List<Faltante> {
        return try {
            val response = client[tableName].select {
                order("nombre", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }
            response.decodeList<Faltante>()
        } catch (e: Exception) {
            Log.e("FaltantesRepo", "Error al obtener faltantes", e)
            emptyList()
        }
    }

    suspend fun agregarFaltante(nuevo: FaltanteInsert) {
        try {
            client[tableName].insert(nuevo)
        } catch (e: Exception) {
            Log.e("FaltantesRepo", "Error al agregar faltante", e)
            throw e
        }
    }

    suspend fun eliminarFaltante(id: Int) {
        try {
            client[tableName].delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            Log.e("FaltantesRepo", "Error al eliminar faltante", e)
        }
    }

    suspend fun limpiarLista() {
        try {
            client[tableName].delete {
                filter { neq("id", 0) }
            }
        } catch (e: Exception) {
            Log.e("FaltantesRepo", "Error al limpiar lista", e)
        }
    }
}