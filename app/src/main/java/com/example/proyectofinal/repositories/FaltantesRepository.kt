package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.models.Faltante
import com.example.proyectofinal.models.FaltanteInsert
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

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
            val faltante = client[tableName].select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<Faltante>()

            if (faltante != null) {
                val cantidadAStock = extraerNumero(faltante.cantidad)

                if (cantidadAStock != null && cantidadAStock > 0) {

                    val ingrediente = client["Ingrediente"].select(
                        columns = io.github.jan.supabase.postgrest.query.Columns.raw("id, stock_actual")
                    ) {
                        filter { eq("nombre", faltante.nombre) }
                    }.decodeSingleOrNull<IngredienteStockUpdateHelper>()

                    if (ingrediente != null) {
                        val nuevoStock = ingrediente.stock_actual + cantidadAStock

                        client["Ingrediente"].update(mapOf("stock_actual" to nuevoStock)) {
                            filter { eq("id", ingrediente.id) }
                        }
                        Log.i("FaltantesRepo", "Stock recuperado para '${faltante.nombre}': +$cantidadAStock")
                    } else {
                        Log.i("FaltantesRepo", "El articulo '${faltante.nombre}' no existe en inventario, solo se elimina de la lista.")
                    }
                }
            }

            client[tableName].delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            Log.e("FaltantesRepo", "Error al eliminar y recuperar stock", e)
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

    private fun extraerNumero(texto: String): Double? {
        return try {
            val regex = Regex("\\d+(\\.\\d+)?") // Busca digitos, opcionalmente seguidos de punto y más digitos
            val match = regex.find(texto)
            match?.value?.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
data class IngredienteStockUpdateHelper(
    val id: Int,
    val stock_actual: Double
)