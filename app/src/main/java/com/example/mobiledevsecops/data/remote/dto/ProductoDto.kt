package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: Int,
    val strNombreProducto: String,
    val strURLImagen: String? = null,
    val strDescripcion: String? = null,
    val intNumeroExistencia: Int,
    val decPrecio: Double,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)

@Serializable
data class ProductoListResponse(
    @SerialName("Items")
    val items: List<ProductoDto>,
    @SerialName("TotalCount")
    val totalCount: Int,
    @SerialName("PageNumber")
    val pageNumber: Int,
    @SerialName("PageSize")
    val pageSize: Int,
    @SerialName("TotalPages")
    val totalPages: Int
)
