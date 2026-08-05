package com.example.mobiledevsecops.ui.ventacrear

import com.example.mobiledevsecops.domain.usecase.BuscarClientesUseCase
import com.example.mobiledevsecops.domain.usecase.BuscarUsuariosUseCase
import com.example.mobiledevsecops.domain.usecase.CrearVentaUseCase
import com.example.mobiledevsecops.shared.fake.FakeClienteRepository
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fake.FakeVentaRepository
import com.example.mobiledevsecops.shared.fixture.ClienteFixtures
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
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
class VentaCrearViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeVentaRepo: FakeVentaRepository
    private lateinit var fakeClienteRepo: FakeClienteRepository
    private lateinit var fakeUsuarioRepo: FakeUsuarioRepository
    private lateinit var viewModel: VentaCrearViewModel

    @Before
    fun setUp() {
        fakeVentaRepo = FakeVentaRepository()
        fakeClienteRepo = FakeClienteRepository()
        fakeClienteRepo.givenClientes(ClienteFixtures.clientesMultiPage)
        fakeUsuarioRepo = FakeUsuarioRepository()
        fakeUsuarioRepo.givenUsuarios(UsuarioFixtures.usuariosMultiPage)

        val crearVentaUseCase = CrearVentaUseCase(fakeVentaRepo)
        val buscarClientesUseCase = BuscarClientesUseCase(fakeClienteRepo)
        val buscarUsuariosUseCase = BuscarUsuariosUseCase(fakeUsuarioRepo)
        viewModel = VentaCrearViewModel(
            crearVentaUseCase,
            buscarClientesUseCase,
            buscarUsuariosUseCase
        )
    }

    @Test
    fun `estado inicial tiene valores por defecto`() {
        val state = viewModel.uiState.value
        assertEquals("", state.clienteSearchText)
        assertEquals(0, state.clienteSeleccionadoId)
        assertNull(state.clienteError)
        assertEquals("", state.usuarioSearchText)
        assertEquals(0, state.usuarioSeleccionadoId)
        assertNull(state.usuarioError)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onClienteSearchChanged con texto corto no busca`() {
        viewModel.onClienteSearchChanged("J")
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.clienteResultados.isEmpty())
        assertFalse(state.clienteBuscando)
    }

    @Test
    fun `onClienteSearchChanged con texto valido trae resultados`() {
        viewModel.onClienteSearchChanged("Cliente 1")
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.clienteResultados.isNotEmpty())
        assertTrue(state.clienteResultados.any { it.strNombreCliente == "Cliente 1" })
    }

    @Test
    fun `onClienteSeleccionado guarda id y nombre`() {
        viewModel.onClienteSearchChanged("Cliente 1")
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onClienteSeleccionado(ClienteFixtures.cliente)

        val state = viewModel.uiState.value
        assertEquals(1, state.clienteSeleccionadoId)
        assertEquals("Juan Pérez", state.clienteSearchText)
        assertTrue(state.clienteResultados.isEmpty())
    }

    @Test
    fun `onClienteLimpiado limpia la seleccion`() {
        viewModel.onClienteSeleccionado(ClienteFixtures.cliente)
        viewModel.onClienteLimpiado()

        val state = viewModel.uiState.value
        assertEquals("", state.clienteSearchText)
        assertEquals(0, state.clienteSeleccionadoId)
        assertTrue(state.clienteResultados.isEmpty())
    }

    @Test
    fun `onUsuarioSearchChanged con texto valido trae resultados`() {
        viewModel.onUsuarioSearchChanged("Usuario 1")
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.usuarioResultados.isNotEmpty())
        assertTrue(state.usuarioResultados.any { it.strNombre == "Usuario 1" })
    }

    @Test
    fun `onUsuarioSeleccionado guarda id y nombre`() {
        viewModel.onUsuarioSearchChanged("Usuario 1")
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onUsuarioSeleccionado(UsuarioFixtures.usuario)

        val state = viewModel.uiState.value
        assertEquals(1, state.usuarioSeleccionadoId)
        assertEquals("Juan Pérez", state.usuarioSearchText)
        assertTrue(state.usuarioResultados.isEmpty())
    }

    @Test
    fun `onUsuarioLimpiado limpia la seleccion`() {
        viewModel.onUsuarioSeleccionado(UsuarioFixtures.usuario)
        viewModel.onUsuarioLimpiado()

        val state = viewModel.uiState.value
        assertEquals("", state.usuarioSearchText)
        assertEquals(0, state.usuarioSeleccionadoId)
        assertTrue(state.usuarioResultados.isEmpty())
    }

    @Test
    fun `onGuardarClicked sin seleccion muestra errores`() {
        viewModel.onGuardarClicked()

        val state = viewModel.uiState.value
        assertNotNull(state.clienteError)
        assertNotNull(state.usuarioError)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `onGuardarClicked con seleccion valida emite VentaCreada`() = runTest {
        viewModel.onClienteSeleccionado(ClienteFixtures.cliente)
        viewModel.onUsuarioSeleccionado(UsuarioFixtures.usuario)

        val events = mutableListOf<VentaCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(events.any { it is VentaCrearEvent.VentaCreada })
        assertEquals(1, fakeVentaRepo.lastIdCliCliente)
        assertEquals(1, fakeVentaRepo.lastIdSegUsuario)
        assertNotNull(fakeVentaRepo.lastDteFechaHoraCompra)
        assertNotNull(fakeVentaRepo.lastStrClaveVenta)
        job.cancel()
    }

    @Test
    fun `onGuardarClicked con error del servidor emite Error`() = runTest {
        fakeVentaRepo.shouldThrowException = true
        viewModel.onClienteSeleccionado(ClienteFixtures.cliente)
        viewModel.onUsuarioSeleccionado(UsuarioFixtures.usuario)

        val events = mutableListOf<VentaCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is VentaCrearEvent.Error })
        job.cancel()
    }

    @Test
    fun `onGuardarClicked con sesion expirada emite SessionExpired`() = runTest {
        fakeVentaRepo.shouldThrowSessionExpired = true
        viewModel.onClienteSeleccionado(ClienteFixtures.cliente)
        viewModel.onUsuarioSeleccionado(UsuarioFixtures.usuario)

        val events = mutableListOf<VentaCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onGuardarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is VentaCrearEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `onCancelarClicked emite NavigateBack`() = runTest {
        val events = mutableListOf<VentaCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onCancelarClicked()
        advanceUntilIdle()

        assertTrue(events.any { it is VentaCrearEvent.NavigateBack })
        job.cancel()
    }

    @Test
    fun `busqueda de cliente con sesion expirada emite SessionExpired`() = runTest {
        fakeClienteRepo.shouldThrowSessionExpired = true

        val events = mutableListOf<VentaCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onClienteSearchChanged("Cliente 1")
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is VentaCrearEvent.SessionExpired })
        job.cancel()
    }

    @Test
    fun `busqueda de usuario con sesion expirada emite SessionExpired`() = runTest {
        fakeUsuarioRepo.shouldThrowSessionExpired = true

        val events = mutableListOf<VentaCrearEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onUsuarioSearchChanged("Usuario 1")
        coroutineRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(events.any { it is VentaCrearEvent.SessionExpired })
        job.cancel()
    }
}
