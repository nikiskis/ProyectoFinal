// Archivo: app/src/main/java/com/example/proyectofinal/repositories/VentasRepository.kt
package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.models.DetalleVenta
import com.example.proyectofinal.models.DetalleVentaInsert
import com.example.proyectofinal.models.FaltanteInsert
import com.example.proyectofinal.models.Venta
import com.example.proyectofinal.models.VentaInsert
import com.example.proyectofinal.models.VentaPagoInsert
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class VentasRepository {

    private val client = SupabaseClient.client.postgrest
    private val tableName = "Venta"
    private val detalleTable = "Detalle_Venta"
    private val pagosTable = "Venta_Pago"
    private val faltantesRepo = FaltantesRepository() // Instancia para agregar faltantes

    suspend fun getVentasActivas(): List<Venta> {
        return try {
            val response = client[tableName].select(
                columns = Columns.raw("*, estado:Estado(*), Detalle_Venta(id)")
            ) {
                filter { eq("id_estado", 1) }
                order("fecha", order = Order.DESCENDING)
            }
            response.decodeList<Venta>()
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error al obtener ventas activas: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getVentaById(id: Int): Venta? {
        return try {
            client[tableName].select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<Venta>()
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error al obtener venta por ID", e)
            null
        }
    }

    suspend fun crearVenta(nuevaVenta: VentaInsert) {
        try {
            client[tableName].insert(nuevaVenta)
            Log.i("VentasRepo", "Venta creada exitosamente")
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error CRÍTICO al crear venta: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateDatosPedido(idVenta: Int, identificador: String, direccion: String?) {
        try {
            val updateMap = mutableMapOf<String, String?>("identificador" to identificador)
            if (direccion != null) {
                updateMap["direccion"] = direccion
            }
            client[tableName].update(updateMap) {
                filter { eq("id", idVenta) }
            }
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error actualizando datos pedido", e)
        }
    }

    suspend fun finalizarVentaCompleta(
        idVenta: Int,
        descuentoMonto: Double,
        descuentoPorcentaje: Int,
        totalFinal: Double,
        propina: Double,
        idMetodoPropina: Int,
        pagos: List<VentaPagoInsert>
    ) {
        try {
            val datosVenta = buildJsonObject {
                put("descuento_monto", descuentoMonto)
                put("descuento_porcentaje", descuentoPorcentaje)
                put("total_final", totalFinal)
                put("propina", propina)
                put("id_metodo_propina", idMetodoPropina)
                put("id_estado", 3)
            }
            client[tableName].update(datosVenta) { filter { eq("id", idVenta) } }

            if (pagos.isNotEmpty()) {
                client[pagosTable].insert(pagos)
            }

            descontarStockDeVenta(idVenta)

        } catch (e: Exception) {
            Log.e("VentasRepo", "Error CRÍTICO al finalizar venta: ${e.message}", e)
            throw e
        }
    }

    private suspend fun descontarStockDeVenta(idVenta: Int) {
        try {
            val response = client[detalleTable].select(
                columns = Columns.raw("cantidad, producto:Producto(producto_ingrediente:Producto_Ingrediente(id_ingrediente, cantidad))")
            ) {
                filter { eq("id_venta", idVenta) }
            }
            val detalles = response.decodeList<DetalleStockHelper>()

            val mapaDescuento = mutableMapOf<Int, Double>()
            detalles.forEach { detalle ->
                detalle.producto.producto_ingrediente.forEach { relacion ->
                    val cantidadTotalIngrediente = (detalle.cantidad * relacion.cantidad).toDouble()
                    val acumulado = mapaDescuento.getOrDefault(relacion.id_ingrediente, 0.0)
                    mapaDescuento[relacion.id_ingrediente] = acumulado + cantidadTotalIngrediente
                }
            }

            if (mapaDescuento.isNotEmpty()) {
                val idsIngredientes = mapaDescuento.keys.toList()

                val ingredientesResponse = client["Ingrediente"].select(
                    columns = Columns.raw("id, nombre, stock_actual, stock_minimo")
                ) {
                    filter { isIn("id", idsIngredientes) }
                }
                val ingredientesActuales = ingredientesResponse.decodeList<IngredienteStockHelper>()

                ingredientesActuales.forEach { ing ->
                    val cantidadADescontar = mapaDescuento[ing.id] ?: 0.0
                    val nuevoStock = ing.stock_actual - cantidadADescontar

                    client["Ingrediente"].update(
                        mapOf("stock_actual" to nuevoStock)
                    ) {
                        filter { eq("id", ing.id) }
                    }
                    Log.i("VentasRepo", "Stock actualizado ${ing.nombre}: $nuevoStock")

                    if (nuevoStock <= ing.stock_minimo) {
                        val cantidadAPedir = ing.stock_minimo * 4
                        try {
                            val nuevoFaltante = FaltanteInsert(
                                nombre = ing.nombre,
                                cantidad = cantidadAPedir.toString()
                            )
                            faltantesRepo.agregarFaltante(nuevoFaltante)
                            Log.i("VentasRepo", "ALERTA: ${ing.nombre} bajo de stock. Agregado a Faltantes.")
                        } catch (e: Exception) {
                            Log.e("VentasRepo", "Error al agregar a faltantes automático", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error al descontar stock de ingredientes: ${e.message}", e)
        }
    }

    suspend fun cancelarVenta(idVenta: Int) {
        try {
            client[tableName].update(mapOf("id_estado" to 4)) { filter { eq("id", idVenta) } }
        } catch (e: Exception) { Log.e("VentasRepo", "Error", e) }
    }

    suspend fun getDetallesVenta(idVenta: Int): List<DetalleVenta> {
        return try {
            val response = client[detalleTable].select(columns = Columns.raw("*, producto:Producto(id, nombre, id_zona_produccion)")) {
                filter { eq("id_venta", idVenta) }
                order("id", order = Order.ASCENDING)
            }
            response.decodeList<DetalleVenta>()
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error", e)
            emptyList()
        }
    }

    suspend fun marcarImpresoCocina(idVenta: Int) {
        try {
            client[tableName].update(mapOf("impreso_cocina" to true)) {
                filter { eq("id", idVenta) }
            }
            Log.i("VentasRepo", "Venta $idVenta marcada como impresa en cocina.")
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error al marcar impreso cocina", e)
        }
    }

    suspend fun updateDescuentoVenta(idVenta: Int, monto: Double, porcentaje: Int) {
        try {
            val updateData = buildJsonObject {
                put("descuento_monto", monto)
                put("descuento_porcentaje", porcentaje)
            }

            client[tableName].update(updateData) {
                filter { eq("id", idVenta) }
            }
            Log.i("VentasRepo", "Descuento actualizado para venta $idVenta")
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error al actualizar descuento", e)
            throw e
        }
    }

    suspend fun getVentasPorRango(fechaInicio: String, fechaFin: String): List<Venta> {
        return try {
            val response = client[tableName].select(
                columns = Columns.raw(
                    "*, " +
                            "estado:Estado(*), " +
                            "Venta_Pago(*), " +
                            "Detalle_Venta(*, producto:Producto(*, zona_produccion:Zona_Produccion(*), categoria_producto:Categoria_Producto(*)))"
                )
            ) {
                filter {
                    gte("fecha", fechaInicio)
                    lte("fecha", fechaFin)
                }
                order("fecha", order = Order.DESCENDING)
            }
            response.decodeList<Venta>()
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error reporte corte: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getVentasComplejas(): List<Venta> {
        return getVentasPorRango("2024-01-01 00:00:00", "2030-12-31 23:59:59")
    }

    suspend fun agregarDetalle(detalle: DetalleVentaInsert) {
        try { client[detalleTable].insert(detalle) } catch (e: Exception) { Log.e("VentasRepo", "Error", e) }
    }

    suspend fun updateNotaDetalle(idDetalle: Int, nota: String) {
        try { client[detalleTable].update(mapOf("notas" to nota)) { filter { eq("id", idDetalle) } } } catch (e: Exception) { Log.e("VentasRepo", "Error", e) }
    }

    suspend fun eliminarDetalle(idDetalle: Int) {
        try { client[detalleTable].delete { filter { eq("id", idDetalle) } } } catch (e: Exception) { Log.e("VentasRepo", "Error", e) }
    }

    suspend fun marcarVentaPagada(idVenta: Int) {
        try { client[tableName].update(mapOf("id_estado" to 3)) { filter { eq("id", idVenta) } } } catch (e: Exception) { Log.e("VentasRepo", "Error", e) }
    }

    suspend fun updateCantidadDetalle(idDetalle: Int, nuevaCantidad: Int) {
        try { client[detalleTable].update(mapOf("cantidad" to nuevaCantidad)) { filter { eq("id", idDetalle) } } } catch (e: Exception) { Log.e("VentasRepo", "Error", e) }
    }
}

@Serializable
data class DetalleStockHelper(
    val cantidad: Int,
    val producto: ProductoStockHelper
)

@Serializable
data class ProductoStockHelper(
    val producto_ingrediente: List<RelacionIngredienteHelper> = emptyList()
)

@Serializable
data class RelacionIngredienteHelper(
    val id_ingrediente: Int,
    val cantidad: Int
)

@Serializable
data class IngredienteStockHelper(
    val id: Int,
    val nombre: String,
    val stock_actual: Double,
    val stock_minimo: Double
)