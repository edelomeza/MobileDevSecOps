package com.example.mobiledevsecops.domain.model

data class VentaDetalle(
    val id: Int = 0,
    val idVenVenta: Int = 0,
    val idProProducto: Int = 0,
    val strNombreProducto: String = "",
    val decPrecio: Double = 0.0,
    val intPiezaVenta: Int = 0,
    val decTotalVenta: Double = 0.0,
    val rowVersion: String = ""
)
