package com.example.mobiledevsecops.ui.login

import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.shared.fake.FakeAuthRepository
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var mockTokenManager: TokenManager
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        mockTokenManager = mockk(relaxed = true)
        every { mockTokenManager.getLoginAttempts() } returns Pair(0, 0L)
        val loginUseCase = com.example.mobiledevsecops.domain.usecase.LoginUseCase(fakeAuthRepository)
        viewModel = LoginViewModel(loginUseCase, mockTokenManager)
    }

    @Test
    fun `estado inicial tiene valores por defecto`() {
        val state = viewModel.uiState.value
        assertEquals("", state.username)
        assertEquals("", state.password)
        assertEquals(false, state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.usernameError)
        assertNull(state.passwordError)
    }

    @Test
    fun `onUsernameChanged actualiza el username`() {
        viewModel.onUsernameChanged("admin")
        assertEquals("admin", viewModel.uiState.value.username)
    }

    @Test
    fun `onUsernameChanged limpia el error de username`() {
        viewModel.onLoginClicked()
        viewModel.onUsernameChanged("admin")
        assertNull(viewModel.uiState.value.usernameError)
    }

    @Test
    fun `onPasswordChanged actualiza el password`() {
        viewModel.onPasswordChanged("pass123")
        assertEquals("pass123", viewModel.uiState.value.password)
    }

    @Test
    fun `onPasswordChanged limpia el error de password`() {
        viewModel.onLoginClicked()
        viewModel.onPasswordChanged("pass123")
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `login exitoso emite NavigateToIndex`() = runTest {
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("password123")

        val event = mutableListOf<LoginEvent>()
        val job = launch {
            viewModel.events.collect { event.add(it) }
        }

        viewModel.onLoginClicked()

        assertEquals(true, viewModel.uiState.value.isLoading)
        advanceTimeBy(100)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertTrue(event.any { it is LoginEvent.NavigateToIndex })
        job.cancel()
    }

    @Test
    fun `login con credenciales invalidas muestra error`() = runTest {
        fakeAuthRepository.shouldFail = true
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("wrong")
        viewModel.onLoginClicked()

        advanceTimeBy(100)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `login con campos vacios muestra errores de validacion`() {
        viewModel.onLoginClicked()

        val state = viewModel.uiState.value
        assertEquals("El usuario no puede estar vacío", state.usernameError)
        assertEquals("La contraseña no puede estar vacía", state.passwordError)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `login con error de red muestra mensaje de conexion`() = runTest {
        fakeAuthRepository.shouldBeNetworkError = true
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("pass")
        viewModel.onLoginClicked()

        advanceTimeBy(100)
        assertEquals("Error de conexión. Verifica tu conexión a internet.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `el error se auto-despues de 7 segundos`() = runTest {
        fakeAuthRepository.shouldFail = true
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("wrong")
        viewModel.onLoginClicked()

        advanceTimeBy(100)
        assertNotNull(viewModel.uiState.value.errorMessage)

        advanceTimeBy(7000)
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
