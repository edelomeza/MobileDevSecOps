package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmpleadoListResponse(
    @SerialName("Items") val items: List<EmpleadoDto>,
    @SerialName("TotalCount") val totalCount: Int,
    @SerialName("PageNumber") val pageNumber: Int,
    @SerialName("PageSize") val pageSize: Int,
    @SerialName("TotalPages") val totalPages: Int
)
