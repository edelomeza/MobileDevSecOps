package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeClienteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ActualizarClienteUseCaseTest {

    private lateinit var fakeRepo: FakeClienteRepository
    private lateinit var useCase: ActualizarClienteUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeClienteRepository()
        useCase = ActualizarClienteUseCase(fakeRepo)
    }

    @Test
    fun `actualizar cliente con datos validos retorna Success`() = runTest {
        val result = useCase(
            id = 1,
            strNombreCliente = "Actualizado",
            strDireccionCliente = "Nueva Dir",
            strCorreoElectronico = "actualizado@example.com",
            strNumeroTelefono = "5512345678",
            rowVersion = "AAAAAAAAB9E="
        )
        assertTrue(result is ActualizarClienteResult.Success)
    }

    @Test
    fun `actualizar cliente con id invalido retorna ValidationError`() = runTest {
        val result = useCase(
            id = 0,
            strNombreCliente = "Test",
            strDireccionCliente = null,
            strCorreoElectronico = "test@example.com",
            strNumeroTelefono = "5512345678",
            rowVersion = "AAAAAAAAB9E="
        )
        assertTrue(result is ActualizarClienteResult.ValidationError)
        val error = result as ActualizarClienteResult.ValidationError
        assertNotNull(error.errores["id"])
    }

    @Test
    fun `actualizar cliente con rowVersion vacio retorna ValidationError`() = runTest {
        val result = useCase(
            id = 1,
            strNombreCliente = "Test",
            strDireccionCliente = null,
            strCorreoElectronico = "test@example.com",
            strNumeroTelefono = "5512345678",
            rowVersion = ""
        )
        assertTrue(result is ActualizarClienteResult.ValidationError)
        val error = result as ActualizarClienteResult.ValidationError
        assertNotNull(error.errores["rowVersion"])
    }

    @Test
    fun `validar retorna error para nombre vacio`() {
        val errores = useCase.validar(1, "", null, "correo@example.com", "5512345678", "AAAAAAAAB9E=")
        assertNotNull(errores["strNombreCliente"])
    }

    @Test
    fun `actualizar cliente con error del servidor retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase(
            id = 1,
            strNombreCliente = "Test",
            strDireccionCliente = null,
            strCorreoElectronico = "test@example.com",
            strNumeroTelefono = "5512345678",
            rowVersion = "AAAAAAAAB9E="
        )
        assertTrue(result is ActualizarClienteResult.Error)
    }

    @Test
    fun `actualizar cliente con conflicto retorna Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        val result = useCase(
            id = 1,
            strNombreCliente = "Test",
            strDireccionCliente = null,
            strCorreoElectronico = "test@example.com",
            strNumeroTelefono = "5512345678",
            rowVersion = "AAAAAAAAB9E="
        )
        assertTrue(result is ActualizarClienteResult.Error)
    }

    @Test
    fun `actualizar cliente con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(
            id = 1,
            strNombreCliente = "Test",
            strDireccionCliente = null,
            strCorreoElectronico = "test@example.com",
            strNumeroTelefono = "5512345678",
            rowVersion = "AAAAAAAAB9E="
        )
        assertTrue(result is ActualizarClienteResult.SessionExpired)
    }
}
