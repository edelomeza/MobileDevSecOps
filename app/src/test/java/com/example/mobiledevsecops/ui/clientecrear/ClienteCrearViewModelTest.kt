package com.example.mobiledevsecops.ui.clientecrear

import com.example.mobiledevsecops.domain.usecase.CrearClienteUseCase
import com.example.mobiledevsecops.shared.fake.FakeClienteRepository
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class ClienteCrearViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeClienteRepository
    private lateinit var viewModel: ClienteCrearViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeClienteRepository()
        val useCase = CrearClienteUseCase(fakeRepo)
        viewModel = ClienteCrearViewModel(useCase)
    }

    @Test
    fun `estado inicial tiene valores por defecto`() {
        val state = viewModel.uiState.value
        assertEquals("", state.nombreCliente)
        assertEquals("", state.direccionCliente)
        assertEquals("", state.correo)
        assertEquals("", state.telefono)
        assertEquals(false, state.isLoading)
        assertNull(state.nombreClienteError)
        assertNull(state.correoError)
        assertNull(state.telefonoError)
        assertNull(state.error)
    }

    @Test
    fun `onNombreClienteChanged actualiza el nombre`() {
        viewModel.onNombreClienteChanged("Juan Pérez")
        assertEquals("Juan Pérez", viewModel.uiState.value.nombreCliente)
    }

    @Test
    fun `onDireccionClienteChanged actualiza la direccion`() {
        viewModel.onDireccionClienteChanged("Calle 123")
        assertEquals("Calle 123", viewModel.uiState.value.direccionCliente)
    }

    @Test
    fun `onCorreoChanged actualiza el correo`() {
        viewModel.onCorreoChanged("juan@example.com")
        assertEquals("juan@example.com", viewModel.uiState.value.correo)
    }

    @Test
    fun `onTelefonoChanged actualiza el telefono`() {
        viewModel.onTelefonoChanged("5512345678")
        assertEquals("5512345678", viewModel.uiState.value.telefono)
    }

    @Test
    fun `onGuardarClicked con datos validos emite ClienteCreado`() = runTest {
        viewModel.onNombreClienteChanged("Juan Pérez")
        viewModel.onDireccionClienteChanged("Calle 123")
        viewModel.onCorreoChanged("juan@example.com")
        viewModel.onTelefonoChanged("5512345678")

        val events = mutableListOf<ClienteCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is ClienteCrearEvent.ClienteCreado })
        job.cancel()
    }

    @Test
    fun `onGuardarClicked con nombre vacio muestra error`() {
        viewModel.onNombreClienteChanged("")
        viewModel.onCorreoChanged("juan@example.com")
        viewModel.onTelefonoChanged("5512345678")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.nombreClienteError)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onGuardarClicked con correo invalido muestra error`() {
        viewModel.onNombreClienteChanged("Juan Pérez")
        viewModel.onCorreoChanged("correo-invalido")
        viewModel.onTelefonoChanged("5512345678")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.correoError)
    }

    @Test
    fun `onGuardarClicked con telefono invalido muestra error`() {
        viewModel.onNombreClienteChanged("Juan Pérez")
        viewModel.onCorreoChanged("juan@example.com")
        viewModel.onTelefonoChanged("12345")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.telefonoError)
    }

    @Test
    fun `onGuardarClicked con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true
        viewModel.onNombreClienteChanged("Juan Pérez")
        viewModel.onCorreoChanged("juan@example.com")
        viewModel.onTelefonoChanged("5512345678")

        val events = mutableListOf<ClienteCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteCrearEvent.Error })
        job.cancel()
    }

    @Test
    fun `onGuardarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        viewModel.onNombreClienteChanged("Juan Pérez")
        viewModel.onCorreoChanged("juan@example.com")
        viewModel.onTelefonoChanged("5512345678")

        val events = mutableListOf<ClienteCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteCrearEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<ClienteCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is ClienteCrearEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onDismissError limpia el error`() {
        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.error)
    }
}
