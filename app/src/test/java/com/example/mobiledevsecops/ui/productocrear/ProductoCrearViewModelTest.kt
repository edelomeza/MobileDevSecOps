package com.example.mobiledevsecops.ui.productocrear

import com.example.mobiledevsecops.domain.usecase.CrearProductoUseCase
import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoCrearViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeProductoRepository
    private lateinit var viewModel: ProductoCrearViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeProductoRepository()
        val useCase = CrearProductoUseCase(fakeRepo)
        viewModel = ProductoCrearViewModel(useCase)
    }

    @Test
    fun `estado inicial tiene valores por defecto`() {
        val state = viewModel.uiState.value
        assertEquals("", state.nombreProducto)
        assertEquals("", state.urlImagen)
        assertEquals("", state.descripcion)
        assertEquals("", state.existencia)
        assertEquals("", state.precio)
        assertEquals(false, state.isLoading)
        assertNull(state.nombreProductoError)
        assertNull(state.urlImagenError)
        assertNull(state.descripcionError)
        assertNull(state.existenciaError)
        assertNull(state.precioError)
        assertNull(state.error)
    }

    @Test
    fun `onNombreProductoChanged actualiza el nombre`() {
        viewModel.onNombreProductoChanged("Laptop")
        assertEquals("Laptop", viewModel.uiState.value.nombreProducto)
    }

    @Test
    fun `onUrlImagenChanged actualiza la URL`() {
        viewModel.onUrlImagenChanged("https://example.com/img.jpg")
        assertEquals("https://example.com/img.jpg", viewModel.uiState.value.urlImagen)
    }

    @Test
    fun `onDescripcionChanged actualiza la descripcion`() {
        viewModel.onDescripcionChanged("Descripción del producto")
        assertEquals("Descripción del producto", viewModel.uiState.value.descripcion)
    }

    @Test
    fun `onExistenciaChanged actualiza la existencia`() {
        viewModel.onExistenciaChanged("10")
        assertEquals("10", viewModel.uiState.value.existencia)
    }

    @Test
    fun `onPrecioChanged actualiza el precio`() {
        viewModel.onPrecioChanged("12500.00")
        assertEquals("12500.00", viewModel.uiState.value.precio)
    }

    @Test
    fun `onGuardarClicked con datos validos emite ProductoCreado`() = runTest {
        viewModel.onNombreProductoChanged("Laptop HP")
        viewModel.onUrlImagenChanged("https://example.com/laptop.jpg")
        viewModel.onDescripcionChanged("Laptop 15.6 pulgadas")
        viewModel.onExistenciaChanged("10")
        viewModel.onPrecioChanged("12500.00")

        val events = mutableListOf<ProductoCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is ProductoCrearEvent.ProductoCreado })
        job.cancel()
    }

    @Test
    fun `onGuardarClicked con nombre vacio muestra error`() {
        viewModel.onNombreProductoChanged("")
        viewModel.onExistenciaChanged("10")
        viewModel.onPrecioChanged("100.00")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.nombreProductoError)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onGuardarClicked con URL invalida muestra error`() {
        viewModel.onNombreProductoChanged("Producto")
        viewModel.onUrlImagenChanged("ftp://invalido")
        viewModel.onExistenciaChanged("10")
        viewModel.onPrecioChanged("100.00")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.urlImagenError)
    }

    @Test
    fun `onGuardarClicked con existencia negativa muestra error`() {
        viewModel.onNombreProductoChanged("Producto")
        viewModel.onExistenciaChanged("-1")
        viewModel.onPrecioChanged("100.00")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.existenciaError)
    }

    @Test
    fun `onGuardarClicked con precio negativo muestra error`() {
        viewModel.onNombreProductoChanged("Producto")
        viewModel.onExistenciaChanged("10")
        viewModel.onPrecioChanged("-1.00")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.precioError)
    }

    @Test
    fun `onGuardarClicked con existencia no numerica muestra error de formato`() {
        viewModel.onNombreProductoChanged("Producto")
        viewModel.onExistenciaChanged("abc")
        viewModel.onPrecioChanged("100.00")
        viewModel.onGuardarClicked()

        assertEquals("Ingrese un número entero válido", viewModel.uiState.value.existenciaError)
        assertNull(viewModel.uiState.value.precioError)
    }

    @Test
    fun `onGuardarClicked con precio vacio muestra error obligatorio`() {
        viewModel.onNombreProductoChanged("Producto")
        viewModel.onExistenciaChanged("10")
        viewModel.onGuardarClicked()

        assertEquals("El precio es obligatorio", viewModel.uiState.value.precioError)
    }

    @Test
    fun `onGuardarClicked con precio con coma decimal muestra error de formato`() {
        viewModel.onNombreProductoChanged("Producto")
        viewModel.onExistenciaChanged("10")
        viewModel.onPrecioChanged("12,50")
        viewModel.onGuardarClicked()

        assertEquals("Ingrese un precio válido", viewModel.uiState.value.precioError)
    }

    @Test
    fun `onGuardarClicked con existencia vacia muestra error obligatorio`() {
        viewModel.onNombreProductoChanged("Producto")
        viewModel.onPrecioChanged("100.00")
        viewModel.onGuardarClicked()

        assertEquals("La existencia es obligatoria", viewModel.uiState.value.existenciaError)
    }

    @Test
    fun `onGuardarClicked con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true
        viewModel.onNombreProductoChanged("Producto")
        viewModel.onExistenciaChanged("10")
        viewModel.onPrecioChanged("100.00")

        val events = mutableListOf<ProductoCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoCrearEvent.Error })
        job.cancel()
    }

    @Test
    fun `onGuardarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        viewModel.onNombreProductoChanged("Producto")
        viewModel.onExistenciaChanged("10")
        viewModel.onPrecioChanged("100.00")

        val events = mutableListOf<ProductoCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoCrearEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<ProductoCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoCrearEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onDismissError limpia el error`() {
        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.error)
    }
}
