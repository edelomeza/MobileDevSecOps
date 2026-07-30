package com.example.mobiledevsecops.ui.clienteeliminar

import com.example.mobiledevsecops.domain.usecase.EliminarClienteUseCase
import com.example.mobiledevsecops.shared.fake.FakeClienteRepository
import com.example.mobiledevsecops.shared.fixture.ClienteFixtures
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
class ClienteEliminarViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeClienteRepository
    private lateinit var viewModel: ClienteEliminarViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeClienteRepository()
        fakeRepo.givenClientes(listOf(ClienteFixtures.cliente))
        val useCase = EliminarClienteUseCase(fakeRepo)
        viewModel = ClienteEliminarViewModel(
            eliminarClienteUseCase = useCase,
            params = ClienteEliminarParams(
                id = 1,
                nombreCliente = "Juan Pérez",
                direccionCliente = "Calle 123",
                correo = "juan@example.com",
                telefono = "5512345678",
                rowVersion = "AAAAAAAAB9E="
            )
        )
    }

    @Test
    fun `estado inicial refleja los parametros del constructor`() {
        val state = viewModel.uiState.value
        assertEquals(1, state.id)
        assertEquals("Juan Pérez", state.nombreCliente)
        assertEquals("Calle 123", state.direccionCliente)
        assertEquals("juan@example.com", state.correo)
        assertEquals("5512345678", state.telefono)
        assertEquals("AAAAAAAAB9E=", state.rowVersion)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onEliminarClicked con datos validos emite ClienteEliminado`() = runTest {
        val events = mutableListOf<ClienteEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is ClienteEliminarEvent.ClienteEliminado })
        job.cancel()
    }

    @Test
    fun `onEliminarClicked con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true

        val events = mutableListOf<ClienteEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteEliminarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onEliminarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true

        val events = mutableListOf<ClienteEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteEliminarEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onEliminarClicked con conflicto emite Error`() = runTest {
        fakeRepo.shouldThrowConflict = true

        val events = mutableListOf<ClienteEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteEliminarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<ClienteEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteEliminarEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onDismissError limpia error`() {
        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onEliminarClicked con id invalido muestra error de validacion`() {
        val useCase = EliminarClienteUseCase(fakeRepo)
        val vm = ClienteEliminarViewModel(
            eliminarClienteUseCase = useCase,
            params = ClienteEliminarParams(
                id = 0,
                nombreCliente = "Test",
                direccionCliente = null,
                correo = "test@test.com",
                telefono = "5512345678",
                rowVersion = ""
            )
        )
        vm.onEliminarClicked()

        assertNotNull(vm.uiState.value.idError)
        assertNotNull(vm.uiState.value.rowVersionError)
    }
}
