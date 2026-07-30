package com.example.mobiledevsecops.domain.model

data class Cliente(
    val id: Int = 0,
    val strNombreCliente: String,
    val strDireccionCliente: String? = null,
    val strCorreoElectronico: String,
    val strNumeroTelefono: String,
    val rowVersion: String = ""
)
