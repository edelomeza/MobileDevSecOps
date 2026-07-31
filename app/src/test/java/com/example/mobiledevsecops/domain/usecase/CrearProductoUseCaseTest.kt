package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrearProductoUseCaseTest {

    private lateinit var fakeRepo: FakeProductoRepository
    private lateinit var useCase: CrearProductoUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeProductoRepository()
        useCase = CrearProductoUseCase(fakeRepo)
    }

    @Test
    fun `crear producto con datos validos retorna Success`() = runTest {
        val result = useCase(
            strNombreProducto = "Laptop HP",
            strURLImagen = "https://example.com/laptop.jpg",
            strDescripcion = "Laptop 15.6 pulgadas",
            intNumeroExistencia = 10,
            decPrecio = 12500.00
        )
        assertTrue(result is CrearProductoResult.Success)
    }

    @Test
    fun `crear producto con nombre vacio retorna ValidationError`() = runTest {
        val result = useCase(
            strNombreProducto = "",
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = 100.00
        )
        assertTrue(result is CrearProductoResult.ValidationError)
        val error = result as CrearProductoResult.ValidationError
        assertNotNull(error.errores["strNombreProducto"])
    }

    @Test
    fun `crear producto con nombre muy largo retorna ValidationError`() = runTest {
        val result = useCase(
            strNombreProducto = "A".repeat(51),
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = 100.00
        )
        assertTrue(result is CrearProductoResult.ValidationError)
        val error = result as CrearProductoResult.ValidationError
        assertNotNull(error.errores["strNombreProducto"])
    }

    @Test
    fun `crear producto con URL invalida retorna ValidationError`() = runTest {
        val result = useCase(
            strNombreProducto = "Laptop",
            strURLImagen = "ftp://invalida.com",
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = 100.00
        )
        assertTrue(result is CrearProductoResult.ValidationError)
        val error = result as CrearProductoResult.ValidationError
        assertNotNull(error.errores["strURLImagen"])
    }

    @Test
    fun `crear producto con existencia negativa retorna ValidationError`() = runTest {
        val result = useCase(
            strNombreProducto = "Laptop",
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = -1,
            decPrecio = 100.00
        )
        assertTrue(result is CrearProductoResult.ValidationError)
        val error = result as CrearProductoResult.ValidationError
        assertNotNull(error.errores["intNumeroExistencia"])
    }

    @Test
    fun `crear producto con precio negativo retorna ValidationError`() = runTest {
        val result = useCase(
            strNombreProducto = "Laptop",
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = -1.0
        )
        assertTrue(result is CrearProductoResult.ValidationError)
        val error = result as CrearProductoResult.ValidationError
        assertNotNull(error.errores["decPrecio"])
    }

    @Test
    fun `crear producto con error del servidor retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase(
            strNombreProducto = "Laptop",
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = 100.00
        )
        assertTrue(result is CrearProductoResult.Error)
    }

    @Test
    fun `crear producto con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(
            strNombreProducto = "Laptop",
            strURLImagen = null,
            strDescripcion = null,
            intNumeroExistencia = 10,
            decPrecio = 100.00
        )
        assertTrue(result is CrearProductoResult.SessionExpired)
    }

    @Test
    fun `validar retorna errores para campos invalidos`() {
        val errores = useCase.validar("", "ftp://bad", "desc", -1, -5.0)
        assertNotNull(errores["strNombreProducto"])
        assertNotNull(errores["strURLImagen"])
        assertNotNull(errores["intNumeroExistencia"])
        assertNotNull(errores["decPrecio"])
    }

    @Test
    fun `validar permite campos opcionales nulos`() {
        val errores = useCase.validar("Producto", null, null, 0, 0.0)
        assertEquals(0, errores.size)
    }
}
