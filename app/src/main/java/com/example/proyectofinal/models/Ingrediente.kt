package com.example.proyectofinal.models

import kotlinx.serialization.Serializable

@Serializable
data class Ingrediente(
    val id: Int,
    val nombre: String,
    val costo: Double,
    val stock_actual: Double,
    val stock_minimo: Double,
    val id_estado: Int,
    val estado: Estado
)

@Serializable
data class Estado(
    val id: Int,
    val estado: String
)

@Serializable
data class IngredienteInsert(
    val nombre: String,
    val costo: Double,
    val stock_actual: Double,
    val stock_minimo: Double,
    val id_estado: Int
)

@Serializable
data class IngredienteUpdate(
    val nombre: String,
    val costo: Double,
    val stock_actual: Double,
    val stock_minimo: Double,
    val id_estado: Int
)