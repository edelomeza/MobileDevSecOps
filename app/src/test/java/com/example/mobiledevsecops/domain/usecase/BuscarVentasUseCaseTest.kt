package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeVentaRepository
import com.example.mobiledevsecops.shared.fixture.VentaFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuscarVentasUseCaseTest {

    private val fakeRepo = FakeVentaRepository()
    private val useCase = BuscarVentasUseCase(fakeRepo)

    @Before
    fun setUp() {
        fakeRepo.givenVentas(VentaFixtures.ventasMultiPage)
    }

    @Test
    fun `buscar con clave valida retorna Success`() = runTest {
        val result = useCase("V000000005", null, null, 1)
        assertTrue(result is BuscarVentasResult.Success)
        val success = result as BuscarVentasResult.Success
        assertEquals(1, success.page.totalCount)
        assertEquals("V000000005", success.page.items.first().strClaveVenta)
    }

    @Test
    fun `buscar con nombre de cliente retorna resultados`() = runTest {
        val result = useCase("Cliente 5", null, null, 1)
        assertTrue(result is BuscarVentasResult.Success)
        val success = result as BuscarVentasResult.Success
        assertEquals(1, success.page.totalCount)
        assertEquals("Cliente 5", success.page.items.first().strNombreCliente)
    }

    @Test
    fun `buscar solo con rango de fechas retorna resultados`() = runTest {
        val result = useCase("", "2026-07-01", "2026-07-03", 1)
        assertTrue(result is BuscarVentasResult.Success)
        val success = result as BuscarVentasResult.Success
        assertEquals(3, success.page.totalCount)
    }

    @Test
    fun `buscar con texto vacio y sin fechas retorna ValidationError`() = runTest {
        val result = useCase("", null, null, 1)
        assertTrue(result is BuscarVentasResult.ValidationError)
    }

    @Test
    fun `buscar con texto largo retorna ValidationError`() = runTest {
        val textoLargo = "A".repeat(101)
        val result = useCase(textoLargo, null, null, 1)
        assertTrue(result is BuscarVentasResult.ValidationError)
    }

    @Test
    fun `buscar con fecha inicio mayor que fin retorna ValidationError`() = runTest {
        val result = useCase("", "2026-07-10", "2026-07-01", 1)
        assertTrue(result is BuscarVentasResult.ValidationError)
    }

    @Test
    fun `buscar sin coincidencias retorna lista vacia`() = runTest {
        val result = useCase("xyz123", null, null, 1)
        assertTrue(result is BuscarVentasResult.Success)
        val success = result as BuscarVentasResult.Success
        assertEquals(0, success.page.totalCount)
        assertTrue(success.page.items.isEmpty())
    }

    @Test
    fun `buscar con coincidencia case insensitive`() = runTest {
        val result = useCase("cliente 5", null, null, 1)
        assertTrue(result is BuscarVentasResult.Success)
        val success = result as BuscarVentasResult.Success
        assertEquals(1, success.page.totalCount)
    }

    @Test
    fun `buscar cuando falla el repositorio retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase("Cliente 1", null, null, 1)
        assertTrue(result is BuscarVentasResult.Error)
    }

    @Test
    fun `buscar con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase("Cliente 1", null, null, 1)
        assertTrue(result is BuscarVentasResult.SessionExpired)
    }
}
