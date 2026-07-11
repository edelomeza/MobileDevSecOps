package com.example.mobiledevsecops.ui.empleadoactualizar

import com.example.mobiledevsecops.domain.model.Empleado
import com.example.mobiledevsecops.domain.usecase.ActualizarEmpleadoUseCase
import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmpleadoActualizarViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeEmpleadoRepository
    private lateinit var useCase: ActualizarEmpleadoUseCase
    private lateinit var viewModel: EmpleadoActualizarViewModel

    private val testEmpleado = Empleado(
        id = 1,
        strNombre = "Juan",
        strAPaterno = "Perez",
        strAMaterno = "Lopez",
        strCURP = "HEXA010101HDFLNN01",
        idEmpCatTipoEmpleado = 1,
        rowVersion = "AAAAAAAAB9E="
    )

    @Before
    fun setUp() {
        fakeRepo = FakeEmpleadoRepository()
        fakeRepo.givenEmpleados(listOf(testEmpleado))
        useCase = ActualizarEmpleadoUseCase(fakeRepo)
        viewModel = EmpleadoActualizarViewModel(
            useCase, fakeRepo,
            EmpleadoActualizarParams(1, "AAAAAAAAB9E=")
        )
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `estado inicial con parametros correctos`() {
        val state = viewModel.uiState.value
        assertEquals(1, state.id)
        assertEquals("Juan", state.nombre)
        assertEquals("Perez", state.aPaterno)
        assertEquals("Lopez", state.aMaterno)
        assertEquals("HEXA010101HDFLNN01", state.curp)
        assertEquals(1, state.idTipoEmpleado)
        assertEquals("AAAAAAAAB9E=", state.rowVersion)
        assertTrue(state.tiposEmpleado.isNotEmpty())
    }

    @Test
    fun `actualizar empleado exitoso emite EmpleadoActualizado`() = runTest {
        val events = mutableListOf<EmpleadoActualizarEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onActualizarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is EmpleadoActualizarEvent.EmpleadoActualizado })
        job.cancel()
    }

    @Test
    fun `actualizar con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true

        val events = mutableListOf<EmpleadoActualizarEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onActualizarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is EmpleadoActualizarEvent.Error })
        job.cancel()
    }

    @Test
    fun `cancelar emite NavigateBack`() = runTest {
        val events = mutableListOf<EmpleadoActualizarEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        viewModel.onCancelarClicked()
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is EmpleadoActualizarEvent.NavigateBack })
        job.cancel()
    }
}
