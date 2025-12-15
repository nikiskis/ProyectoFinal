package com.example.proyectofinal.models

import kotlinx.serialization.Serializable

@Serializable
data class Faltante(
    val id: Int,
    val nombre: String,
    val cantidad: String,
    val completado: Boolean = false
)

@Serializable
data class FaltanteInsert(
    val nombre: String,
    val cantidad: String
)