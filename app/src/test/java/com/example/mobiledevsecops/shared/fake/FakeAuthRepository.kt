package com.example.mobiledevsecops.shared.fake

import com.example.mobiledevsecops.domain.model.AuthResult
import com.example.mobiledevsecops.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {

    var shouldFail = false
    var shouldBeNetworkError = false
    var shouldBeSessionExpired = false
    var isUserLoggedIn = true
    var savedToken: String? = "fake-jwt-token"
    var capturedUsername: String? = null
    var capturedPassword: String? = null

    override suspend fun login(username: String, password: String): AuthResult {
        capturedUsername = username
        capturedPassword = password

        return when {
            shouldFail -> AuthResult.Error("Credenciales inválidas")
            shouldBeNetworkError -> AuthResult.NetworkError
            shouldBeSessionExpired -> AuthResult.SessionExpired
            else -> AuthResult.Success
        }
    }

    override suspend fun logout(): AuthResult {
        savedToken = null
        isUserLoggedIn = false
        return AuthResult.Success
    }

    override suspend fun isLoggedIn(): Boolean = isUserLoggedIn

    override fun getToken(): String? = savedToken
}
