package com.example.mobiledevsecops.ui.producto

import com.example.mobiledevsecops.domain.usecase.BuscarProductosUseCase
import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import com.example.mobiledevsecops.shared.fixture.ProductoFixtures
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeProductoRepository
    private lateinit var buscarProductosUseCase: BuscarProductosUseCase
    private lateinit var viewModel: ProductoViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeProductoRepository()
        fakeRepo.givenProductos(ProductoFixtures.productosMultiPage)
        buscarProductosUseCase = BuscarProductosUseCase(fakeRepo)
        viewModel = ProductoViewModel(fakeRepo, buscarProductosUseCase)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `init carga pagina 1 exitosamente`() {
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(1, state.currentPage)
        assertEquals(15, state.totalCount)
        assertEquals(2, state.totalPages)
        assertEquals(8, state.productos.size)
    }

    @Test
    fun `loadPage pagina 2 muestra todos los items restantes`() {
        viewModel.loadPage(2)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertEquals(7, state.productos.size)
        assertEquals("Producto 9", state.productos.first().strNombreProducto)
        assertEquals("Producto 15", state.productos.last().strNombreProducto)
    }

    @Test
    fun `goToNextPage carga pagina siguiente`() {
        viewModel.goToNextPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
    }

    @Test
    fun `goToPreviousPage retrocede de pagina`() {
        viewModel.goToNextPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.goToPreviousPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `goToNextPage no avanza si estamos en la ultima pagina`() {
        viewModel.loadPage(2)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.goToNextPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `goToPreviousPage no retrocede si estamos en la primera pagina`() {
        viewModel.goToPreviousPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `loadPage con error establece mensaje de error`() {
        fakeRepo.shouldThrowException = true
        viewModel.loadPage(1)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `loadPage con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true

        val events = mutableListOf<ProductoEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.loadPage(1)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is ProductoEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `loadPage con page menor a 1 no hace nada`() {
        viewModel.loadPage(0)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `onBackClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<ProductoEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.onBackClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is ProductoEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onSearchTextChanged actualiza searchText en el estado`() {
        viewModel.onSearchTextChanged("Laptop")
        assertEquals("Laptop", viewModel.uiState.value.searchText)
    }

    @Test
    fun `onBuscarClicked con texto valido retorna resultados`() {
        viewModel.onSearchTextChanged("Producto 5")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSearchActive)
        assertEquals(1, state.totalCount)
        assertEquals("Producto 5", state.productos.first().strNombreProducto)
    }

    @Test
    fun `onBuscarClicked sin texto no hace nada`() {
        viewModel.onSearchTextChanged("")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSearchActive)
    }

    @Test
    fun `onClearSearch restaura lista completa`() {
        viewModel.onSearchTextChanged("Producto 1")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onClearSearch()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSearchActive)
        assertEquals("", state.searchText)
        assertEquals(15, state.totalCount)
    }

    @Test
    fun `onBuscarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true

        val events = mutableListOf<ProductoEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.onSearchTextChanged("Producto 1")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is ProductoEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onBuscarClicked con error emite ShowSnackbar`() = runTest {
        fakeRepo.shouldThrowException = true
        viewModel.onSearchTextChanged("Producto 1")

        val events = mutableListOf<ProductoEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is ProductoEvent.ShowSnackbar })
        job.cancel()
    }

    @Test
    fun `paginacion en busqueda funciona correctamente`() {
        fakeRepo.givenProductos(
            (1..20).map { i ->
                com.example.mobiledevsecops.domain.model.Producto(
                    id = i,
                    strNombreProducto = "Laptop $i",
                    strURLImagen = null,
                    strDescripcion = null,
                    intNumeroExistencia = i * 2,
                    decPrecio = i * 100.0,
                    rowVersion = "AAAAAAAAB9E="
                )
            }
        )
        buscarProductosUseCase = BuscarProductosUseCase(fakeRepo)
        viewModel = ProductoViewModel(fakeRepo, buscarProductosUseCase)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchTextChanged("Laptop")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentPage)
        assertEquals(3, state.totalPages)
        assertEquals(20, state.totalCount)
        assertEquals(8, state.productos.size)

        viewModel.goToNextPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state2 = viewModel.uiState.value
        assertEquals(2, state2.currentPage)
    }
}
