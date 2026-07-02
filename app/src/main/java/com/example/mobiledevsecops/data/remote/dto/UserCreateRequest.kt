package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserCreateRequest(
    val strNombre: String,
    val strPWD: String,
    val strCorreoElectronico: String
)
