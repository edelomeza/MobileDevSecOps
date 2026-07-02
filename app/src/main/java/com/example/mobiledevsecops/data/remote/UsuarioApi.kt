package com.example.mobiledevsecops.data.remote

import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.data.remote.dto.UserCreateRequest
import com.example.mobiledevsecops.data.remote.dto.UserDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.UserUpdateRequest
import com.example.mobiledevsecops.data.remote.dto.UsuarioListResponse
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

class UsuarioApi(
    private val httpClient: HttpClient
) {
    suspend fun getUsuarios(page: Int, pageSize: Int = PAGE_SIZE): UsuarioListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Usuario") {
            parameter("PageNumber", page)
            parameter("PageSize", pageSize)
        }
        if (response.status.value == 401) throw SessionExpiredException()
        return response.body()
    }

    suspend fun crearUsuario(request: UserCreateRequest) {
        val response = httpClient.post("${BuildConfig.BASE_URL}/api/v1/Usuario") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            401 -> throw SessionExpiredException()
            409 -> throw ConflictException()
        }
    }

    suspend fun actualizarUsuario(request: UserUpdateRequest) {
        val response = httpClient.put("${BuildConfig.BASE_URL}/api/v1/Usuario/${request.id}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            401 -> throw SessionExpiredException()
            409 -> throw ConflictException()
        }
    }

    suspend fun eliminarUsuario(request: UserDeleteRequest) {
        val response = httpClient.delete("${BuildConfig.BASE_URL}/api/v1/Usuario/${request.id}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            401 -> throw SessionExpiredException()
            409 -> throw ConflictException()
        }
    }

    companion object {
        const val PAGE_SIZE = 8
    }
}
