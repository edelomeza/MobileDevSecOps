package com.example.mobiledevsecops.data.remote

import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.data.remote.dto.EstadoVentaListResponse
import com.example.mobiledevsecops.data.remote.dto.ProductoAutocompleteDto
import com.example.mobiledevsecops.data.remote.dto.VentaCreateRequest
import com.example.mobiledevsecops.data.remote.dto.VentaDetalleCreateRequest
import com.example.mobiledevsecops.data.remote.dto.VentaDetalleDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.VentaDetalleDto
import com.example.mobiledevsecops.data.remote.dto.VentaDetalleListResponse
import com.example.mobiledevsecops.data.remote.dto.VentaListResponse
import com.example.mobiledevsecops.data.remote.dto.VentaUpdateRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class VentaApi(
    private val httpClient: HttpClient
) {
    suspend fun getVentas(page: Int, pageSize: Int = PAGE_SIZE): VentaListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Venta") {
            parameter("PageNumber", page)
            parameter("PageSize", pageSize)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun buscarVentas(
        strClaveVenta: String?,
        strNombreCliente: String?,
        dteFechaInicio: String?,
        dteFechaFin: String?,
        page: Int,
        pageSize: Int = PAGE_SIZE
    ): VentaListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Venta/buscar") {
            parameter("PageNumber", page)
            parameter("PageSize", pageSize)
            if (!strClaveVenta.isNullOrBlank()) parameter("strClaveVenta", strClaveVenta)
            if (!strNombreCliente.isNullOrBlank()) parameter("strNombreCliente", strNombreCliente)
            if (!dteFechaInicio.isNullOrBlank()) parameter("dteFechaInicio", dteFechaInicio)
            if (!dteFechaFin.isNullOrBlank()) parameter("dteFechaFin", dteFechaFin)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun crearVenta(request: VentaCreateRequest) {
        val response = httpClient.post("${BuildConfig.BASE_URL}/api/v1/Venta") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    suspend fun actualizarEstadoVenta(
        id: Int,
        idCliCliente: Int,
        idSegUsuario: Int,
        idVenCatEstado: Int,
        rowVersion: String?
    ) {
        val response = httpClient.put("${BuildConfig.BASE_URL}/api/v1/Venta/$id") {
            contentType(ContentType.Application.Json)
            setBody(
                VentaUpdateRequest(
                    id = id,
                    idCliCliente = idCliCliente,
                    idSegUsuario = idSegUsuario,
                    idVenCatEstado = idVenCatEstado,
                    rowVersion = rowVersion
                )
            )
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    suspend fun obtenerEstadosVenta(): EstadoVentaListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/EstadoVenta") {
            parameter("PageSize", 50)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun crearVentaDetalle(request: VentaDetalleCreateRequest): VentaDetalleDto {
        val response = httpClient.post("${BuildConfig.BASE_URL}/api/v1/VentaDetalle") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
        }
        return response.body()
    }

    suspend fun buscarProductosAutocomplete(
        texto: String,
        maxResultados: Int = 10
    ): List<ProductoAutocompleteDto> {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/VentaDetalle/autocomplete") {
            parameter("texto", texto)
            parameter("maxResultados", maxResultados)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun eliminarVentaDetalle(id: Int, rowVersion: String?) {
        val response = httpClient.delete("${BuildConfig.BASE_URL}/api/v1/VentaDetalle/$id") {
            contentType(ContentType.Application.Json)
            setBody(VentaDetalleDeleteRequest(id = id, rowVersion = rowVersion))
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    suspend fun getDetallesByVentaId(ventaId: Int): VentaDetalleListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/VentaDetalle") {
            parameter("PageSize", 1000)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    companion object {
        const val PAGE_SIZE = 8
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_CONFLICT = 409
    }
}
