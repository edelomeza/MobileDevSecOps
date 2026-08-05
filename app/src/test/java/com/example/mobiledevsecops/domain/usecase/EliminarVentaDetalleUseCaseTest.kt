package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeVentaRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EliminarVentaDetalleUseCaseTest {

    private lateinit var fakeRepo: FakeVentaRepository
    private lateinit var useCase: EliminarVentaDetalleUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeVentaRepository()
        useCase = EliminarVentaDetalleUseCase(fakeRepo)
    }

    @Test
    fun `eliminar detalle con id valido retorna Success`() = runTest {
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarVentaDetalleResult.Success)
        assertEquals(1, fakeRepo.lastEliminarDetalleId)
        assertEquals("AAAAAAAAB9E=", fakeRepo.lastEliminarDetalleRowVersion)
    }

    @Test
    fun `eliminar detalle con id cero retorna Error`() = runTest {
        val result = useCase(0, "AAAAAAAAB9E=")
        assertTrue(result is EliminarVentaDetalleResult.Error)
    }

    @Test
    fun `eliminar detalle con id negativo retorna Error`() = runTest {
        val result = useCase(-1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarVentaDetalleResult.Error)
    }

    @Test
    fun `eliminar detalle con error del servidor retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarVentaDetalleResult.Error)
    }

    @Test
    fun `eliminar detalle con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarVentaDetalleResult.SessionExpired)
    }
}
