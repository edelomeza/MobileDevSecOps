package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.domain.model.AuthResult
import com.example.mobiledevsecops.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): AuthResult {
        return authRepository.login(username, password)
    }
}
