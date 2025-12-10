package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.models.ZonaProduccion
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class ZonasRepository {
    private val client = SupabaseClient.client.postgrest
    private val tableName = "Zona_Produccion"

    suspend fun getZonas(): List<ZonaProduccion> {
        return try {
            val response = client[tableName].select()
            response.decodeList<ZonaProduccion>()
        } catch (e: Exception) {
            Log.e("ZonasRepository", "Error al obtener zonas", e)
            emptyList()
        }
    }

    suspend fun crearZona(nombre: String) {
        try {
            val nuevaZona = mapOf("nombre" to nombre)
            client[tableName].insert(nuevaZona)
            Log.i("ZonasRepository", "Zona '$nombre' creada exitosamente.")
        } catch (e: Exception) {
            Log.e("ZonasRepository", "Error al crear la zona", e)
        }
    }

    suspend fun updateZona(id: Int, nuevoNombre: String) {
        try {
            client[tableName].update(mapOf("nombre" to nuevoNombre)) {
                filter { eq("id", id) }
            }
            Log.i("ZonasRepository", "Zona ID $id actualizada.")
        } catch (e: Exception) {
            Log.e("ZonasRepository", "Error al actualizar zona", e)
        }
    }

    suspend fun deleteZona(id: Int) {
        try {
            client[tableName].delete {
                filter { eq("id", id) }
            }
            Log.i("ZonasRepository", "Zona ID $id eliminada.")
        } catch (e: Exception) {
            Log.e("ZonasRepository", "Error al eliminar zona", e)
        }
    }
}