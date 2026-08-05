package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.data.remote.VentaApi
import com.example.mobiledevsecops.shared.fixture.VentaFixtures
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class VentaRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private lateinit var mockEngine: MockEngine
    private lateinit var ventaApi: VentaApi
    private lateinit var repository: VentaRepositoryImpl

    @Before
    fun setUp() {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(json.encodeToString(VentaFixtures.ventaListResponse)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)
    }

    @Test
    fun `getVentas retorna VentaPage correctamente`() = runTest {
        val result = repository.getVentas(1, 10)

        assertEquals(1, result.items.size)
        assertEquals(1, result.pageNumber)
        assertEquals(1, result.totalPages)
        assertEquals(1, result.totalCount)
        assertEquals("V001001001", result.items.first().strClaveVenta)
        assertEquals("Juan Perez", result.items.first().strNombreCliente)
        assertEquals("Pagada", result.items.first().strEstado)
    }

    @Test
    fun `getVentas con error 401 lanza SessionExpiredException`() = runTest {
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
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        try {
            repository.getVentas(1, 10)
            fail("Expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun `buscarVentas envia request con parametros correctos`() = runTest {
        mockEngine = MockEngine { request ->
            assertEquals("/api/v1/Venta/buscar", request.url.encodedPath)
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("V001001001", request.url.parameters["strClaveVenta"])
            assertEquals("Juan Perez", request.url.parameters["strNombreCliente"])
            assertEquals("2026-07-01", request.url.parameters["dteFechaInicio"])
            assertEquals("2026-07-31", request.url.parameters["dteFechaFin"])
            assertEquals("2", request.url.parameters["PageNumber"])

            respond(
                content = ByteReadChannel(
                    json.encodeToString(
                        VentaFixtures.ventaListResponse.copy(pageNumber = 2)
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        val result = repository.buscarVentas(
            strClaveVenta = "V001001001",
            strNombreCliente = "Juan Perez",
            dteFechaInicio = "2026-07-01",
            dteFechaFin = "2026-07-31",
            page = 2,
            pageSize = 10
        )

        assertEquals(1, result.items.size)
        assertEquals(2, result.pageNumber)
    }

    @Test
    fun `buscarVentas con error 401 lanza SessionExpiredException`() = runTest {
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
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        try {
            repository.buscarVentas("V001001001", null, null, null, 1, 10)
            fail("Expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun `crearVenta envia request correctamente`() = runTest {
        mockEngine = MockEngine { request ->
            assertEquals("/api/v1/Venta", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)

            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        repository.crearVenta(
            idCliCliente = 1,
            idSegUsuario = 2,
            dteFechaHoraCompra = "2026-07-01T10:15:00",
            strClaveVenta = "V001001001"
        )
    }

    @Test
    fun `crearVenta con error 401 lanza SessionExpiredException`() = runTest {
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
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        try {
            repository.crearVenta(1, 2, "2026-07-01T10:15:00", "V001001001")
            fail("Expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun `crearVenta con error 409 lanza mensaje de conflicto`() = runTest {
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
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        try {
            repository.crearVenta(1, 2, "2026-07-01T10:15:00", "V001001001")
            fail("Expected ConflictException")
        } catch (e: ConflictException) {
            assertEquals("Conflicto en la operación", e.message)
        }
    }

    @Test
    fun `getVentas con contrato real del backend deserializa correctamente`() = runTest {
        mockEngine = MockEngine { request ->
            assertEquals("/api/v1/Venta", request.url.encodedPath)
            respond(
                content = ByteReadChannel(
                    json.encodeToString(
                        VentaFixtures.ventaListResponse.copy(
                            items = listOf(
                                VentaFixtures.ventaDto.copy(
                                    strNombreCliente = "Juan Perez",
                                    strEstado = "Abierta"
                                )
                            )
                        )
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        val result = repository.getVentas(1, 10)

        assertEquals(1, result.items.size)
        assertEquals("Abierta", result.items.first().strEstado)
        assertEquals("Juan Perez", result.items.first().strNombreCliente)
    }

    @Test
    fun `crearVenta con dto mapea rowVersion nulo a cadena vacia`() = runTest {
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(
                    json.encodeToString(
                        VentaFixtures.ventaListResponse.copy(
                            items = listOf(
                                VentaFixtures.ventaDto.copy(rowVersion = null)
                            )
                        )
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        val result = repository.getVentas(1, 10)

        assertTrue(result.items.first().rowVersion.isEmpty())
    }

    @Test
    fun `eliminarVentaDetalle envia request correctamente`() = runTest {
        mockEngine = MockEngine { request ->
            assertEquals("/api/v1/VentaDetalle/1", request.url.encodedPath)
            assertEquals(HttpMethod.Delete, request.method)
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        repository.eliminarVentaDetalle(1, "AAAAAAAAB9E=")
    }

    @Test
    fun `eliminarVentaDetalle con error 401 lanza SessionExpiredException`() = runTest {
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
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        try {
            repository.eliminarVentaDetalle(1, "AAAAAAAAB9E=")
            fail("Expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun `eliminarVentaDetalle con error 409 lanza ConflictException`() = runTest {
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
        ventaApi = VentaApi(httpClient)
        repository = VentaRepositoryImpl(ventaApi)

        try {
            repository.eliminarVentaDetalle(1, "AAAAAAAAB9E=")
            fail("Expected ConflictException")
        } catch (e: ConflictException) {
            // expected
        }
    }
}
