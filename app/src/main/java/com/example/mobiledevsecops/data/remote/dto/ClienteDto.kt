package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClienteDto(
    val id: Int,
    val strNombreCliente: String,
    val strDireccionCliente: String? = null,
    val strCorreoElectronico: String,
    val strNumeroTelefono: String,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)

@Serializable
data class ClienteListResponse(
    @SerialName("Items")
    val items: List<ClienteDto>,
    @SerialName("TotalCount")
    val totalCount: Int,
    @SerialName("PageNumber")
    val pageNumber: Int,
    @SerialName("PageSize")
    val pageSize: Int,
    @SerialName("TotalPages")
    val totalPages: Int
)
