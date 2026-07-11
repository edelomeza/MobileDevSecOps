package com.example.mobiledevsecops.data.remote

import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.data.remote.dto.EmpleadoCreateRequest
import com.example.mobiledevsecops.data.remote.dto.EmpleadoDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.EmpleadoDto
import com.example.mobiledevsecops.data.remote.dto.EmpleadoListResponse
import com.example.mobiledevsecops.data.remote.dto.EmpleadoUpdateRequest
import com.example.mobiledevsecops.data.remote.dto.EmpCatTipoEmpleadoListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class EmpleadoApi(
    private val httpClient: HttpClient
) {
    suspend fun getEmpleados(page: Int, pageSize: Int = PAGE_SIZE): EmpleadoListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Empleado") {
            parameter("PageNumber", page)
            parameter("PageSize", pageSize)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun buscarEmpleados(texto: String?, idTipoEmpleado: Int?, page: Int, pageSize: Int = PAGE_SIZE): EmpleadoListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Empleado/buscar") {
            if (!texto.isNullOrBlank()) parameter("texto", texto)
            if (idTipoEmpleado != null && idTipoEmpleado > 0) parameter("idTipoEmpleado", idTipoEmpleado)
            parameter("PageNumber", page)
            parameter("PageSize", pageSize)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun getEmpleadoById(id: Int): EmpleadoDto {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Empleado/$id")
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun getTiposEmpleado(pageSize: Int = 50): EmpCatTipoEmpleadoListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/TipoEmpleado") {
            parameter("PageNumber", 1)
            parameter("PageSize", pageSize)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun crearEmpleado(request: EmpleadoCreateRequest) {
        val response = httpClient.post("${BuildConfig.BASE_URL}/api/v1/Empleado") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    suspend fun actualizarEmpleado(request: EmpleadoUpdateRequest) {
        val response = httpClient.put("${BuildConfig.BASE_URL}/api/v1/Empleado/${request.id}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    suspend fun eliminarEmpleado(request: EmpleadoDeleteRequest) {
        val response = httpClient.delete("${BuildConfig.BASE_URL}/api/v1/Empleado/${request.id}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    companion object {
        const val PAGE_SIZE = 8
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_CONFLICT = 409
    }
}
