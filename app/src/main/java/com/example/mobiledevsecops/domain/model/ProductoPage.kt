package com.example.mobiledevsecops.domain.model

data class ProductoPage(
    val items: List<Producto>,
    val totalCount: Int,
    val pageNumber: Int,
    val totalPages: Int
)
