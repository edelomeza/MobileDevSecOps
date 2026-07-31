package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EliminarProductoUseCaseTest {

    private lateinit var fakeRepo: FakeProductoRepository
    private lateinit var useCase: EliminarProductoUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeProductoRepository()
        useCase = EliminarProductoUseCase(fakeRepo)
    }

    @Test
    fun `eliminar producto con datos validos retorna Success`() = runTest {
        val result = useCase(id = 1, rowVersion = "AAAAAAAAB9E=")
        assertTrue(result is EliminarProductoResult.Success)
    }

    @Test
    fun `eliminar producto con id invalido retorna ValidationError`() = runTest {
        val result = useCase(id = 0, rowVersion = "AAAAAAAAB9E=")
        assertTrue(result is EliminarProductoResult.ValidationError)
        val error = result as EliminarProductoResult.ValidationError
        assertNotNull(error.errores["id"])
    }

    @Test
    fun `eliminar producto con rowVersion vacio retorna ValidationError`() = runTest {
        val result = useCase(id = 1, rowVersion = "")
        assertTrue(result is EliminarProductoResult.ValidationError)
        val error = result as EliminarProductoResult.ValidationError
        assertNotNull(error.errores["rowVersion"])
    }

    @Test
    fun `eliminar producto con conflicto retorna Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        val result = useCase(id = 1, rowVersion = "AAAAAAAAB9E=")
        assertTrue(result is EliminarProductoResult.Error)
    }

    @Test
    fun `eliminar producto con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(id = 1, rowVersion = "AAAAAAAAB9E=")
        assertTrue(result is EliminarProductoResult.SessionExpired)
    }

    @Test
    fun `eliminar producto con error del servidor retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase(id = 1, rowVersion = "AAAAAAAAB9E=")
        assertTrue(result is EliminarProductoResult.Error)
    }
}
