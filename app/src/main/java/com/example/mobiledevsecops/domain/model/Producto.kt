package com.example.mobiledevsecops.domain.model

data class Producto(
    val id: Int = 0,
    val strNombreProducto: String,
    val strURLImagen: String? = null,
    val strDescripcion: String? = null,
    val intNumeroExistencia: Int = 0,
    val decPrecio: Double = 0.0,
    val rowVersion: String = ""
)
