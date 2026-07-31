package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ActualizarProductoUseCaseTest {

    private lateinit var fakeRepo: FakeProductoRepository
    private lateinit var useCase: ActualizarProductoUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeProductoRepository()
        useCase = ActualizarProductoUseCase(fakeRepo)
    }

    @Test
    fun `actualizar producto con datos validos retorna Success`() = runTest {
        val result = useCase(
            id = 1,
            strNombreProducto = "Laptop HP",
            strURLImagen = "https://example.com/laptop.jpg",
            strDescripcion = "Laptop 15.6 pulgadas",
            intNumeroExistencia = 10,
            decPrecio = 12500.00,
            rowVersion = "AAAAAAAAB9E="
        )
        assertTrue(result is ActualizarProductoResult.Success)
    }

    @Test
    fun `actualizar producto con id invalido retorna ValidationError`() = runTest {
        val result = useCase(
            id = 0,
            strNombreProducto = "Laptop",
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = 100.00,
            rowVersion = "AAAAAAAAB9E="
        )
        assertTrue(result is ActualizarProductoResult.ValidationError)
        val error = result as ActualizarProductoResult.ValidationError
        assertNotNull(error.errores["id"])
    }

    @Test
    fun `actualizar producto con rowVersion vacio retorna ValidationError`() = runTest {
        val result = useCase(
            id = 1,
            strNombreProducto = "Laptop",
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = 100.00,
            rowVersion = ""
        )
        assertTrue(result is ActualizarProductoResult.ValidationError)
        val error = result as ActualizarProductoResult.ValidationError
        assertNotNull(error.errores["rowVersion"])
    }

    @Test
    fun `actualizar producto con conflicto retorna Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        val result = useCase(
            id = 1,
            strNombreProducto = "Laptop",
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = 100.00,
            rowVersion = "AAAAAAAAB9E="
        )
        assertTrue(result is ActualizarProductoResult.Error)
    }

    @Test
    fun `actualizar producto con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(
            id = 1,
            strNombreProducto = "Laptop",
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = 100.00,
            rowVersion = "AAAAAAAAB9E="
        )
        assertTrue(result is ActualizarProductoResult.SessionExpired)
    }
}
