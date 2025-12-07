package com.example.proyectofinal.models

import kotlinx.serialization.Serializable

@Serializable
data class DetalleVenta(
    val id: Int,
    val id_venta: Int,
    val id_producto: Int, // Este es el ID foráneo, pero a veces necesitamos el del objeto anidado
    var cantidad: Int,    // CAMBIO: 'var' para poder editar la cantidad en memoria si es necesario
    val precio_unidad: Double,
    val notas: String? = null,
    val producto: ProductoSimple? = null
)

@Serializable
data class ProductoSimple(
    val id: Int,          // <--- ¡AQUÍ ESTABA EL ERROR! Faltaba este campo.
    val nombre: String,
    val id_zona_produccion: Int? = null // Puede ser nulo si la DB no lo trae siempre
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