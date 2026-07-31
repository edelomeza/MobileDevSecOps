package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductCreateRequest(
    val strNombreProducto: String,
    val strURLImagen: String? = null,
    val strDescripcion: String? = null,
    val intNumeroExistencia: Int,
    val decPrecio: Double
)
