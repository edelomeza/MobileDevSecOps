package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.ProductoApi
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.shared.fixture.ProductoFixtures
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductoRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    lateinit var productoApi: ProductoApi

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `getProductos success`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(json.encodeToString(ProductoFixtures.productoListResponse)),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        val result = repo.getProductos(1)

        assertEquals(1, result.items.size)
        assertEquals("Laptop HP", result.items.first().strNombreProducto)
    }

    @Test
    fun `getProductos con 401 lanza SessionExpiredException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.getProductos(1)
            assertTrue(false)
        } catch (e: SessionExpiredException) {
            assertTrue(true)
        }
    }

    @Test
    fun `buscarProductos success`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(json.encodeToString(ProductoFixtures.productoListResponse)),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        val result = repo.buscarProductos("Laptop", 1)

        assertEquals(1, result.items.size)
        assertEquals("Laptop HP", result.items.first().strNombreProducto)
    }

    @Test
    fun `buscarProductos con 401 lanza SessionExpiredException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.buscarProductos("Laptop", 1)
            assertTrue(false)
        } catch (e: SessionExpiredException) {
            assertTrue(true)
        }
    }

    @Test
    fun `crearProducto envia request a Producto endpoint`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/api/v1/Producto", request.url.encodedPath)
            assertEquals("POST", request.method.value)

            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        repo.crearProducto("Nuevo Producto", "https://img.com", "Descripción", 10, 100.00)
    }

    @Test
    fun `actualizarProducto envia request a Producto endpoint`() = runTest {
        val engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.contains("/api/v1/Producto/1"))
            assertEquals("PUT", request.method.value)

            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        repo.actualizarProducto(1, "Actualizado", "https://img.com", "Desc", 5, 200.00, "AAAAAAAAB9E=")
    }

    @Test
    fun `actualizarProducto con 409 lanza ConflictException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Conflict
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.actualizarProducto(1, "Test", null, null, 10, 100.00, "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: ConflictException) {
            assertTrue(true)
        }
    }

    @Test
    fun `actualizarProducto con 401 lanza SessionExpiredException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.actualizarProducto(1, "Test", null, null, 10, 100.00, "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: SessionExpiredException) {
            assertTrue(true)
        }
    }

    @Test
    fun `eliminarProducto envia request a Producto endpoint`() = runTest {
        val engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.contains("/api/v1/Producto/1"))
            assertEquals("DELETE", request.method.value)

            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        repo.eliminarProducto(1, "AAAAAAAAB9E=")
    }

    @Test
    fun `eliminarProducto con 401 lanza SessionExpiredException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.eliminarProducto(1, "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: SessionExpiredException) {
            assertTrue(true)
        }
    }

    @Test
    fun `eliminarProducto con 409 lanza ConflictException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Conflict
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.eliminarProducto(1, "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: ConflictException) {
            assertTrue(true)
        }
    }

    @Test
    fun `crearProducto con 500 lanza excepcion`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.crearProducto("Nuevo Producto", null, null, 10, 100.00)
            assertTrue(false)
        } catch (e: Exception) {
            assertTrue(true)
        }
    }

    @Test
    fun `crearProducto con 400 lanza excepcion`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.BadRequest
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.crearProducto("Nuevo Producto", null, null, 10, 100.00)
            assertTrue(false)
        } catch (e: Exception) {
            assertTrue(true)
        }
    }

    @Test
    fun `actualizarProducto con 404 lanza excepcion`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.actualizarProducto(99, "Test", null, null, 10, 100.00, "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: Exception) {
            assertTrue(true)
        }
    }

    @Test
    fun `eliminarProducto con 404 lanza excepcion`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ProductoApi(httpClient)
        val repo = ProductoRepositoryImpl(api)

        try {
            repo.eliminarProducto(99, "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: Exception) {
            assertTrue(true)
        }
    }
}
