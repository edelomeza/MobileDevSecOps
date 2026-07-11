package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmpleadoCreateRequest(
    val strNombre: String,
    val strAPaterno: String? = null,
    val strAMaterno: String? = null,
    val strCURP: String? = null,
    val idEmpCatTipoEmpleado: Int? = null
)
