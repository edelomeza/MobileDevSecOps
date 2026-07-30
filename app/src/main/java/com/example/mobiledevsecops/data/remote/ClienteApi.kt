package com.example.mobiledevsecops.data.remote

import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.data.remote.dto.ClienteCreateRequest
import com.example.mobiledevsecops.data.remote.dto.ClienteDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.ClienteDto
import com.example.mobiledevsecops.data.remote.dto.ClienteListResponse
import com.example.mobiledevsecops.data.remote.dto.ClienteUpdateRequest
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

class ClienteApi(
    private val httpClient: HttpClient
) {
    suspend fun getClientes(page: Int, pageSize: Int = PAGE_SIZE): ClienteListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Cliente") {
            parameter("PageNumber", page)
            parameter("PageSize", pageSize)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun getClienteById(id: Int): ClienteDto {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Cliente/$id")
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun buscarClientes(texto: String, page: Int, pageSize: Int = PAGE_SIZE): ClienteListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Cliente/buscar") {
            parameter("texto", texto)
            parameter("PageNumber", page)
            parameter("PageSize", pageSize)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun crearCliente(request: ClienteCreateRequest) {
        val response = httpClient.post("${BuildConfig.BASE_URL}/api/v1/Cliente") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    suspend fun actualizarCliente(request: ClienteUpdateRequest) {
        val response = httpClient.put("${BuildConfig.BASE_URL}/api/v1/Cliente/${request.id}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    suspend fun eliminarCliente(request: ClienteDeleteRequest) {
        val response = httpClient.delete("${BuildConfig.BASE_URL}/api/v1/Cliente/${request.id}") {
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
