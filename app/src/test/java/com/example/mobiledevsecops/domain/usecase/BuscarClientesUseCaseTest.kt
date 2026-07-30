package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeClienteRepository
import com.example.mobiledevsecops.shared.fixture.ClienteFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuscarClientesUseCaseTest {

    private lateinit var fakeRepo: FakeClienteRepository
    private lateinit var useCase: BuscarClientesUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeClienteRepository()
        fakeRepo.givenClientes(ClienteFixtures.clientesMultiPage)
        useCase = BuscarClientesUseCase(fakeRepo)
    }

    @Test
    fun `buscar con texto valido retorna resultados`() = runTest {
        val result = useCase("Cliente 5", 1)
        assertTrue(result is BuscarClientesResult.Success)
        val success = result as BuscarClientesResult.Success
        assertEquals(1, success.page.totalCount)
        assertEquals("Cliente 5", success.page.items.first().strNombreCliente)
    }

    @Test
    fun `buscar con texto corto retorna ValidationError`() = runTest {
        val result = useCase("C", 1)
        assertTrue(result is BuscarClientesResult.ValidationError)
    }

    @Test
    fun `buscar con texto largo retorna ValidationError`() = runTest {
        val result = useCase("C".repeat(101), 1)
        assertTrue(result is BuscarClientesResult.ValidationError)
    }

    @Test
    fun `buscar con error de repositorio retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase("Cliente", 1)
        assertTrue(result is BuscarClientesResult.Error)
    }

    @Test
    fun `buscar con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase("Cliente", 1)
        assertTrue(result is BuscarClientesResult.SessionExpired)
    }
}
