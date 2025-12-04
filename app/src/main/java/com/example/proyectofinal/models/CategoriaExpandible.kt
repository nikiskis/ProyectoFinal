package com.example.proyectofinal.models

data class CategoriaExpandible(
    val categoria: CategoriaProducto,
    val productos: List<Articulo>,
    var isExpanded: Boolean = false
)