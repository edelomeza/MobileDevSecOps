package com.example.mobiledevsecops.domain.model

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    data object SessionExpired : AuthResult()
    data object NetworkError : AuthResult()
}
