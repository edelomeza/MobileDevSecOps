package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.domain.model.VentaDetalle
import com.example.mobiledevsecops.shared.fake.FakeVentaRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ObtenerDetallesVentaUseCaseTest {

    private lateinit var fakeRepo: FakeVentaRepository
    private lateinit var useCase: ObtenerDetallesVentaUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeVentaRepository()
        useCase = ObtenerDetallesVentaUseCase(fakeRepo)
    }

    @Test
    fun `obtener detalles con venta id valido retorna Success`() = runTest {
        fakeRepo.givenDetalles(
            listOf(
                VentaDetalle(
                    id = 1,
                    idVenVenta = 1,
                    idProProducto = 1,
                    strNombreProducto = "Producto A",
                    decPrecio = 100.0,
                    intPiezaVenta = 2,
                    decTotalVenta = 200.0
                ),
                VentaDetalle(
                    id = 2,
                    idVenVenta = 1,
                    idProProducto = 2,
                    strNombreProducto = "Producto B",
                    decPrecio = 50.0,
                    intPiezaVenta = 3,
                    decTotalVenta = 150.0
                )
            )
        )

        val result = useCase(1)
        assertTrue(result is ObtenerDetallesResult.Success)
        val success = result as ObtenerDetallesResult.Success
        assertEquals(2, success.detalles.size)
        assertEquals("Producto A", success.detalles[0].strNombreProducto)
        assertEquals("Producto B", success.detalles[1].strNombreProducto)
    }

    @Test
    fun `obtener detalles con venta sin detalles retorna Success con lista vacia`() = runTest {
        val result = useCase(99)
        assertTrue(result is ObtenerDetallesResult.Success)
        val success = result as ObtenerDetallesResult.Success
        assertEquals(0, success.detalles.size)
    }

    @Test
    fun `obtener detalles con id invalido retorna Error`() = runTest {
        val result = useCase(0)
        assertTrue(result is ObtenerDetallesResult.Error)
    }

    @Test
    fun `obtener detalles con error del servidor retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase(1)
        assertTrue(result is ObtenerDetallesResult.Error)
    }

    @Test
    fun `obtener detalles con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(1)
        assertTrue(result is ObtenerDetallesResult.SessionExpired)
    }
}
