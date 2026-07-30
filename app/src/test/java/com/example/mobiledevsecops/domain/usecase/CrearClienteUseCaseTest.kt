package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeClienteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrearClienteUseCaseTest {

    private lateinit var fakeRepo: FakeClienteRepository
    private lateinit var useCase: CrearClienteUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeClienteRepository()
        useCase = CrearClienteUseCase(fakeRepo)
    }

    @Test
    fun `crear cliente con datos validos retorna Success`() = runTest {
        val result = useCase(
            strNombreCliente = "Juan Pérez",
            strDireccionCliente = "Calle 123",
            strCorreoElectronico = "juan@example.com",
            strNumeroTelefono = "5512345678"
        )
        assertTrue(result is CrearClienteResult.Success)
    }

    @Test
    fun `crear cliente con nombre vacio retorna ValidationError`() = runTest {
        val result = useCase(
            strNombreCliente = "",
            strDireccionCliente = null,
            strCorreoElectronico = "juan@example.com",
            strNumeroTelefono = "5512345678"
        )
        assertTrue(result is CrearClienteResult.ValidationError)
        val error = result as CrearClienteResult.ValidationError
        assertNotNull(error.errores["strNombreCliente"])
    }

    @Test
    fun `crear cliente con correo invalido retorna ValidationError`() = runTest {
        val result = useCase(
            strNombreCliente = "Juan",
            strDireccionCliente = null,
            strCorreoElectronico = "correo-invalido",
            strNumeroTelefono = "5512345678"
        )
        assertTrue(result is CrearClienteResult.ValidationError)
        val error = result as CrearClienteResult.ValidationError
        assertNotNull(error.errores["strCorreoElectronico"])
    }

    @Test
    fun `crear cliente con telefono invalido retorna ValidationError`() = runTest {
        val result = useCase(
            strNombreCliente = "Juan",
            strDireccionCliente = null,
            strCorreoElectronico = "juan@example.com",
            strNumeroTelefono = "12345"
        )
        assertTrue(result is CrearClienteResult.ValidationError)
        val error = result as CrearClienteResult.ValidationError
        assertNotNull(error.errores["strNumeroTelefono"])
    }

    @Test
    fun `crear cliente con error del servidor retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase(
            strNombreCliente = "Juan",
            strDireccionCliente = null,
            strCorreoElectronico = "juan@example.com",
            strNumeroTelefono = "5512345678"
        )
        assertTrue(result is CrearClienteResult.Error)
    }

    @Test
    fun `crear cliente con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(
            strNombreCliente = "Juan",
            strDireccionCliente = null,
            strCorreoElectronico = "juan@example.com",
            strNumeroTelefono = "5512345678"
        )
        assertTrue(result is CrearClienteResult.SessionExpired)
    }

    @Test
    fun `validar retorna errores para todos los campos vacios`() {
        val errores = useCase.validar("", null, "", "")
        assertNotNull(errores["strNombreCliente"])
        assertNotNull(errores["strCorreoElectronico"])
        assertNotNull(errores["strNumeroTelefono"])
    }
}
