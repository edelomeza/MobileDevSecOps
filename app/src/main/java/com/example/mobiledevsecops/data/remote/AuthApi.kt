package com.example.mobiledevsecops.data.remote

import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.data.remote.dto.LoginRequest
import com.example.mobiledevsecops.data.remote.dto.LoginResponse
import com.example.mobiledevsecops.data.remote.dto.LogoutResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(
    private val httpClient: HttpClient
) {
    suspend fun login(username: String, password: String): LoginResponse {
        val response = httpClient.post("${BuildConfig.BASE_URL}/api/v1/Login/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }
        if (response.status.value !in HTTP_OK..HTTP_PARTIAL_CONTENT) {
            val errorBody = try {
                response.body<LoginResponse>()
            } catch (e: Exception) {
                null
            }
            if (response.status.value == HTTP_UNAUTHORIZED && errorBody?.message.isNullOrBlank()) {
                throw SessionExpiredException()
            }
            return errorBody ?: LoginResponse(token = null, message = null)
        }
        return response.body()
    }

    companion object {
        private const val HTTP_OK = 200
        private const val HTTP_PARTIAL_CONTENT = 299
        private const val HTTP_UNAUTHORIZED = 401
    }

    suspend fun logout(): LogoutResponse {
        val response = httpClient.post("${BuildConfig.BASE_URL}/api/v1/Logout/logout") {
            contentType(ContentType.Application.Json)
        }
        return response.body()
    }
}
