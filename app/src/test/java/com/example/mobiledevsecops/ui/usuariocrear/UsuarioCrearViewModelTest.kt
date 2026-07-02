package com.example.mobiledevsecops.ui.usuariocrear

import com.example.mobiledevsecops.domain.usecase.CrearUsuarioUseCase
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
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
class UsuarioCrearViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeUsuarioRepository
    private lateinit var viewModel: UsuarioCrearViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeUsuarioRepository()
        val useCase = CrearUsuarioUseCase(fakeRepo)
        viewModel = UsuarioCrearViewModel(useCase)
    }

    @Test
    fun `estado inicial tiene valores por defecto`() {
        val state = viewModel.uiState.value
        assertEquals("", state.nombre)
        assertEquals("", state.pwd)
        assertEquals("", state.correo)
        assertEquals(false, state.isLoading)
        assertNull(state.nombreError)
        assertNull(state.pwdError)
        assertNull(state.correoError)
        assertNull(state.error)
    }

    @Test
    fun `onNombreChanged actualiza el nombre`() {
        viewModel.onNombreChanged("Juan Pérez")
        assertEquals("Juan Pérez", viewModel.uiState.value.nombre)
    }

    @Test
    fun `onPwdChanged actualiza la contraseña`() {
        viewModel.onPwdChanged("password123")
        assertEquals("password123", viewModel.uiState.value.pwd)
    }

    @Test
    fun `onCorreoChanged actualiza el correo`() {
        viewModel.onCorreoChanged("juan@example.com")
        assertEquals("juan@example.com", viewModel.uiState.value.correo)
    }

    @Test
    fun `onGuardarClicked con datos validos emite UsuarioCreado`() = runTest {
        viewModel.onNombreChanged("Juan Pérez")
        viewModel.onPwdChanged("Abcd1234!@")
        viewModel.onCorreoChanged("juan@example.com")

        val events = mutableListOf<UsuarioCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is UsuarioCrearEvent.UsuarioCreado })
        job.cancel()
    }

    @Test
    fun `onGuardarClicked con nombre vacio muestra error`() {
        viewModel.onNombreChanged("")
        viewModel.onPwdChanged("password123")
        viewModel.onCorreoChanged("juan@example.com")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.nombreError)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onGuardarClicked con password corto muestra error`() {
        viewModel.onNombreChanged("Juan Pérez")
        viewModel.onPwdChanged("1234567")
        viewModel.onCorreoChanged("juan@example.com")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.pwdError)
    }

    @Test
    fun `onGuardarClicked con correo invalido muestra error`() {
        viewModel.onNombreChanged("Juan Pérez")
        viewModel.onPwdChanged("password123")
        viewModel.onCorreoChanged("correo-invalido")
        viewModel.onGuardarClicked()

        assertNotNull(viewModel.uiState.value.correoError)
    }

    @Test
    fun `onGuardarClicked con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true
        viewModel.onNombreChanged("Juan Pérez")
        viewModel.onPwdChanged("Abcd1234!@")
        viewModel.onCorreoChanged("juan@example.com")

        val events = mutableListOf<UsuarioCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioCrearEvent.Error })
        job.cancel()
    }

    @Test
    fun `onGuardarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        viewModel.onNombreChanged("Juan Pérez")
        viewModel.onPwdChanged("Abcd1234!@")
        viewModel.onCorreoChanged("juan@example.com")

        val events = mutableListOf<UsuarioCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioCrearEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<UsuarioCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioCrearEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onDismissError limpia el error`() {
        viewModel.onNombreChanged("")
        viewModel.onGuardarClicked()
        viewModel.onDismissError()

        assertNull(viewModel.uiState.value.error)
    }
}
