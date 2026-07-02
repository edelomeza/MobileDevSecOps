package com.example.mobiledevsecops.shared.security

class FakeTokenManager {
    private var token: String? = null

    fun saveToken(token: String): Boolean {
        this.token = token
        return true
    }

    fun getToken(): String? = token

    fun clearAll() {
        token = null
    }

    fun isLoggedIn(): Boolean = token != null
}
