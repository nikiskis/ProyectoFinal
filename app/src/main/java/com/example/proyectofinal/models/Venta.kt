package com.example.proyectofinal.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Venta(
    val id: Int,
    val fecha: String,
    val id_estado: Int,
    val tipo_pedido: String? = null,
    val identificador: String? = null,
    val direccion: String? = null,
    val impreso_cocina: Boolean = false,
    val estado: Estado? = null,

    @SerialName("Detalle_Venta")
    val detalles: List<DetalleVenta>? = null,
    val descuento_monto: Double? = 0.0,
    val descuento_porcentaje: Int? = 0,
    val total_final: Double? = 0.0,
    val propina: Double? = 0.0,
    val id_metodo_propina: Int? = null
)

@Serializable
data class VentaInsert(
    val fecha: String,
    val id_estado: Int,
    val tipo_pedido: String,
    val identificador: String,
    val direccion: String? = null
)

@Serializable
data class VentaPagoInsert(
    val id_venta: Int,
    val id_metodo_pago: Int,
    val monto: Double
)