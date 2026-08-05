package com.example.mobiledevsecops.ui.venta

import com.example.mobiledevsecops.domain.model.Venta
import com.example.mobiledevsecops.domain.usecase.BuscarVentasUseCase
import com.example.mobiledevsecops.shared.fake.FakeVentaRepository
import com.example.mobiledevsecops.shared.fixture.VentaFixtures
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
class VentaViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeVentaRepository
    private lateinit var buscarVentasUseCase: BuscarVentasUseCase
    private lateinit var viewModel: VentaViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeVentaRepository()
        fakeRepo.givenVentas(VentaFixtures.ventasMultiPage)
        buscarVentasUseCase = BuscarVentasUseCase(fakeRepo)
        viewModel = VentaViewModel(fakeRepo, buscarVentasUseCase)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `init carga pagina 1 exitosamente`() {
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(1, state.currentPage)
        assertEquals(15, state.totalCount)
        assertEquals(2, state.totalPages)
        assertEquals(8, state.ventas.size)
    }

    @Test
    fun `goToNextPage carga pagina siguiente`() {
        viewModel.goToNextPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertEquals(5, state.ventas.size)
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

        val events = mutableListOf<VentaEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.loadPage(1)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is VentaEvent.SessionExpired })
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
        val events = mutableListOf<VentaEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.onBackClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is VentaEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onSearchTextChanged actualiza searchText en el estado`() {
        viewModel.onSearchTextChanged("V001001001")
        assertEquals("V001001001", viewModel.uiState.value.searchText)
    }

    @Test
    fun `onFechaInicioChanged convierte millis a ISO`() {
        viewModel.onFechaInicioChanged(1782864000000L)
        assertEquals("2026-07-01", viewModel.uiState.value.fechaInicio)
    }

    @Test
    fun `onFechaInicioChanged con null limpia la fecha`() {
        viewModel.onFechaInicioChanged(1782864000000L)
        viewModel.onFechaInicioChanged(null)
        assertEquals(null, viewModel.uiState.value.fechaInicio)
    }

    @Test
    fun `onBuscarClicked con texto valido retorna resultados`() {
        viewModel.onSearchTextChanged("Cliente 5")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSearchActive)
        assertEquals(1, state.totalCount)
        assertEquals("Cliente 5", state.ventas.first().strNombreCliente)
    }

    @Test
    fun `onBuscarClicked con fechas retorna resultados`() {
        viewModel.onFechaInicioChanged(1782950400000L)
        viewModel.onFechaFinChanged(1783123200000L)
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSearchActive)
        assertTrue(state.totalCount > 0)
    }

    @Test
    fun `onBuscarClicked sin criterios no hace nada`() {
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSearchActive)
    }

    @Test
    fun `onClearSearch restaura lista completa`() {
        viewModel.onSearchTextChanged("Cliente 1")
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

        val events = mutableListOf<VentaEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.onSearchTextChanged("Cliente 1")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is VentaEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onBuscarClicked con error emite ShowSnackbar`() = runTest {
        fakeRepo.shouldThrowException = true
        viewModel.onSearchTextChanged("Cliente 1")

        val events = mutableListOf<VentaEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(events.any { it is VentaEvent.ShowSnackbar })
        job.cancel()
    }

    @Test
    fun `paginacion en busqueda funciona correctamente`() {
        fakeRepo.givenVentas(
            (1..20).map { i ->
                Venta(
                    id = i,
                    strClaveVenta = "V${i.toString().padStart(9, '0')}",
                    dteFechaHoraCompra = "2026-07-${i.toString().padStart(2, '0')}T10:15:00",
                    strNombreCliente = "Cliente $i",
                    strEstado = "Abierta",
                    rowVersion = "AAAAAAAAB9E="
                )
            }
        )
        buscarVentasUseCase = BuscarVentasUseCase(fakeRepo)
        viewModel = VentaViewModel(fakeRepo, buscarVentasUseCase)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchTextChanged("Cliente")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentPage)
        assertEquals(3, state.totalPages)
        assertEquals(20, state.totalCount)
        assertEquals(8, state.ventas.size)

        viewModel.goToNextPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state2 = viewModel.uiState.value
        assertEquals(2, state2.currentPage)
        assertEquals(8, state2.ventas.size)
    }
}
