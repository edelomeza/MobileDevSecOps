package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDeleteRequest(
    val id: Int,
    val rowVersion: String
)
