package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuscarProductosUseCaseTest {

    private lateinit var fakeRepo: FakeProductoRepository
    private lateinit var useCase: BuscarProductosUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeProductoRepository()
        useCase = BuscarProductosUseCase(fakeRepo)
    }

    @Test
    fun `buscar con texto muy corto retorna ValidationError`() = runTest {
        val result = useCase(texto = "A", page = 1)
        assertTrue(result is BuscarProductosResult.ValidationError)
    }

    @Test
    fun `buscar con texto muy largo retorna ValidationError`() = runTest {
        val result = useCase(texto = "A".repeat(101), page = 1)
        assertTrue(result is BuscarProductosResult.ValidationError)
    }

    @Test
    fun `buscar con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(texto = "Laptop", page = 1)
        assertTrue(result is BuscarProductosResult.SessionExpired)
    }

    @Test
    fun `buscar con error del servidor retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase(texto = "Laptop", page = 1)
        assertTrue(result is BuscarProductosResult.Error)
    }

    @Test
    fun `buscar productos existentes retorna Success con resultados`() = runTest {
        com.example.mobiledevsecops.shared.fixture.ProductoFixtures.productosMultiPage.forEach {
            fakeRepo.crearProducto(
                it.strNombreProducto, it.strURLImagen, it.strDescripcion,
                it.intNumeroExistencia, it.decPrecio
            )
        }
        val result = useCase(texto = "Producto 1", page = 1)
        assertTrue(result is BuscarProductosResult.Success)
        val success = result as BuscarProductosResult.Success
        assertTrue(success.page.items.isNotEmpty())
    }
}
