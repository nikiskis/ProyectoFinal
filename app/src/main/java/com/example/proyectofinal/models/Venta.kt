package com.example.proyectofinal.models

import kotlinx.serialization.Serializable

@Serializable
data class Venta(
    val id: Int,
    val fecha: String,
    val id_estado: Int,
    val tipo_pedido: String? = null,
    val identificador: String? = null,
    val estado: Estado? = null,
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
    val identificador: String
)

@Serializable
data class VentaPagoInsert(
    val id_venta: Int,
    val id_metodo_pago: Int,
    val monto: Double
)