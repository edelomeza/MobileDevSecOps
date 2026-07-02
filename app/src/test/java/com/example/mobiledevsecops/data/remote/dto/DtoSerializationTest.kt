package com.example.mobiledevsecops.data.remote.dto

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DtoSerializationTest {

    private lateinit var json: Json

    @Before
    fun setUp() {
        json = Json { ignoreUnknownKeys = true; isLenient = true }
    }

    @Test
    fun `loginRequest se serializa correctamente`() {
        val request = LoginRequest(User = "admin", Password = "pass123")
        val jsonString = json.encodeToString(request)

        assertTrue(jsonString.contains("\"User\""))
        assertTrue(jsonString.contains("\"Password\""))
        assertTrue(jsonString.contains("admin"))
        assertTrue(jsonString.contains("pass123"))
    }

    @Test
    fun `loginRequest se serializa con campos vacios`() {
        val request = LoginRequest(User = "", Password = "")
        val jsonString = json.encodeToString(request)

        assertTrue(jsonString.contains("\"User\":\"\""))
        assertTrue(jsonString.contains("\"Password\":\"\""))
    }

    @Test
    fun `loginResponse se deserializa con token`() {
        val jsonString = """{"token":"abc123","message":"ok"}"""
        val response = json.decodeFromString<LoginResponse>(jsonString)

        assertEquals("abc123", response.token)
        assertEquals("ok", response.message)
    }

    @Test
    fun `loginResponse se deserializa con token nulo`() {
        val jsonString = """{"message":"Credenciales inválidas"}"""
        val response = json.decodeFromString<LoginResponse>(jsonString)

        assertNull(response.token)
        assertEquals("Credenciales inválidas", response.message)
    }

    @Test
    fun `loginResponse se deserializa con ambos campos nulos`() {
        val jsonString = """{}"""
        val response = json.decodeFromString<LoginResponse>(jsonString)

        assertNull(response.token)
        assertNull(response.message)
    }

    @Test
    fun `loginResponse con campos desconocidos no falla`() {
        val jsonString = """{"token":"x","message":"y","extraField":"value"}"""
        val response = json.decodeFromString<LoginResponse>(jsonString)

        assertEquals("x", response.token)
        assertEquals("y", response.message)
    }

    @Test(expected = SerializationException::class)
    fun `loginResponse con json invalido lanza excepcion`() {
        val jsonString = """not valid json at all"""
        json.decodeFromString<LoginResponse>(jsonString)
    }

    @Test
    fun `logoutResponse se deserializa correctamente`() {
        val jsonString = """{"message":"Logout exitoso"}"""
        val response = json.decodeFromString<LogoutResponse>(jsonString)

        assertEquals("Logout exitoso", response.message)
    }

    @Test
    fun `logoutResponse se deserializa con message nulo`() {
        val jsonString = """{}"""
        val response = json.decodeFromString<LogoutResponse>(jsonString)

        assertNull(response.message)
    }

    @Test
    fun `usuarioDto se deserializa con rowVersion`() {
        val jsonString = """{"id":1,"strNombre":"Juan","strCorreoElectronico":"juan@test.com","RowVersion":"abc123"}"""
        val dto = json.decodeFromString<UsuarioDto>(jsonString)

        assertEquals(1, dto.id)
        assertEquals("Juan", dto.strNombre)
        assertEquals("juan@test.com", dto.strCorreoElectronico)
        assertEquals("abc123", dto.rowVersion)
    }

    @Test
    fun `usuarioDto se deserializa sin rowVersion`() {
        val jsonString = """{"id":2,"strNombre":"Maria","strCorreoElectronico":"maria@test.com"}"""
        val dto = json.decodeFromString<UsuarioDto>(jsonString)

        assertEquals(2, dto.id)
        assertEquals("Maria", dto.strNombre)
        assertEquals("maria@test.com", dto.strCorreoElectronico)
        assertNull(dto.rowVersion)
    }

    @Test
    fun `usuarioDto rowVersion nulo se mapea a vacio segun logica del repositorio`() {
        val jsonString = """{"id":3,"strNombre":"Pedro","strCorreoElectronico":"pedro@test.com"}"""
        val dto = json.decodeFromString<UsuarioDto>(jsonString)
        val rowVersion = dto.rowVersion ?: ""

        assertEquals(3, dto.id)
        assertEquals("Pedro", dto.strNombre)
        assertEquals("pedro@test.com", dto.strCorreoElectronico)
        assertEquals("", rowVersion)
    }

    @Test
    fun `usuarioDto rowVersion presente se usa directamente`() {
        val jsonString = """{"id":4,"strNombre":"Ana","strCorreoElectronico":"ana@test.com","RowVersion":"v99"}"""
        val dto = json.decodeFromString<UsuarioDto>(jsonString)
        val rowVersion = dto.rowVersion ?: ""

        assertEquals(4, dto.id)
        assertEquals("Ana", dto.strNombre)
        assertEquals("ana@test.com", dto.strCorreoElectronico)
        assertEquals("v99", rowVersion)
    }

    @Test
    fun `usuarioListResponse se deserializa correctamente`() {
        val jsonString = """{"Items":[{"id":1,"strNombre":"Juan","strCorreoElectronico":"juan@test.com","RowVersion":"v1"}],"TotalCount":1,"PageNumber":1,"PageSize":8,"TotalPages":1}"""
        val response = json.decodeFromString<UsuarioListResponse>(jsonString)

        assertEquals(1, response.items.size)
        assertEquals(1, response.totalCount)
        assertEquals(1, response.pageNumber)
        assertEquals(8, response.pageSize)
        assertEquals(1, response.totalPages)
        assertEquals("Juan", response.items[0].strNombre)
    }

    @Test
    fun `usuarioListResponse se deserializa con lista vacia`() {
        val jsonString = """{"Items":[],"TotalCount":0,"PageNumber":1,"PageSize":8,"TotalPages":0}"""
        val response = json.decodeFromString<UsuarioListResponse>(jsonString)

        assertTrue(response.items.isEmpty())
        assertEquals(0, response.totalCount)
        assertEquals(0, response.totalPages)
    }

    @Test
    fun `userCreateRequest se serializa correctamente`() {
        val request = UserCreateRequest(strNombre = "Carlos", strPWD = "securePass1", strCorreoElectronico = "carlos@test.com")
        val jsonString = json.encodeToString(request)

        assertTrue(jsonString.contains("Carlos"))
        assertTrue(jsonString.contains("securePass1"))
        assertTrue(jsonString.contains("carlos@test.com"))
    }

    @Test
    fun `userCreateRequest con datos especiales`() {
        val request = UserCreateRequest(strNombre = "O'Brien 100%", strPWD = "p@ss w0rd", strCorreoElectronico = "o'brien+tag@test.co.uk")
        val jsonString = json.encodeToString(request)

        assertNotNull(json.decodeFromString<UserCreateRequest>(jsonString))
    }

    @Test
    fun `userUpdateRequest se serializa correctamente`() {
        val request = UserUpdateRequest(id = 5, strNombre = "Laura", strPWD = "newPass456", strCorreoElectronico = "laura@test.com", rowVersion = "v2")
        val jsonString = json.encodeToString(request)

        assertTrue(jsonString.contains("\"id\":5"))
        assertTrue(jsonString.contains("Laura"))
        assertTrue(jsonString.contains("newPass456"))
        assertTrue(jsonString.contains("v2"))
    }

    @Test
    fun `userDeleteRequest se serializa correctamente`() {
        val request = UserDeleteRequest(id = 10, rowVersion = "v3")
        val jsonString = json.encodeToString(request)

        assertTrue(jsonString.contains("\"id\":10"))
        assertTrue(jsonString.contains("v3"))
    }
}
