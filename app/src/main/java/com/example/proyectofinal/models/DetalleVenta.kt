package com.example.proyectofinal.models

import kotlinx.serialization.Serializable

@Serializable
data class DetalleVenta(
    val id: Int,
    val id_venta: Int = 0,
    val id_producto: Int = 0,
    val cantidad: Int = 0,
    val precio_unidad: Double = 0.0,
    val costo_unidad: Double = 0.0,
    val notas: String? = null,
    val producto: Articulo? = null
)

@Serializable
data class DetalleVentaInsert(
    val id_venta: Int,
    val id_producto: Int,
    val cantidad: Int,
    val precio_unidad: Double,
    val costo_unidad: Double,
    val notas: String? = null
)