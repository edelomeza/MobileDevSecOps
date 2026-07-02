package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.domain.model.AuthResult
import com.example.mobiledevsecops.shared.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LogoutUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val useCase = LogoutUseCase(fakeRepo)

    @Test
    fun `logout exitoso retorna Success y limpia token`() = runTest {
        val result = useCase()
        assertEquals(AuthResult.Success, result)
        assertEquals(null, fakeRepo.getToken())
    }
}
