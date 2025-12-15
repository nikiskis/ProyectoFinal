package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.models.Proveedor
import com.example.proyectofinal.models.ProveedorIngredienteInsert
import com.example.proyectofinal.models.ProveedorInsert
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class ProveedoresRepository {

    private val client = SupabaseClient.client.postgrest
    private val tableName = "Proveedor"
    private val relationTable = "Proveedor_Ingrediente"

    suspend fun getProveedores(): List<Proveedor> {
        return try {
            val response = client[tableName].select(
                columns = Columns.raw("*, Proveedor_Ingrediente(Ingrediente(*, estado:Estado(*)))")
            )
            response.decodeList<Proveedor>()
        } catch (e: Exception) {
            Log.e("ProvRepo", "Error get proveedores", e)
            emptyList()
        }
    }

    suspend fun crearProveedor(nuevo: ProveedorInsert) {
        try { client[tableName].insert(nuevo) }
        catch (e: Exception) { Log.e("ProvRepo", "Error crear", e) }
    }

    suspend fun updateProveedor(id: Int, actualizado: ProveedorInsert) {
        try {
            client[tableName].update(actualizado) { filter { eq("id", id) } }
        } catch (e: Exception) { Log.e("ProvRepo", "Error update", e) }
    }

    suspend fun deleteProveedor(id: Int) {
        try {
            client[tableName].delete { filter { eq("id", id) } }
        } catch (e: Exception) { Log.e("ProvRepo", "Error delete", e) }
    }

    suspend fun syncIngredientes(idProveedor: Int, nuevosIds: List<Int>) {
        try {
            client[relationTable].delete {
                filter { eq("id_proveedor", idProveedor) }
            }

            if (nuevosIds.isNotEmpty()) {
                val listaInsertar = nuevosIds.map { idIng ->
                    ProveedorIngredienteInsert(idProveedor, idIng)
                }
                client[relationTable].insert(listaInsertar)
            }
        } catch (e: Exception) {
            Log.e("ProvRepo", "Error sync ingredientes", e)
        }
    }
}