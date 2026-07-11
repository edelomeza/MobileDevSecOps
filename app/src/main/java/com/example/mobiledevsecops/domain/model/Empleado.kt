package com.example.mobiledevsecops.domain.model

data class Empleado(
    val id: Int = 0,
    val strNombre: String,
    val strAPaterno: String? = null,
    val strAMaterno: String? = null,
    val strCURP: String? = null,
    val idEmpCatTipoEmpleado: Int? = null,
    val rowVersion: String = ""
)
