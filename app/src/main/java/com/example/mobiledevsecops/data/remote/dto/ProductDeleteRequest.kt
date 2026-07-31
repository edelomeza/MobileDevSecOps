package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDeleteRequest(
    val id: Int,
    val rowVersion: String
)
