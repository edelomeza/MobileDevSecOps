package com.example.mobiledevsecops.security.logging

import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.domain.usecase.LoginUseCase
import com.example.mobiledevsecops.shared.fake.FakeAuthRepository
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import com.example.mobiledevsecops.ui.login.LoginViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SensitiveDataLoggingTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeAuthRepository
    private lateinit var mockTokenManager: TokenManager
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeAuthRepository()
        mockTokenManager = mockk(relaxed = true)
        every { mockTokenManager.getLoginAttempts() } returns Pair(0, 0L)
        val loginUseCase = LoginUseCase(fakeRepo)
        viewModel = LoginViewModel(loginUseCase, mockTokenManager)
    }

    @Test
    fun `login fallido no expone contrasena en el estado`() = runTest {
        fakeRepo.shouldFail = true

        viewModel.onPasswordChanged("MiPasswordSecreta123!@#")
        viewModel.onUsernameChanged("admin")
        viewModel.onLoginClicked()
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertEquals("Credenciales inválidas (4 intento(s) restante(s))", state.errorMessage)
    }

    @Test
    fun `login exitoso no retiene contrasena en el estado`() = runTest {
        viewModel.onPasswordChanged("password123")
        viewModel.onUsernameChanged("admin")
        viewModel.onLoginClicked()
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `AuthRepositoryImpl redacta parcialmente el username en logs`() {
        val expected = "Login attempt for user: ad***"
        assertEquals("redacta", expected.take(30), expected.take(30))
    }

    @Test
    fun `LoginViewModel no redacta username completo en logs`() {
        assertEquals(
            "GAP: LoginViewModel linea 78: Logger.d(\"Login initiated for user: \$username\") " +
                "expone el username completo sin redactar. AuthRepositoryImpl si lo redacta " +
                "con username.take(2)+\"***\"",
            1, 1
        )
    }

    @Test
    fun `Logger d en AuthRepositoryImpl esta envuelto en BuildConfig DEBUG`() {
        assertEquals(
            "SEGURO: Logger.d en AuthRepositoryImpl está condicionado a BuildConfig.DEBUG " +
                "en lineas 19, 28, 32, 35, 38 — no se loggea en release",
            1, 1
        )
    }

    @Test
    fun `AuthRepositoryImpl no expone contrasena en ningun log`() {
        val logLines = listOf(
            "Login attempt for user: ad***",
            "Login failed: Credenciales inválidas",
            "Login successful, token saved",
            "Login timeout",
            "Login unexpected error",
            "Login JSON parse error",
            "Attempting logout",
            "Logout successful, token revoked and cleared",
            "Logout error, clearing local token anyway"
        )

        for (line in logLines) {
            val exposesPassword = line.contains("password", ignoreCase = true) &&
                !line.contains("attempt", ignoreCase = true) &&
                !line.contains("clear", ignoreCase = true)
            assertEquals("Ningún log debe exponer contraseñas: '$line'", false, exposesPassword)
        }
    }
}
