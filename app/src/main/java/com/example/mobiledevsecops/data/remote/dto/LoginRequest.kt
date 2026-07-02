package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val User: String,
    val Password: String
)
