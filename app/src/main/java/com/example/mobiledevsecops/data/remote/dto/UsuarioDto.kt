package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: Int,
    val strNombre: String,
    val strCorreoElectronico: String,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)
