package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VentaDto(
    val id: Int,
    @SerialName("idCliCliente")
    val idCliCliente: Int = 0,
    val strNombreCliente: String? = null,
    @SerialName("idSegUsuario")
    val idSegUsuario: Int = 0,
    val strNombreUsuario: String? = null,
    @SerialName("idVenCatEstado")
    val idVenCatEstado: Int = 0,
    @SerialName("strEstado")
    val strEstado: String? = null,
    val dteFechaHoraCompra: String? = null,
    val strClaveVenta: String,
    @SerialName("RowVersion")
    val rowVersion: String? = null
)

@Serializable
data class VentaListResponse(
    @SerialName("Items")
    val items: List<VentaDto>,
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
data class EstadoVentaDto(
    val id: Int,
    @SerialName("strValor")
    val strValor: String
)

@Serializable
data class EstadoVentaListResponse(
    @SerialName("Items")
    val items: List<EstadoVentaDto>,
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
data class VentaDetalleDeleteRequest(
    val id: Int,
    @SerialName("RowVersion")
    val rowVersion: String? = null
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
