package com.example.mobiledevsecops.data.remote

import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.data.remote.dto.ProductCreateRequest
import com.example.mobiledevsecops.data.remote.dto.ProductDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.ProductUpdateRequest
import com.example.mobiledevsecops.data.remote.dto.ProductoListResponse
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

class ProductoApi(
    private val httpClient: HttpClient
) {
    suspend fun getProductos(page: Int, pageSize: Int = PAGE_SIZE): ProductoListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Producto") {
            parameter("PageNumber", page)
            parameter("PageSize", pageSize)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun buscarProductos(texto: String, page: Int, pageSize: Int = PAGE_SIZE): ProductoListResponse {
        val response = httpClient.get("${BuildConfig.BASE_URL}/api/v1/Producto/buscar") {
            parameter("texto", texto)
            parameter("PageNumber", page)
            parameter("PageSize", pageSize)
        }
        if (response.status.value == HTTP_UNAUTHORIZED) throw SessionExpiredException()
        return response.body()
    }

    suspend fun crearProducto(request: ProductCreateRequest) {
        val response = httpClient.post("${BuildConfig.BASE_URL}/api/v1/Producto") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    suspend fun actualizarProducto(request: ProductUpdateRequest) {
        val response = httpClient.put("${BuildConfig.BASE_URL}/api/v1/Producto/${request.id}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status.value) {
            HTTP_UNAUTHORIZED -> throw SessionExpiredException()
            HTTP_CONFLICT -> throw ConflictException()
        }
    }

    suspend fun eliminarProducto(request: ProductDeleteRequest) {
        val response = httpClient.delete("${BuildConfig.BASE_URL}/api/v1/Producto/${request.id}") {
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
