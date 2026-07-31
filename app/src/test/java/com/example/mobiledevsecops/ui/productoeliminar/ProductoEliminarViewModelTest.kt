package com.example.mobiledevsecops.ui.productoeliminar

import com.example.mobiledevsecops.domain.usecase.EliminarProductoUseCase
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
class ProductoEliminarViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeProductoRepository
    private lateinit var viewModel: ProductoEliminarViewModel

    private val params = ProductoEliminarParams(
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
        val useCase = EliminarProductoUseCase(fakeRepo)
        viewModel = ProductoEliminarViewModel(useCase, params)
    }

    @Test
    fun `estado inicial tiene datos del producto`() {
        val state = viewModel.uiState.value
        assertEquals("Laptop HP", state.nombreProducto)
        assertEquals("https://example.com/laptop.jpg", state.urlImagen)
        assertEquals("Laptop 15.6 pulgadas", state.descripcion)
        assertEquals("10", state.existencia)
        assertEquals("12500.00", state.precio)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onEliminarClicked con datos validos emite ProductoEliminado`() = runTest {
        val events = mutableListOf<ProductoEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is ProductoEliminarEvent.ProductoEliminado })
        job.cancel()
    }

    @Test
    fun `onEliminarClicked con rowVersion invalido muestra error y no emite eventos`() {
        val viewModelSinVersion = ProductoEliminarViewModel(
            EliminarProductoUseCase(fakeRepo),
            params.copy(rowVersion = "")
        )

        viewModelSinVersion.onEliminarClicked()

        assertNotNull(viewModelSinVersion.uiState.value.rowVersionError)
        assertEquals(false, viewModelSinVersion.uiState.value.isLoading)
    }

    @Test
    fun `onEliminarClicked con id invalido muestra error`() {
        val viewModelSinId = ProductoEliminarViewModel(
            EliminarProductoUseCase(fakeRepo),
            params.copy(id = 0)
        )

        viewModelSinId.onEliminarClicked()

        assertNotNull(viewModelSinId.uiState.value.idError)
        assertEquals(false, viewModelSinId.uiState.value.isLoading)
    }

    @Test
    fun `onEliminarClicked con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true

        val events = mutableListOf<ProductoEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoEliminarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onEliminarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true

        val events = mutableListOf<ProductoEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoEliminarEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onEliminarClicked con conflicto emite Error`() = runTest {
        fakeRepo.shouldThrowConflict = true

        val events = mutableListOf<ProductoEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoEliminarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<ProductoEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ProductoEliminarEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onDismissError limpia el error`() {
        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.error)
    }
}
