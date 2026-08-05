package com.example.mobiledevsecops.domain.model

data class VentaPage(
    val items: List<Venta>,
    val totalCount: Int,
    val pageNumber: Int,
    val totalPages: Int
)