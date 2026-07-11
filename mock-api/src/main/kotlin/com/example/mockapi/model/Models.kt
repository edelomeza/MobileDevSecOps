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

@Serializable
data class EmpEmpleadoDto(
    val id: Int,
    val strNombre: String,
    val strAPaterno: String? = null,
    val strAMaterno: String? = null,
    val strCURP: String? = null,
    val idEmpCatTipoEmpleado: Int? = null,
    @SerialName("rowVersion")
    val rowVersion: String? = null
)

@Serializable
data class EmpEmpleadoListResponse(
    @SerialName("Items") val items: List<EmpEmpleadoDto>,
    @SerialName("TotalCount") val totalCount: Int,
    @SerialName("PageNumber") val pageNumber: Int,
    @SerialName("PageSize") val pageSize: Int,
    @SerialName("TotalPages") val totalPages: Int
)

@Serializable
data class EmpEmpleadoCreateRequest(
    val strNombre: String,
    val strAPaterno: String? = null,
    val strAMaterno: String? = null,
    val strCURP: String? = null,
    val idEmpCatTipoEmpleado: Int? = null
)

@Serializable
data class EmpEmpleadoUpdateRequest(
    val id: Int,
    val strNombre: String,
    val strAPaterno: String? = null,
    val strAMaterno: String? = null,
    val strCURP: String? = null,
    val idEmpCatTipoEmpleado: Int? = null,
    val rowVersion: String
)

@Serializable
data class EmpEmpleadoDeleteRequest(
    val id: Int,
    val rowVersion: String
)

@Serializable
data class EmpCatTipoEmpleadoDto(
    val id: Int,
    val strValor: String,
    val strDescripcion: String
)

@Serializable
data class EmpCatTipoEmpleadoListResponse(
    @SerialName("Items") val items: List<EmpCatTipoEmpleadoDto>,
    @SerialName("TotalCount") val totalCount: Int,
    @SerialName("PageNumber") val pageNumber: Int,
    @SerialName("PageSize") val pageSize: Int,
    @SerialName("TotalPages") val totalPages: Int
)
