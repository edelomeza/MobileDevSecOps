package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeVentaRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrearVentaUseCaseTest {

    private val fakeRepo = FakeVentaRepository()
    private val useCase = CrearVentaUseCase(fakeRepo)

    @Before
    fun setUp() {
        fakeRepo.givenVentas(emptyList())
    }

    @Test
    fun `crear con campos validos retorna Success y envia datos`() = runTest {
        val result = useCase(1, 2)

        assertTrue(result is CrearVentaResult.Success)
        assertEquals(1, fakeRepo.lastIdCliCliente)
        assertEquals(2, fakeRepo.lastIdSegUsuario)
        assertNotNull(fakeRepo.lastDteFechaHoraCompra)
        assertNotNull(fakeRepo.lastStrClaveVenta)
    }

    @Test
    fun `crear genera clave con formato correcto`() = runTest {
        useCase(1, 2)

        val clave = fakeRepo.ventas.first().strClaveVenta

        assertTrue(clave.startsWith("V"))
        assertEquals(10, clave.length)
        assertTrue(clave.all { it.isDigit() } || clave.drop(1).all { it.isDigit() })
    }

    @Test
    fun `crear genera fecha hora ISO correcta`() = runTest {
        useCase(1, 2)

        val fecha = fakeRepo.ventas.first().dteFechaHoraCompra
        assertNotNull(fecha)
        assertTrue((fecha ?: "").matches(Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}""")))
    }

    @Test
    fun `crear con idCliCliente invalido retorna ValidationError`() = runTest {
        val result = useCase(0, 2)

        assertTrue(result is CrearVentaResult.ValidationError)
        val errors = (result as CrearVentaResult.ValidationError).errores
        assertEquals("Debe seleccionar un cliente", errors["idCliCliente"])
    }

    @Test
    fun `crear con idSegUsuario invalido retorna ValidationError`() = runTest {
        val result = useCase(1, 0)

        assertTrue(result is CrearVentaResult.ValidationError)
        val errors = (result as CrearVentaResult.ValidationError).errores
        assertEquals("Debe seleccionar un usuario", errors["idSegUsuario"])
    }

    @Test
    fun `crear con ambos ids invalidos retorna ValidationError`() = runTest {
        val result = useCase(0, 0)

        assertTrue(result is CrearVentaResult.ValidationError)
        val errors = (result as CrearVentaResult.ValidationError).errores
        assertEquals(2, errors.size)
    }

    @Test
    fun `crear con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true

        val result = useCase(1, 2)

        assertTrue(result is CrearVentaResult.SessionExpired)
    }

    @Test
    fun `crear cuando falla el repositorio retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true

        val result = useCase(1, 2)

        assertTrue(result is CrearVentaResult.Error)
        assertEquals("Error al crear la venta", (result as CrearVentaResult.Error).mensaje)
    }
}
