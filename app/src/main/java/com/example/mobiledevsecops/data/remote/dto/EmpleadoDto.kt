package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmpleadoDto(
    val id: Int,
    val strNombre: String,
    val strAPaterno: String? = null,
    val strAMaterno: String? = null,
    val strCURP: String? = null,
    val idEmpCatTipoEmpleado: Int? = null,
    @SerialName("RowVersion") val rowVersion: String? = null
)
