package com.example.proyectofinal.models

import kotlinx.serialization.Serializable

@Serializable
data class DetalleVenta(
    val id: Int,
    val id_venta: Int,
    val id_producto: Int,
    val cantidad: Int,
    val precio_unidad: Double,
    val notas: String? = null,
    val producto: ProductoSimple? = null
)

@Serializable
data class ProductoSimple(
    val nombre: String
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