package com.example.mockapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val User: String,
    val Password: String
)

@Serializable
data class LoginResponse(
    val token: String? = null,
    val message: String? = null
)

@Serializable
data class LogoutResponse(
    val message: String? = null
)

@Serializable
data class UsuarioDto(
    val id: Int,
    val strNombre: String,
    val strCorreoElectronico: String,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)

@Serializable
data class UsuarioListResponse(
    @SerialName("Items")
    val items: List<UsuarioDto>,
    @SerialName("TotalCount")
    val totalCount: Int,
    @SerialName("PageNumber")
    val pageNumber: Int,
    @SerialName("PageSize")
    val pageSize: Int,
    @SerialName("TotalPages")
    val totalPages: Int
)

@Serializable
data class UserCreateRequest(
    val strNombre: String,
    val strPWD: String,
    val strCorreoElectronico: String
)

@Serializable
data class UserUpdateRequest(
    val id: Int,
    val strNombre: String,
    val strPWD: String,
    val strCorreoElectronico: String,
    val rowVersion: String
)

@Serializable
data class UserDeleteRequest(
    val id: Int,
    val rowVersion: String
)
