package com.example.mobiledevsecops.ui.productoeliminar

data class ProductoEliminarParams(
    val id: Int,
    val strNombreProducto: String,
    val strURLImagen: String?,
    val strDescripcion: String?,
    val intNumeroExistencia: Int,
    val decPrecio: Double,
    val rowVersion: String
)
