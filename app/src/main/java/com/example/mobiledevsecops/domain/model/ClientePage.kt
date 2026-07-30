package com.example.mobiledevsecops.domain.model

data class ClientePage(
    val items: List<Cliente>,
    val totalCount: Int,
    val pageNumber: Int,
    val totalPages: Int
)
