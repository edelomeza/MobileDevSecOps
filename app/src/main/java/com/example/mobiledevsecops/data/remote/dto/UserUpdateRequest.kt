package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest(
    val id: Int,
    val strNombre: String,
    val strPWD: String,
    val strCorreoElectronico: String,
    val rowVersion: String
)
