package com.example.mobiledevsecops.domain.model

data class Usuario(
    val id: Int = 0,
    val strNombre: String,
    val strCorreoElectronico: String,
    val rowVersion: String = ""
)
