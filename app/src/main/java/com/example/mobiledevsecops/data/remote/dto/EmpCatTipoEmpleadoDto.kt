package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmpCatTipoEmpleadoDto(
    val id: Int,
    val strValor: String,
    val strDescripcion: String
)

@Serializable
data class EmpCatTipoEmpleadoListResponse(
    @SerialName("Items") val items: List<EmpCatTipoEmpleadoDto>,
    @SerialName("TotalCount") val totalCount: Int,
    @SerialName("PageNumber") val pageNumber: Int,
    @SerialName("PageSize") val pageSize: Int,
    @SerialName("TotalPages") val totalPages: Int
)
