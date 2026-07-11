package com.example.mobiledevsecops.ui.empleadocrear

import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import com.example.mobiledevsecops.domain.usecase.CrearEmpleadoUseCase
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
class EmpleadoCrearViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeEmpleadoRepository
    private lateinit var useCase: CrearEmpleadoUseCase
    private lateinit var viewModel: EmpleadoCrearViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeEmpleadoRepository()
        useCase = CrearEmpleadoUseCase(fakeRepo)
        viewModel = EmpleadoCrearViewModel(useCase, fakeRepo)
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `estado inicial con valores por defecto`() {
        val state = viewModel.uiState.value
        assertEquals("", state.nombre)
        assertEquals("", state.aPaterno)
        assertEquals("", state.aMaterno)
        assertEquals("", state.curp)
        assertNull(state.idTipoEmpleado)
        assertEquals(false, state.isLoading)
        assertTrue(state.tiposEmpleado.isNotEmpty())
    }

    @Test
    fun `cambio de nombre actualiza estado`() {
        viewModel.onNombreChanged("Juan")
        assertEquals("Juan", viewModel.uiState.value.nombre)
    }

    @Test
    fun `guardar empleado exitoso emite EmpleadoCreado`() = runTest {
        viewModel.onNombreChanged("Juan")
        viewModel.onAPaternoChanged("Perez")
        viewModel.onCurpChanged("HEXA010101HDFLNN01")
        viewModel.onTipoEmpleadoChanged(1)

        val events = mutableListOf<EmpleadoCrearEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onGuardarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is EmpleadoCrearEvent.EmpleadoCreado })
        job.cancel()
    }

    @Test
    fun `guardar con nombre vacio muestra error`() {
        viewModel.onGuardarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.nombreError)
    }

    @Test
    fun `guardar con curp invalido muestra error`() {
        viewModel.onNombreChanged("Juan")
        viewModel.onCurpChanged("INVALIDA")
        viewModel.onGuardarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.curpError)
    }

    @Test
    fun `guardar con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true
        viewModel.onNombreChanged("Juan")

        val events = mutableListOf<EmpleadoCrearEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onGuardarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is EmpleadoCrearEvent.Error })
        job.cancel()
    }

    @Test
    fun `guardar con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        viewModel.onNombreChanged("Juan")

        val events = mutableListOf<EmpleadoCrearEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onGuardarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is EmpleadoCrearEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `cancelar emite NavigateBack`() = runTest {
        val events = mutableListOf<EmpleadoCrearEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.onCancelarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is EmpleadoCrearEvent.NavigateBack })
        job.cancel()
    }
}
