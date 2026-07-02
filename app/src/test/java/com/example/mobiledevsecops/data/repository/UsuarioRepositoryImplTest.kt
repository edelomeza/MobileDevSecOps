package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.data.remote.UsuarioApi
import com.example.mobiledevsecops.data.remote.dto.UserCreateRequest
import com.example.mobiledevsecops.data.remote.dto.UserDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.UserUpdateRequest
import com.example.mobiledevsecops.data.remote.dto.UsuarioListResponse
import com.example.mobiledevsecops.domain.model.UsuarioPage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class UsuarioRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private lateinit var mockEngine: MockEngine
    private lateinit var usuarioApi: UsuarioApi
    private lateinit var repository: UsuarioRepositoryImpl

    @Before
    fun setUp() {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(json.encodeToString(usuarioListResponse)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        usuarioApi = UsuarioApi(httpClient)
        repository = UsuarioRepositoryImpl(usuarioApi)
    }

    @Test
    fun `getUsuarios retorna UsuarioPage correctamente`() = runTest {
        val result = repository.getUsuarios(1, 10)

        assertEquals(1, result.items.size)
        assertEquals(1, result.pageNumber)
        assertEquals(1, result.totalPages)
        assertEquals(1, result.totalCount)
        assertEquals("Juan Pérez", result.items.first().strNombre)
    }

    @Test
    fun `getUsuarios con error 401 lanza SessionExpiredException`() = runTest {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        usuarioApi = UsuarioApi(httpClient)
        repository = UsuarioRepositoryImpl(usuarioApi)

        try {
            repository.getUsuarios(1, 10)
            fail("Expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun `crearUsuario envia request correctamente`() = runTest {
        mockEngine = MockEngine { request ->
            assertEquals("/api/v1/Usuario", request.url.encodedPath)
            assertEquals("POST", request.method.value)

            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        usuarioApi = UsuarioApi(httpClient)
        repository = UsuarioRepositoryImpl(usuarioApi)

        repository.crearUsuario("Juan Pérez", "password123", "juan@example.com")
    }

    @Test
    fun `actualizarUsuario envia request correctamente`() = runTest {
        mockEngine = MockEngine { request ->
            assertTrue(request.url.encodedPath.contains("/api/v1/Usuario/1"))
            assertEquals("PUT", request.method.value)

            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        usuarioApi = UsuarioApi(httpClient)
        repository = UsuarioRepositoryImpl(usuarioApi)

        repository.actualizarUsuario(1, "Nuevo Nombre", "newpass123", "nuevo@example.com", "AAAAAAAAB9E=")
    }

    @Test
    fun `actualizarUsuario con error 409 lanza mensaje de conflicto`() = runTest {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        usuarioApi = UsuarioApi(httpClient)
        repository = UsuarioRepositoryImpl(usuarioApi)

        try {
            repository.actualizarUsuario(1, "N", "p", "c@c.com", "AAAAAAAAB9E=")
            fail("Expected ConflictException")
        } catch (e: ConflictException) {
            assertEquals("Conflicto en la operación", e.message)
        }
    }

    @Test
    fun `actualizarUsuario con error 401 lanza SessionExpiredException`() = runTest {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        usuarioApi = UsuarioApi(httpClient)
        repository = UsuarioRepositoryImpl(usuarioApi)

        try {
            repository.actualizarUsuario(1, "N", "p", "c@c.com", "AAAAAAAAB9E=")
            fail("Expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun `eliminarUsuario envia request correctamente`() = runTest {
        mockEngine = MockEngine { request ->
            assertTrue(request.url.encodedPath.contains("/api/v1/Usuario/1"))
            assertEquals("DELETE", request.method.value)

            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        usuarioApi = UsuarioApi(httpClient)
        repository = UsuarioRepositoryImpl(usuarioApi)

        repository.eliminarUsuario(1, "AAAAAAAAB9E=")
    }

    @Test
    fun `eliminarUsuario con error 409 lanza mensaje de conflicto`() = runTest {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        usuarioApi = UsuarioApi(httpClient)
        repository = UsuarioRepositoryImpl(usuarioApi)

        try {
            repository.eliminarUsuario(1, "AAAAAAAAB9E=")
            fail("Expected ConflictException")
        } catch (e: ConflictException) {
            assertEquals("Conflicto en la operación", e.message)
        }
    }

    @Test
    fun `eliminarUsuario con error 401 lanza SessionExpiredException`() = runTest {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        usuarioApi = UsuarioApi(httpClient)
        repository = UsuarioRepositoryImpl(usuarioApi)

        try {
            repository.eliminarUsuario(1, "AAAAAAAAB9E=")
            fail("Expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    companion object {
        private val usuarioListResponse = UsuarioListResponse(
            items = listOf(
                com.example.mobiledevsecops.data.remote.dto.UsuarioDto(
                    id = 1,
                    strNombre = "Juan Pérez",
                    strCorreoElectronico = "juan@example.com",
                    rowVersion = "AAAAAAAAB9E="
                )
            ),
            totalCount = 1,
            pageNumber = 1,
            pageSize = 10,
            totalPages = 1
        )
    }
}
