package com.example.proyectofinal.models

import kotlinx.serialization.Serializable

@Serializable
data class CategoriaProducto(
    val id: Long,
    val nombre: String
)
