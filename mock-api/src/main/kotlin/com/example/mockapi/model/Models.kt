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

@Serializable
data class ProductoDto(
    val id: Int,
    val strNombreProducto: String,
    val strURLImagen: String? = null,
    val strDescripcion: String? = null,
    val intNumeroExistencia: Int,
    val decPrecio: Double,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)

@Serializable
data class ProductoListResponse(
    @SerialName("Items") val items: List<ProductoDto>,
    @SerialName("TotalCount") val totalCount: Int,
    @SerialName("PageNumber") val pageNumber: Int,
    @SerialName("PageSize") val pageSize: Int,
    @SerialName("TotalPages") val totalPages: Int
)

@Serializable
data class ProductCreateRequest(
    val strNombreProducto: String,
    val strURLImagen: String? = null,
    val strDescripcion: String? = null,
    val intNumeroExistencia: Int,
    val decPrecio: Double
)

@Serializable
data class ProductUpdateRequest(
    val id: Int,
    val strNombreProducto: String,
    val strURLImagen: String? = null,
    val strDescripcion: String? = null,
    val intNumeroExistencia: Int,
    val decPrecio: Double,
    val rowVersion: String
)

@Serializable
data class ProductDeleteRequest(
    val id: Int,
    val rowVersion: String
)

@Serializable
data class ClienteDto(
    val id: Int,
    val strNombreCliente: String,
    val strDireccionCliente: String? = null,
    val strCorreoElectronico: String,
    val strNumeroTelefono: String,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)

@Serializable
data class ClienteListResponse(
    @SerialName("Items") val items: List<ClienteDto>,
    @SerialName("TotalCount") val totalCount: Int,
    @SerialName("PageNumber") val pageNumber: Int,
    @SerialName("PageSize") val pageSize: Int,
    @SerialName("TotalPages") val totalPages: Int
)

@Serializable
data class VenCatEstadoDto(
    val id: Int,
    val strValor: String,
    val strDescripcion: String
)

@Serializable
data class VenCatEstadoListResponse(
    @SerialName("Items") val items: List<VenCatEstadoDto>,
    @SerialName("TotalCount") val totalCount: Int,
    @SerialName("PageNumber") val pageNumber: Int,
    @SerialName("PageSize") val pageSize: Int,
    @SerialName("TotalPages") val totalPages: Int
)

@Serializable
data class VentaDto(
    val id: Int,
    val strClaveVenta: String,
    val dteFechaHoraCompra: String? = null,
    val strNombreCliente: String? = null,
    @SerialName("strEstado")
    val strEstado: String? = null,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)

@Serializable
data class VentaListResponse(
    @SerialName("Items") val items: List<VentaDto>,
    @SerialName("TotalCount") val totalCount: Int,
    @SerialName("PageNumber") val pageNumber: Int,
    @SerialName("PageSize") val pageSize: Int,
    @SerialName("TotalPages") val totalPages: Int
)

@Serializable
data class VentaCreateRequest(
    val idCliCliente: Int,
    val idSegUsuario: Int,
    val dteFechaHoraCompra: String,
    val strClaveVenta: String
)

@Serializable
data class VentaUpdateRequest(
    val id: Int,
    val idCliCliente: Int,
    val idSegUsuario: Int,
    @SerialName("idVenCatEstado")
    val idVenCatEstado: Int,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)

@Serializable
data class ProductoAutocompleteDto(
    val id: Int,
    @SerialName("strTextoAutocomplete")
    val strTextoAutocomplete: String
)

@Serializable
data class VentaDetalleDto(
    val id: Int,
    @SerialName("idVenVenta")
    val idVenVenta: Int,
    @SerialName("idProProducto")
    val idProProducto: Int,
    @SerialName("strNombreProducto")
    val strNombreProducto: String? = null,
    @SerialName("decPrecio")
    val decPrecio: Double = 0.0,
    @SerialName("intPiezaVenta")
    val intPiezaVenta: Int,
    @SerialName("decTotalVenta")
    val decTotalVenta: Double = 0.0,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)

@Serializable
data class VentaDetalleCreateRequest(
    @SerialName("idVenVenta")
    val idVenVenta: Int,
    @SerialName("idProProducto")
    val idProProducto: Int,
    @SerialName("intPiezaVenta")
    val intPiezaVenta: Int
)

@Serializable
data class VentaDetalleListResponse(
    @SerialName("Items")
    val items: List<VentaDetalleDto>,
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
data class VentaDetalleDeleteRequest(
    val id: Int,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)
