package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VentaCreateRequest(
    val idCliCliente: Int,
    val idSegUsuario: Int,
    val dteFechaHoraCompra: String,
    val strClaveVenta: String
)