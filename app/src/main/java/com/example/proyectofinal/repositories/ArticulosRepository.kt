package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.models.Articulo
import com.example.proyectofinal.models.ArticuloInsert
import com.example.proyectofinal.models.ArticuloIngredienteInsert
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest // Assuming you still use this library
import io.github.jan.supabase.postgrest.query.Columns

class ArticulosRepository {

    private val client = SupabaseClient.client.postgrest
    private val productoTable = "Producto"
    private val productoIngredienteTable = "Producto_Ingrediente"

    suspend fun getArticulos(): List<Articulo> {
        return try {
            // Consulta compleja para traer el producto y sus datos relacionados de otras tablas
            val response = client[productoTable].select(
                // Renombramos las relaciones para que coincidan con las data class
                columns = Columns.raw("*, estado:Estado(*), zona_produccion:Zona_Produccion(*), categoria_producto:Categoria_Producto(*), producto_ingrediente:Producto_Ingrediente(*, ingrediente:Ingrediente(*))")
            )
            response.decodeList<Articulo>()
        } catch (e: Exception) {
            Log.e("ArticulosRepository", "Error al obtener artículos", e)
            emptyList()
        }
    }

    suspend fun crearArticulo(nuevoArticulo: ArticuloInsert): Int? {
        return try {
            // Usamos 'select().decodeSingle()' para obtener el objeto recién creado
            val response = client[productoTable].insert(nuevoArticulo) {
                select()
            }.decodeSingle<Articulo>() // Asume que la respuesta es un solo artículo
            Log.i("ArticulosRepository", "Artículo '${nuevoArticulo.nombre}' creado con ID ${response.id}.")
            response.id // Devuelve el ID del nuevo artículo
        } catch (e: Exception) {
            Log.e("ArticulosRepository", "Error al crear el artículo", e)
            null // Devuelve null si hubo un error
        }
    }

    suspend fun updateArticulo(id: Int, articuloActualizado: ArticuloInsert) {
        try {
            client[productoTable].update(articuloActualizado) {
                filter { eq("id", id) } // Filtra por el ID del artículo a actualizar
            }
            Log.i("ArticulosRepository", "Artículo ID $id actualizado.")
        } catch (e: Exception) {
            Log.e("ArticulosRepository", "Error al actualizar el artículo", e)
        }
    }

    suspend fun deleteArticulo(id: Int) {
        try {
            client[productoTable].delete {
                filter { eq("id", id) } // Filtra por el ID del artículo a eliminar
            }
            Log.i("ArticulosRepository", "Artículo ID $id eliminado.")
        } catch (e: Exception) {
            Log.e("ArticulosRepository", "Error al eliminar el artículo", e)
        }
    }

    suspend fun addIngredienteToArticulo(relacion: ArticuloIngredienteInsert) {
        try {
            client[productoIngredienteTable].insert(relacion)
            Log.i("ArticulosRepository", "Ingrediente añadido al producto ID ${relacion.id_producto}.")
        } catch (e: Exception) {
            Log.e("ArticulosRepository", "Error al añadir ingrediente al artículo", e)
        }
    }

    suspend fun removeIngredienteFromArticulo(idRelacion: Int) {
        try {
            client[productoIngredienteTable].delete {
                filter { eq("id", idRelacion) } // Filtra por el ID de la relación a eliminar
            }
            Log.i("ArticulosRepository", "Relación de ingrediente ID $idRelacion eliminada.")
        } catch (e: Exception) {
            Log.e("ArticulosRepository", "Error al eliminar ingrediente del artículo", e)
        }
    }
}