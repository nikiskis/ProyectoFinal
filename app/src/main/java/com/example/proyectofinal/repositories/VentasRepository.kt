package com.example.proyectofinal.repositories

import android.util.Log
import com.example.proyectofinal.models.DetalleVenta
import com.example.proyectofinal.models.DetalleVentaInsert
import com.example.proyectofinal.models.Venta
import com.example.proyectofinal.models.VentaInsert
import com.example.proyectofinal.models.VentaPagoInsert
import com.example.proyectofinal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class VentasRepository {

    private val client = SupabaseClient.client.postgrest
    private val tableName = "Venta"
    private val detalleTable = "Detalle_Venta"
    private val pagosTable = "Venta_Pago"

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
        } catch (e: Exception) {
            Log.e("VentasRepo", "Error CRÍTICO al finalizar venta: ${e.message}", e)
            throw e
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