package com.example.mobiledevsecops.ui.empleado

import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import com.example.mobiledevsecops.shared.fixture.EmpleadoFixtures
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.launch
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
class EmpleadoViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeEmpleadoRepository
    private lateinit var viewModel: EmpleadoViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeEmpleadoRepository()
        fakeRepo.givenEmpleados(EmpleadoFixtures.empleadosMultiPage)
        viewModel = EmpleadoViewModel(fakeRepo)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `init carga pagina 1 exitosamente`() {
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(1, state.currentPage)
        assertEquals(15, state.totalCount)
        assertEquals(2, state.totalPages)
        assertEquals(8, state.empleados.size)
    }

    @Test
    fun `init carga tipos de empleado`() {
        val state = viewModel.uiState.value
        assertTrue(state.tiposEmpleado.isNotEmpty())
    }

    @Test
    fun `goToNextPage carga pagina siguiente`() {
        viewModel.goToNextPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertEquals(5, state.empleados.size)
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

        val events = mutableListOf<EmpleadoEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.loadPage(1)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is EmpleadoEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `loadPage con page menor a 1 no hace nada`() {
        viewModel.loadPage(0)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `buscar por texto filtra resultados`() {
        viewModel.onSearchTextChanged("Empleado 1")
        viewModel.onSearch()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.empleados.isNotEmpty())
        assertTrue(state.isSearching)
    }

    @Test
    fun `onBackClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<EmpleadoEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.onBackClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is EmpleadoEvent.NavigateBack })
        job.cancel()
    }
}
