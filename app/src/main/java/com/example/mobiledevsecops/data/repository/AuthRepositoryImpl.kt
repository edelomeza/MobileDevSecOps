package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.data.remote.AuthApi
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.AuthResult
import com.example.mobiledevsecops.domain.repository.AuthRepository
import com.example.mobiledevsecops.util.Logger
import java.io.IOException
import io.ktor.serialization.JsonConvertException

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): AuthResult {
        return try {
            if (BuildConfig.DEBUG) Logger.d("Login attempt for user: ${username.take(2)}***")
            val response = authApi.login(username, password)
            val token = response.token

            if (!token.isNullOrBlank()) {
                if (tokenManager.saveToken(token)) {
                    if (BuildConfig.DEBUG) Logger.i("Login successful, token saved")
                    AuthResult.Success
                } else {
                    if (BuildConfig.DEBUG) Logger.w("Token JWT inválido recibido del servidor")
                    AuthResult.Error("Token JWT inválido recibido del servidor")
                }
            } else {
                if (BuildConfig.DEBUG) Logger.w("Login failed: ${response.message}")
                AuthResult.Error(response.message ?: "Credenciales inválidas")
            }
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) Logger.e("Login timeout", e)
            AuthResult.NetworkError
        } catch (e: JsonConvertException) {
            if (BuildConfig.DEBUG) Logger.e("Login JSON parse error", e)
            AuthResult.Error("Error de comunicación con el servidor")
        } catch (e: SessionExpiredException) {
            if (BuildConfig.DEBUG) Logger.w("Login session expired", e)
            tokenManager.clearAll()
            AuthResult.SessionExpired
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Logger.e("Login unexpected error", e)
            AuthResult.NetworkError
        }
    }

    override suspend fun logout(): AuthResult {
        return try {
            if (BuildConfig.DEBUG) Logger.d("Attempting logout")
            authApi.logout()
            tokenManager.clearAll()
            if (BuildConfig.DEBUG) Logger.i("Logout successful, token revoked and cleared")
            AuthResult.Success
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Logger.e("Logout error, clearing local token anyway", e)
            tokenManager.clearAll()
            AuthResult.Success
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    override fun getToken(): String? {
        return tokenManager.getToken()
    }
}
