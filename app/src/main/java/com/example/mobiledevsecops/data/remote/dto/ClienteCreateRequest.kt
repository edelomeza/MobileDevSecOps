package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClienteCreateRequest(
    val strNombreCliente: String,
    val strDireccionCliente: String? = null,
    val strCorreoElectronico: String,
    val strNumeroTelefono: String
)
