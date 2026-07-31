package com.example.mobiledevsecops.ui.productoactualizar

import com.example.mobiledevsecops.domain.usecase.ActualizarProductoUseCase
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
class ProductoActualizarViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeProductoRepository
    private lateinit var viewModel: ProductoActualizarViewModel

    private val params = ProductoActualizarParams(
        id = 1,
        strNombreProducto = "Laptop HP",
        strURLImagen = "https://example.com/laptop.jpg",
        strDescripcion = "Laptop 15.6 pulgadas",
        intNumeroExistencia = 10,
        decPrecio = 12500.00,
        rowVersion = "AAAAAAAAB9E="
    )

    @Before
    fun setUp() {
        fakeRepo = FakeProductoRepository()
        val useCase = ActualizarProductoUseCase(fakeRepo)
        viewModel = ProductoActualizarViewModel(useCase, params)
    }

    @Test
    fun `estado inicial tiene valores del producto`() {
        val state = viewModel.uiState.value
        assertEquals("Laptop HP", state.nombreProducto)
        assertEquals("https://example.com/laptop.jpg", state.urlImagen)
        assertEquals("Laptop 15.6 pulgadas", state.descripcion)
        assertEquals("10", state.existencia)
        assertEquals("12500.00", state.precio)
        assertEquals(false, state.isLoading)
        assertNull(state.nombreProductoError)
        assertNull(state.error)
    }

    @Test
    fun `onNombreProductoChanged actualiza el nombre`() {
        viewModel.onNombreProductoChanged("Producto Actualizado")
        assertEquals("Producto Actualizado", viewModel.uiState.value.nombreProducto)
    }

    @Test
    fun `onUrlImagenChanged actualiza la URL`() {
        viewModel.onUrlImagenChanged("https://example.com/new.jpg")
        assertEquals("https://example.com/new.jpg", viewModel.uiState.value.urlImagen)
    }

    @Test
    fun `onDescripcionChanged actualiza la descripcion`() {
        viewModel.onDescripcionChanged("Nueva descripción")
        assertEquals("Nueva descripción", viewModel.uiState.value.descripcion)
    }

    @Test
    fun `onActualizarClicked con datos validos emite ProductoActualizado`() = runTest {
        val events = mutableListOf<ProductoActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is ProductoActualizarEvent.ProductoActualizado })
        job.cancel()
    }

    @Test
    fun `onActualizarClicked con nombre vacio muestra error`() {
        viewModel.onNombreProductoChanged("")
        viewModel.onActualizarClicked()

        assertNotNull(viewModel.uiState.value.nombreProductoError)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onActualizarClicked con rowVersion vacio muestra error de version`() {
        val viewModelSinVersion = ProductoActualizarViewModel(
            ActualizarProductoUseCase(fakeRepo),
            params.copy(rowVersion = "")
        )
        viewModelSinVersion.onActualizarClicked()

        assertNotNull(viewModelSinVersion.uiState.value.rowVersionError)
        assertEquals(false, viewModelSinVersion.uiState.value.isLoading)
    }

    @Test
    fun `onActualizarClicked con existencia no numerica muestra error de formato`() {
        viewModel.onExistenciaChanged("abc")
        viewModel.onActualizarClicked()

        assertEquals("Ingrese un número entero válido", viewModel.uiState.value.existenciaError)
        assertNull(viewModel.uiState.value.precioError)
    }

    @Test
    fun `onActualizarClicked con precio vacio muestra error obligatorio`() {
        viewModel.onPrecioChanged("")
        viewModel.onActualizarClicked()

        assertEquals("El precio es obligatorio", viewModel.uiState.value.precioError)
    }

    @Test
    fun `onActualizarClicked con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true

        val events = mutableListOf<ProductoActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoActualizarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onActualizarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true

        val events = mutableListOf<ProductoActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoActualizarEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onActualizarClicked con conflicto emite Error`() = runTest {
        fakeRepo.shouldThrowConflict = true

        val events = mutableListOf<ProductoActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoActualizarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<ProductoActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoActualizarEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onDismissError limpia el error`() {
        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.error)
    }
}
