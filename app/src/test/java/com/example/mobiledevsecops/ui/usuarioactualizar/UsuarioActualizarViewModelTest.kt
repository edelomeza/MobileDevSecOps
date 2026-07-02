package com.example.mobiledevsecops.ui.usuarioactualizar

import com.example.mobiledevsecops.domain.usecase.ActualizarUsuarioUseCase
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
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
class UsuarioActualizarViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeUsuarioRepository
    private lateinit var viewModel: UsuarioActualizarViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeUsuarioRepository()
        fakeRepo.givenUsuarios(listOf(UsuarioFixtures.usuario))
        val useCase = ActualizarUsuarioUseCase(fakeRepo)
        viewModel = UsuarioActualizarViewModel(
            actualizarUsuarioUseCase = useCase,
            id = 1,
            nombre = "Juan Pérez",
            correo = "juan@example.com",
            rowVersion = "AAAAAAAAB9E="
        )
    }

    @Test
    fun `estado inicial refleja los parametros del constructor`() {
        val state = viewModel.uiState.value
        assertEquals(1, state.id)
        assertEquals("Juan Pérez", state.nombre)
        assertEquals("juan@example.com", state.correo)
        assertEquals("AAAAAAAAB9E=", state.rowVersion)
        assertEquals("", state.pwd)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `onNombreChanged actualiza el nombre`() {
        viewModel.onNombreChanged("Nuevo Nombre")
        assertEquals("Nuevo Nombre", viewModel.uiState.value.nombre)
    }

    @Test
    fun `onPwdChanged actualiza la contraseña`() {
        viewModel.onPwdChanged("newpassword123")
        assertEquals("newpassword123", viewModel.uiState.value.pwd)
    }

    @Test
    fun `onCorreoChanged actualiza el correo`() {
        viewModel.onCorreoChanged("nuevo@example.com")
        assertEquals("nuevo@example.com", viewModel.uiState.value.correo)
    }

    @Test
    fun `onActualizarClicked con datos validos emite UsuarioActualizado`() = runTest {
        viewModel.onNombreChanged("Nuevo Nombre")
        viewModel.onPwdChanged("Newpass123!")
        viewModel.onCorreoChanged("nuevo@example.com")

        val events = mutableListOf<UsuarioActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is UsuarioActualizarEvent.UsuarioActualizado })
        job.cancel()
    }

    @Test
    fun `onActualizarClicked con nombre vacio muestra error`() {
        viewModel.onNombreChanged("")
        viewModel.onPwdChanged("newpassword123")
        viewModel.onCorreoChanged("nuevo@example.com")
        viewModel.onActualizarClicked()

        assertNotNull(viewModel.uiState.value.nombreError)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onActualizarClicked con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true
        viewModel.onNombreChanged("Nuevo")
        viewModel.onPwdChanged("Newpass123!")
        viewModel.onCorreoChanged("nuevo@example.com")

        val events = mutableListOf<UsuarioActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioActualizarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onActualizarClicked con conflicto emite Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        viewModel.onNombreChanged("Nuevo")
        viewModel.onPwdChanged("Newpass123!")
        viewModel.onCorreoChanged("nuevo@example.com")

        val events = mutableListOf<UsuarioActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioActualizarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onActualizarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        viewModel.onNombreChanged("Nuevo")
        viewModel.onPwdChanged("Newpass123!")
        viewModel.onCorreoChanged("nuevo@example.com")

        val events = mutableListOf<UsuarioActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onActualizarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioActualizarEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<UsuarioActualizarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioActualizarEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onDismissError limpia error`() {
        viewModel.onNombreChanged("")
        viewModel.onActualizarClicked()

        assertEquals("El nombre es obligatorio", viewModel.uiState.value.nombreError)
    }
}
