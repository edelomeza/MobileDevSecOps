package com.example.mobiledevsecops.domain.model

data class Venta(
    val id: Int = 0,
    val idCliCliente: Int = 0,
    val idSegUsuario: Int = 0,
    val idVenCatEstado: Int = 0,
    val strClaveVenta: String,
    val dteFechaHoraCompra: String? = null,
    val strNombreCliente: String = "",
    val strEstado: String = "",
    val rowVersion: String = ""
)
