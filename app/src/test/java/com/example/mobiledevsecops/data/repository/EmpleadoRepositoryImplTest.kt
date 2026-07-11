package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.EmpleadoApi
import com.example.mobiledevsecops.data.remote.dto.EmpleadoCreateRequest
import com.example.mobiledevsecops.data.remote.dto.EmpleadoDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.EmpleadoDto
import com.example.mobiledevsecops.data.remote.dto.EmpleadoListResponse
import com.example.mobiledevsecops.data.remote.dto.EmpleadoUpdateRequest
import com.example.mobiledevsecops.data.remote.dto.EmpCatTipoEmpleadoDto
import com.example.mobiledevsecops.data.remote.dto.EmpCatTipoEmpleadoListResponse
import com.example.mobiledevsecops.domain.model.Empleado
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EmpleadoRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun createRepository(response: String, status: HttpStatusCode = HttpStatusCode.OK): EmpleadoRepositoryImpl {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(response),
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }
        val api = EmpleadoApi(httpClient)
        return EmpleadoRepositoryImpl(api)
    }

    @Test
    fun `getEmpleados retorna EmpleadoPage correctamente`() = runTest {
        val response = """{
            "Items": [{"id": 1, "strNombre": "Juan", "strAPaterno": "Perez", "strCURP": "HEXA010101HDFLNN01", "RowVersion": "AAAAAAAAB9E="}],
            "TotalCount": 1,
            "PageNumber": 1,
            "PageSize": 10,
            "TotalPages": 1
        }"""
        val repo = createRepository(response)
        val result = repo.getEmpleados(1)
        assertEquals(1, result.items.size)
        assertEquals("Juan", result.items[0].strNombre)
    }

    @Test
    fun `getTiposEmpleado retorna lista de tipos`() = runTest {
        val response = """{
            "Items": [{"id": 1, "strValor": "Desarrollador", "strDescripcion": "Desarrollador de software"}],
            "TotalCount": 1,
            "PageNumber": 1,
            "PageSize": 50,
            "TotalPages": 1
        }"""
        val repo = createRepository(response)
        val result = repo.getTiposEmpleado()
        assertEquals(1, result.size)
        assertEquals("Desarrollador", result[0].strValor)
    }
}
