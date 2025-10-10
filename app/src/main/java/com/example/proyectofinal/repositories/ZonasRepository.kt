package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class ZonasRepository {
    private val client = SupabaseClient.client

    suspend fun crearZona(nombre: String) {
        try {
            val nuevaZona = mapOf("nombre" to nombre)

            client.postgrest["Zona_Produccion"].insert(nuevaZona)
            Log.i("ZonasRepository", "Zona '$nombre' creada exitosamente.")
        } catch (e: Exception) {
            Log.e("ZonasRepository", "Error al crear la zona", e)

        }
    }
    suspend fun updateZona(id: Int, nuevoNombre: String) {
        client.postgrest["Zona_Produccion"].update(mapOf("nombre" to nuevoNombre)) { filter { eq("id", id) } }
    }
    suspend fun deleteZona(id: Int) {
        client.postgrest["Zona_Produccion"].delete { filter { eq("id", id) } }
    }

}