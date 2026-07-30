package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClienteDeleteRequest(
    val id: Int,
    val rowVersion: String
)
