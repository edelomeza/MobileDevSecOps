package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.domain.model.AuthResult
import com.example.mobiledevsecops.shared.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val useCase = LoginUseCase(fakeRepo)

    @Test
    fun `login con credenciales validas retorna Success`() = runTest {
        val result = useCase("admin", "password123")
        assertEquals(AuthResult.Success, result)
        assertEquals("admin", fakeRepo.capturedUsername)
        assertEquals("password123", fakeRepo.capturedPassword)
    }

    @Test
    fun `login con credenciales invalidas retorna Error`() = runTest {
        fakeRepo.shouldFail = true
        val result = useCase("wrong", "wrong")
        assertTrue(result is AuthResult.Error)
        assertEquals("Credenciales inválidas", (result as AuthResult.Error).message)
    }

    @Test
    fun `login con error de red retorna NetworkError`() = runTest {
        fakeRepo.shouldBeNetworkError = true
        val result = useCase("admin", "pass")
        assertEquals(AuthResult.NetworkError, result)
    }

    @Test
    fun `login con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldBeSessionExpired = true
        val result = useCase("admin", "pass")
        assertEquals(AuthResult.SessionExpired, result)
    }
}
