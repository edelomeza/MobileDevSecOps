package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.ClienteApi
import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.shared.fixture.ClienteFixtures
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

class ClienteRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    lateinit var clienteApi: ClienteApi

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `getClientes success`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(json.encodeToString(ClienteFixtures.clienteListResponse)),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        val result = repo.getClientes(1)

        assertEquals(1, result.items.size)
        assertEquals("Juan Pérez", result.items.first().strNombreCliente)
    }

    @Test
    fun `getClientes con 401 lanza SessionExpiredException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        try {
            repo.getClientes(1)
            assertTrue(false)
        } catch (e: SessionExpiredException) {
            assertTrue(true)
        }
    }

    @Test
    fun `buscarClientes success`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(json.encodeToString(ClienteFixtures.clienteListResponse)),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        val result = repo.buscarClientes("Juan", 1)

        assertEquals(1, result.items.size)
        assertEquals("Juan Pérez", result.items.first().strNombreCliente)
    }

    @Test
    fun `buscarClientes con 401 lanza SessionExpiredException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        try {
            repo.buscarClientes("Juan", 1)
            assertTrue(false)
        } catch (e: SessionExpiredException) {
            assertTrue(true)
        }
    }

    @Test
    fun `crearCliente envia request a Cliente endpoint`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/api/v1/Cliente", request.url.encodedPath)
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
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        repo.crearCliente("Nuevo Cliente", "Dirección", "nuevo@example.com", "5512345678")
    }

    @Test
    fun `actualizarCliente envia request a Cliente endpoint`() = runTest {
        val engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.contains("/api/v1/Cliente/1"))
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
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        repo.actualizarCliente(1, "Actualizado", "Dir", "correo@example.com", "5512345678", "AAAAAAAAB9E=")
    }

    @Test
    fun `actualizarCliente con 409 lanza ConflictException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Conflict
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        try {
            repo.actualizarCliente(1, "Test", "Dir", "correo@example.com", "5512345678", "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: ConflictException) {
            assertTrue(true)
        }
    }

    @Test
    fun `actualizarCliente con 401 lanza SessionExpiredException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        try {
            repo.actualizarCliente(1, "Test", "Dir", "correo@example.com", "5512345678", "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: SessionExpiredException) {
            assertTrue(true)
        }
    }

    @Test
    fun `eliminarCliente envia request a Cliente endpoint`() = runTest {
        val engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.contains("/api/v1/Cliente/1"))
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
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        repo.eliminarCliente(1, "AAAAAAAAB9E=")
    }

    @Test
    fun `eliminarCliente con 401 lanza SessionExpiredException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        try {
            repo.eliminarCliente(1, "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: SessionExpiredException) {
            assertTrue(true)
        }
    }

    @Test
    fun `eliminarCliente con 409 lanza ConflictException`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.Conflict
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val api = ClienteApi(httpClient)
        val repo = ClienteRepositoryImpl(api)

        try {
            repo.eliminarCliente(1, "AAAAAAAAB9E=")
            assertTrue(false)
        } catch (e: ConflictException) {
            assertTrue(true)
        }
    }
}
