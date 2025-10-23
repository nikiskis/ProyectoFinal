package com.example.proyectofinal.models

import kotlinx.serialization.Serializable

// Representa la tabla Producto con sus relaciones
@Serializable
data class Articulo(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val id_estado: Int,
    val id_zona_produccion: Int,
    val id_categoria_producto: Int,
    val estado: Estado,
    val zona_produccion: ZonaProduccion,
    val categoria_producto: CategoriaProducto,
    val producto_ingrediente: List<ArticuloIngrediente> = emptyList()
)

// Representa la tabla de unión Producto_Ingrediente y su ingrediente anidado
@Serializable
data class ArticuloIngrediente(
    val id: Int,
    val cantidad: Int,
    val ingrediente: IngredienteRelacionado // Usamos un nombre diferente para evitar confusión
)

// Data class simplificada para el Ingrediente DENTRO de ArticuloIngrediente
@Serializable
data class IngredienteRelacionado(
    val id: Int,
    val nombre: String,
    val costo: Double
)


@Serializable
data class ZonaProduccion(val id: Int, val nombre: String)

@Serializable
data class CategoriaProducto(val id: Int, val nombre: String)

@Serializable
data class ArticuloInsert(
    val nombre: String,
    val precio: Double,
    val id_estado: Int,
    val id_zona_produccion: Int,
    val id_categoria_producto: Int
)

@Serializable
data class ArticuloIngredienteInsert(
    val cantidad: Int,
    val id_producto: Int,
    val id_ingrediente: Int
)