package com.example.mobiledevsecops.domain.repository

import com.example.mobiledevsecops.domain.model.AuthResult

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthResult
    suspend fun logout(): AuthResult
    suspend fun isLoggedIn(): Boolean
    fun getToken(): String?
}
