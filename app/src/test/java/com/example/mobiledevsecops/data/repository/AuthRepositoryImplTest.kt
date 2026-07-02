package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.data.remote.AuthApi
import com.example.mobiledevsecops.data.remote.dto.LoginResponse
import com.example.mobiledevsecops.data.remote.dto.LogoutResponse
import com.example.mobiledevsecops.domain.model.AuthResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private lateinit var mockTokenManager: TokenManager
    private lateinit var mockEngine: MockEngine
    private lateinit var authApi: AuthApi
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        mockTokenManager = mockk(relaxed = true)
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(json.encodeToString(LoginResponse(token = "fake-token"))),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        authApi = AuthApi(httpClient)
        repository = AuthRepositoryImpl(authApi, mockTokenManager)
    }

    @Test
    fun `login exitoso retorna Success y guarda token`() = runTest {
        every { mockTokenManager.saveToken(any()) } returns true

        val result = repository.login("admin", "password123")

        assertEquals(AuthResult.Success, result)
        verify(exactly = 1) { mockTokenManager.saveToken("fake-token") }
    }

    @Test
    fun `login con token JWT invalido retorna Error`() = runTest {
        every { mockTokenManager.saveToken(any()) } returns false

        val result = repository.login("admin", "password123")

        assertTrue(result is AuthResult.Error)
        assertEquals("Token JWT inválido recibido del servidor", (result as AuthResult.Error).message)
    }

    @Test
    fun `login con token nulo retorna Error`() = runTest {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(json.encodeToString(LoginResponse(token = null, message = "Credenciales inválidas"))),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        authApi = AuthApi(httpClient)
        repository = AuthRepositoryImpl(authApi, mockTokenManager)

        val result = repository.login("admin", "wrong")

        assertTrue(result is AuthResult.Error)
        assertEquals("Credenciales inválidas", (result as AuthResult.Error).message)
    }

    @Test
    fun `login con timeout retorna NetworkError`() = runTest {
        mockEngine = MockEngine { _ ->
            throw IOException("Request timeout")
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        authApi = AuthApi(httpClient)
        repository = AuthRepositoryImpl(authApi, mockTokenManager)

        val result = repository.login("admin", "pass")

        assertEquals(AuthResult.NetworkError, result)
    }

    @Test
    fun `login con error de parseo JSON retorna Error`() = runTest {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("{invalid-json}"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        authApi = AuthApi(httpClient)
        repository = AuthRepositoryImpl(authApi, mockTokenManager)

        val result = repository.login("admin", "pass")

        assertTrue(result is AuthResult.Error)
    }

    @Test
    fun `login con error 401 limpia token y retorna SessionExpired`() = runTest {
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
        authApi = AuthApi(httpClient)
        repository = AuthRepositoryImpl(authApi, mockTokenManager)

        every { mockTokenManager.clearAll() } returns Unit

        val result = repository.login("admin", "pass")

        assertEquals(AuthResult.SessionExpired, result)
        verify(exactly = 1) { mockTokenManager.clearAll() }
    }

    @Test
    fun `logout exitoso retorna Success y limpia token`() = runTest {
        every { mockTokenManager.clearAll() } returns Unit
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(json.encodeToString(LogoutResponse(message = "Logout exitoso"))),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        authApi = AuthApi(httpClient)
        repository = AuthRepositoryImpl(authApi, mockTokenManager)

        val result = repository.logout()

        assertEquals(AuthResult.Success, result)
        verify(exactly = 1) { mockTokenManager.clearAll() }
    }

    @Test
    fun `logout con error aun limpia token local y retorna Success`() = runTest {
        every { mockTokenManager.clearAll() } returns Unit
        mockEngine = MockEngine { _ ->
            throw Exception("Network error")
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        authApi = AuthApi(httpClient)
        repository = AuthRepositoryImpl(authApi, mockTokenManager)

        val result = repository.logout()

        assertEquals(AuthResult.Success, result)
        verify(exactly = 1) { mockTokenManager.clearAll() }
    }

    @Test
    fun `isLoggedIn delega al TokenManager`() = runTest {
        every { mockTokenManager.isLoggedIn() } returns true
        assertTrue(repository.isLoggedIn())
        verify(exactly = 1) { mockTokenManager.isLoggedIn() }

        every { mockTokenManager.isLoggedIn() } returns false
        assertEquals(false, repository.isLoggedIn())
    }

    @Test
    fun `getToken delega al TokenManager`() {
        every { mockTokenManager.getToken() } returns "test-token"
        assertEquals("test-token", repository.getToken())
        verify(exactly = 1) { mockTokenManager.getToken() }
    }
}
