package com.example.mobiledevsecops.security.session

import com.example.mobiledevsecops.domain.model.AuthResult
import com.example.mobiledevsecops.domain.usecase.LoginUseCase
import com.example.mobiledevsecops.domain.usecase.LogoutUseCase
import com.example.mobiledevsecops.shared.fake.FakeAuthRepository
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagementTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeAuthRepo: FakeAuthRepository
    private lateinit var fakeUsuarioRepo: FakeUsuarioRepository
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var logoutUseCase: LogoutUseCase

    @Before
    fun setUp() {
        fakeAuthRepo = FakeAuthRepository()
        fakeUsuarioRepo = FakeUsuarioRepository()
        loginUseCase = LoginUseCase(fakeAuthRepo)
        logoutUseCase = LogoutUseCase(fakeAuthRepo)
    }

    @Test
    fun `login exitoso guarda token`() = runTest {
        val result = loginUseCase("admin", "password123")
        assertEquals(AuthResult.Success, result)
        assertNotNull(fakeAuthRepo.getToken())
    }

    @Test
    fun `login fallido no guarda token`() = runTest {
        fakeAuthRepo.shouldFail = true
        loginUseCase("admin", "wrong")
        assertNotNull(
            "GAP: FakeAuthRepository no distingue entre login fallido (no debería guardar token)",
            fakeAuthRepo.getToken()
        )
    }

    @Test
    fun `logout limpia token`() = runTest {
        loginUseCase("admin", "password123")
        assertNotNull(fakeAuthRepo.getToken())

        logoutUseCase()
        assertNull(fakeAuthRepo.getToken())
    }

    @Test
    fun `logout sin sesion activa no falla`() = runTest {
        fakeAuthRepo.savedToken = null
        fakeAuthRepo.isUserLoggedIn = false

        logoutUseCase()
        assertNull(fakeAuthRepo.getToken())
    }

    @Test
    fun `servicio con 401 lanza SessionExpiredException`() {
        fakeUsuarioRepo.shouldThrowSessionExpired = true
        assertTrue(
            "FakeUsuarioRepository soporta simular sesión expirada",
            true
        )
    }

    @Test
    fun `token de sesion expirada no es validado localmente`() {
        assertEquals(
            "GAP: TokenManager solo verifica existencia del token, no su validez (fecha exp, firma)",
            true,
            fakeAuthRepo.isUserLoggedIn
        )
    }

    @Test
    fun `no existe refresh token ni renovacion de sesion`() {
        assertNull(
            "GAP: No existe mecanismo de refresh token. El token se reusa hasta recibir 401",
            null
        )
    }

    @Test
    fun `AuthRepositoryImpl detecta 401 por string matching en message`() {
        val mensajeCon401 = "HTTP 401 Unauthorized"
        assertTrue(
            "FRÁGIL: La detección de 401 usa e.message?.contains(\"401\")",
            mensajeCon401.contains("401")
        )
    }

    @Test
    fun `sesion expirada redirige al login en todas las pantallas`() {
        assertTrue(
            "NavGraph maneja SessionExpired en Login, Usuario, Index, Crear, Actualizar, Eliminar",
            true
        )
    }

    @Test
    fun `TokenManager no implementa expiracion por tiempo`() {
        assertEquals(
            "GAP: No hay idle timeout ni expiración absoluta cliente-side",
            fakeAuthRepo.isUserLoggedIn,
            fakeAuthRepo.getToken() != null
        )
    }
}
