package com.example.mobiledevsecops.ui.usuarioeliminar

import com.example.mobiledevsecops.domain.usecase.EliminarUsuarioUseCase
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
class UsuarioEliminarViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeUsuarioRepository
    private lateinit var viewModel: UsuarioEliminarViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeUsuarioRepository()
        fakeRepo.givenUsuarios(listOf(UsuarioFixtures.usuario))
        val useCase = EliminarUsuarioUseCase(fakeRepo)
        viewModel = UsuarioEliminarViewModel(
            eliminarUsuarioUseCase = useCase,
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
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onEliminarClicked con datos validos emite UsuarioEliminado`() = runTest {
        val events = mutableListOf<UsuarioEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is UsuarioEliminarEvent.UsuarioEliminado })
        job.cancel()
    }

    @Test
    fun `onEliminarClicked con error del servidor emite Error`() = runTest {
        fakeRepo.shouldThrowException = true

        val events = mutableListOf<UsuarioEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioEliminarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onEliminarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true

        val events = mutableListOf<UsuarioEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioEliminarEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onEliminarClicked con conflicto emite Error`() = runTest {
        fakeRepo.shouldThrowConflict = true

        val events = mutableListOf<UsuarioEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onEliminarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioEliminarEvent.Error })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<UsuarioEliminarEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is UsuarioEliminarEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `onDismissError limpia error`() {
        viewModel.onDismissError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onEliminarClicked con id invalido muestra error de validacion`() {
        val useCase = EliminarUsuarioUseCase(fakeRepo)
        val vm = UsuarioEliminarViewModel(
            eliminarUsuarioUseCase = useCase,
            id = 0,
            nombre = "Test",
            correo = "test@test.com",
            rowVersion = ""
        )
        vm.onEliminarClicked()

        assertNotNull(vm.uiState.value.idError)
        assertNotNull(vm.uiState.value.rowVersionError)
    }
}
