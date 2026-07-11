package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmpleadoDeleteRequest(
    val id: Int,
    val rowVersion: String
)
