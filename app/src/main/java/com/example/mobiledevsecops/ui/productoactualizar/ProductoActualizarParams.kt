package com.example.mobiledevsecops.ui.productoactualizar

data class ProductoActualizarParams(
    val id: Int,
    val strNombreProducto: String,
    val strURLImagen: String?,
    val strDescripcion: String?,
    val intNumeroExistencia: Int,
    val decPrecio: Double,
    val rowVersion: String
)
