package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeClienteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EliminarClienteUseCaseTest {

    private lateinit var fakeRepo: FakeClienteRepository
    private lateinit var useCase: EliminarClienteUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeClienteRepository()
        useCase = EliminarClienteUseCase(fakeRepo)
    }

    @Test
    fun `eliminar cliente con datos validos retorna Success`() = runTest {
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarClienteResult.Success)
    }

    @Test
    fun `eliminar cliente con id invalido retorna ValidationError`() = runTest {
        val result = useCase(0, "AAAAAAAAB9E=")
        assertTrue(result is EliminarClienteResult.ValidationError)
        val error = result as EliminarClienteResult.ValidationError
        assertNotNull(error.errores["id"])
    }

    @Test
    fun `eliminar cliente con rowVersion vacio retorna ValidationError`() = runTest {
        val result = useCase(1, "")
        assertTrue(result is EliminarClienteResult.ValidationError)
        val error = result as EliminarClienteResult.ValidationError
        assertNotNull(error.errores["rowVersion"])
    }

    @Test
    fun `eliminar cliente con error del servidor retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarClienteResult.Error)
    }

    @Test
    fun `eliminar cliente con conflicto retorna Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarClienteResult.Error)
    }

    @Test
    fun `eliminar cliente con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarClienteResult.SessionExpired)
    }

    @Test
    fun `validar retorna errores para id y rowVersion invalidos`() {
        val errores = useCase.validar(0, "")
        assertNotNull(errores["id"])
        assertNotNull(errores["rowVersion"])
    }
}
