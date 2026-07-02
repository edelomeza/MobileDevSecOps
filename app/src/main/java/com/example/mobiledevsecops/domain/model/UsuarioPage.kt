package com.example.mobiledevsecops.domain.model

data class UsuarioPage(
    val items: List<Usuario>,
    val totalCount: Int,
    val pageNumber: Int,
    val totalPages: Int
)
