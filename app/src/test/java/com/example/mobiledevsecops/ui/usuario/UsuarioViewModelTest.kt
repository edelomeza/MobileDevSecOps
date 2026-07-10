package com.example.mobiledevsecops.ui.usuario

import com.example.mobiledevsecops.domain.usecase.BuscarUsuariosUseCase
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsuarioViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeUsuarioRepository
    private lateinit var buscarUsuariosUseCase: BuscarUsuariosUseCase
    private lateinit var viewModel: UsuarioViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeUsuarioRepository()
        fakeRepo.givenUsuarios(UsuarioFixtures.usuariosMultiPage)
        buscarUsuariosUseCase = BuscarUsuariosUseCase(fakeRepo)
        viewModel = UsuarioViewModel(fakeRepo, buscarUsuariosUseCase)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `init carga pagina 1 exitosamente`() {
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(1, state.currentPage)
        assertEquals(15, state.totalCount)
        assertEquals(2, state.totalPages)
        assertEquals(8, state.usuarios.size)
    }

    @Test
    fun `goToNextPage carga pagina siguiente`() {
        viewModel.goToNextPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertEquals(5, state.usuarios.size)
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

        val events = mutableListOf<UsuarioEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.loadPage(1)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is UsuarioEvent.SessionExpired })
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
        val events = mutableListOf<UsuarioEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.onBackClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is UsuarioEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onSearchTextChanged actualiza searchText en el estado`() {
        viewModel.onSearchTextChanged("Juan")
        assertEquals("Juan", viewModel.uiState.value.searchText)
    }

    @Test
    fun `onBuscarClicked con texto valido retorna resultados`() {
        viewModel.onSearchTextChanged("Usuario 5")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSearchActive)
        assertEquals(1, state.totalCount)
        assertEquals("Usuario 5", state.usuarios.first().strNombre)
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
        viewModel.onSearchTextChanged("Usuario 1")
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

        val events = mutableListOf<UsuarioEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.onSearchTextChanged("Usuario 1")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is UsuarioEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onBuscarClicked con error emite ShowSnackbar`() = runTest {
        fakeRepo.shouldThrowException = true
        viewModel.onSearchTextChanged("Usuario 1")

        val events = mutableListOf<UsuarioEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(events.any { it is UsuarioEvent.ShowSnackbar })
        job.cancel()
    }

    @Test
    fun `paginacion en busqueda funciona correctamente`() {
        fakeRepo.givenUsuarios(
            (1..20).map { i ->
                com.example.mobiledevsecops.domain.model.Usuario(
                    id = i,
                    strNombre = "Juan $i",
                    strCorreoElectronico = "juan$i@example.com",
                    rowVersion = "AAAAAAAAB9E="
                )
            }
        )
        buscarUsuariosUseCase = BuscarUsuariosUseCase(fakeRepo)
        viewModel = UsuarioViewModel(fakeRepo, buscarUsuariosUseCase)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchTextChanged("Juan")
        viewModel.onBuscarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentPage)
        assertEquals(3, state.totalPages)
        assertEquals(20, state.totalCount)
        assertEquals(8, state.usuarios.size)

        viewModel.goToNextPage()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state2 = viewModel.uiState.value
        assertEquals(2, state2.currentPage)
        assertEquals(8, state2.usuarios.size)
    }
}
