package com.example.mobiledevsecops.domain.model

data class EmpleadoPage(
    val items: List<Empleado>,
    val totalCount: Int,
    val pageNumber: Int,
    val totalPages: Int
)
