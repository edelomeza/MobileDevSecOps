package com.example.mobiledevsecops.ui.clienteactualizar

import com.example.mobiledevsecops.domain.usecase.ActualizarClienteUseCase
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
class ClienteActualizarViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeClienteRepository
    private lateinit var viewModel: ClienteActualizarViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeClienteRepository()
        fakeRepo.givenClientes(listOf(ClienteFixtures.cliente))
        val useCase = ActualizarClienteUseCase(fakeRepo)
        viewModel = ClienteActualizarViewModel(
            actualizarClienteUseCase = useCase,
            params = ClienteActualizarParams(
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
    }

    @Test
    fun `onNombreClienteChanged actualiza el nombre`() {
        viewModel.onNombreClienteChanged("Nuevo Nombre")
        assertEquals("Nuevo Nombre", viewModel.uiState.value.nombreCliente)
    }

    @Test
    fun `onDireccionClienteChanged actualiza la direccion`() {
        viewModel.onDireccionClienteChanged("Nueva Dirección")
        assertEquals("Nueva Dirección", viewModel.uiState.value.direccionCliente)
    }

    @Test
    fun `onCorreoChanged actualiza el correo`() {
        viewModel.onCorreoChanged("nuevo@example.com")
        assertEquals("nuevo@example.com", viewModel.uiState.value.correo)
    }

    @Test
    fun `onTelefonoChanged actualiza el telefono`() {
        viewModel.onTelefonoChanged("5512345679")
        assertEquals("5512345679", viewModel.uiState.value.telefono)
    }

    @Test
    fun `onActualizarClicked con datos validos emite ClienteActualizado`() = runTest {
        viewModel.onNombreClienteChanged("Nuevo Nombre")
        viewModel.onCorreoChanged("nuevo@example.com")
        viewModel.onTelefonoChanged("5512345679")

        val events = mutableListOf<ClienteActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is ClienteActualizarEvent.ClienteActualizado })
        job.cancel()
    }

    @Test
    fun `onActualizarClicked con nombre vacio muestra error`() {
        viewModel.onNombreClienteChanged("")
        viewModel.onCorreoChanged("nuevo@example.com")
        viewModel.onActualizarClicked()

        assertNotNull(viewModel.uiState.value.nombreClienteError)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onActualizarClicked con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true
        viewModel.onNombreClienteChanged("Nuevo")
        viewModel.onCorreoChanged("nuevo@example.com")
        viewModel.onTelefonoChanged("5512345678")

        val events = mutableListOf<ClienteActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteActualizarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onActualizarClicked con conflicto emite Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        viewModel.onNombreClienteChanged("Nuevo")
        viewModel.onCorreoChanged("nuevo@example.com")
        viewModel.onTelefonoChanged("5512345678")

        val events = mutableListOf<ClienteActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteActualizarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onActualizarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        viewModel.onNombreClienteChanged("Nuevo")
        viewModel.onCorreoChanged("nuevo@example.com")
        viewModel.onTelefonoChanged("5512345678")

        val events = mutableListOf<ClienteActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteActualizarEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<ClienteActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteActualizarEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onDismissError limpia error`() {
        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.error)
    }
}
