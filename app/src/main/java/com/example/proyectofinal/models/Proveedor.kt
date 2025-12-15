package com.example.proyectofinal.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Proveedor(
    val id: Int,
    val nombre: String,
    val empresa: String? = null,
    val telefono: String,
    @SerialName("Proveedor_Ingrediente")
    val relaciones: List<ProveedorIngredienteDetalle>? = null
)

@Serializable
data class ProveedorIngredienteDetalle(
    @SerialName("Ingrediente")
    val ingrediente: Ingrediente
)

@Serializable
data class ProveedorInsert(
    val nombre: String,
    val empresa: String?,
    val telefono: String
)

@Serializable
data class ProveedorIngredienteInsert(
    val id_proveedor: Int,
    val id_ingrediente: Int
)