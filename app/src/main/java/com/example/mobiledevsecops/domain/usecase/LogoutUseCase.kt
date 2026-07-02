package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.domain.model.AuthResult
import com.example.mobiledevsecops.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult {
        return authRepository.logout()
    }
}
