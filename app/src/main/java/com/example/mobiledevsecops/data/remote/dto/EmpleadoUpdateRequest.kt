package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmpleadoUpdateRequest(
    val id: Int,
    val strNombre: String,
    val strAPaterno: String? = null,
    val strAMaterno: String? = null,
    val strCURP: String? = null,
    val idEmpCatTipoEmpleado: Int? = null,
    val rowVersion: String
)
