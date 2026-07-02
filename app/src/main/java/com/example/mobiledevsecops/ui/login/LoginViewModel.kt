package com.example.mobiledevsecops.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.domain.model.AuthResult
import com.example.mobiledevsecops.domain.usecase.LoginUseCase
import com.example.mobiledevsecops.util.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null
)

sealed class LoginEvent {
    data object NavigateToIndex : LoginEvent()
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    private var consecutiveFailures: Int
    private var lockoutUntil: Long

    init {
        val (attempts, lockout) = tokenManager.getLoginAttempts()
        consecutiveFailures = attempts
        lockoutUntil = lockout
    }

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null,
            errorMessage = null
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        )
    }

    fun onLoginClicked() {
        val currentState = _uiState.value
        val username = currentState.username.trim()
        val password = currentState.password.trim()

        val now = System.currentTimeMillis()
        if (now < lockoutUntil) {
            val remainingSec = ((lockoutUntil - now) / 1000) + 1
            showError("Demasiados intentos. Espera $remainingSec segundos.")
            return
        }

        var hasError = false

        if (username.isBlank()) {
            _uiState.value = _uiState.value.copy(usernameError = "El usuario no puede estar vacío")
            hasError = true
        } else if (username.length > USERNAME_MAX_LENGTH) {
            _uiState.value = _uiState.value.copy(usernameError = "El usuario no puede exceder $USERNAME_MAX_LENGTH caracteres")
            hasError = true
        } else if (!USERNAME_REGEX.matches(username)) {
            _uiState.value = _uiState.value.copy(usernameError = "Solo letras, números, puntos, guiones y @")
            hasError = true
        }

        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "La contraseña no puede estar vacía")
            hasError = true
        }

        if (hasError) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            if (BuildConfig.DEBUG) {
                Logger.d("Login initiated for user: ${username.take(2)}***, consecutive failure ${consecutiveFailures + 1}")
            }
            when (val result = loginUseCase(username, password)) {
                is AuthResult.Success -> {
                    consecutiveFailures = 0
                    lockoutUntil = 0
                    tokenManager.clearLoginAttempts()
                    _uiState.value = _uiState.value.copy(isLoading = false, password = "")
                    if (BuildConfig.DEBUG) Logger.i("Login successful, navigating to Index")
                    _events.emit(LoginEvent.NavigateToIndex)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(password = "")
                    consecutiveFailures++
                    val remainingBeforeLockout = MAX_LOGIN_ATTEMPTS - (consecutiveFailures % MAX_LOGIN_ATTEMPTS)
                    if (consecutiveFailures >= MAX_LOGIN_ATTEMPTS) {
                        val shift = (consecutiveFailures - MAX_LOGIN_ATTEMPTS).coerceAtMost(MAX_BACKOFF_SHIFTS)
                        val delayMs = BACKOFF_BASE_MS * (1 shl shift)
                        lockoutUntil = System.currentTimeMillis() + delayMs
                        val totalSec = delayMs / 1000
                        if (BuildConfig.DEBUG) Logger.w("Login failed, locked out for ${totalSec}s (failure #$consecutiveFailures)")
                        showError("Demasiados intentos fallidos. Espera ${totalSec}s.")
                    } else {
                        if (BuildConfig.DEBUG) Logger.w("Login failed (failure #$consecutiveFailures): ${result.message}")
                        showError("${result.message} ($remainingBeforeLockout intento(s) restante(s))")
                    }
                    tokenManager.saveLoginAttempts(consecutiveFailures, lockoutUntil)
                }
                is AuthResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(password = "")
                    if (BuildConfig.DEBUG) Logger.w("Login failed: network error")
                    showError("Error de conexión. Verifica tu conexión a internet.")
                }
                is AuthResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(password = "")
                    if (BuildConfig.DEBUG) Logger.w("Login failed: session expired")
                    showError("Sesión expirada. Intenta de nuevo.")
                }
            }
        }
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
        viewModelScope.launch {
            delay(ERROR_DISPLAY_DURATION_MS)
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    companion object {
        private const val MAX_LOGIN_ATTEMPTS = 5
        private const val BACKOFF_BASE_MS = 30_000L
        private const val MAX_BACKOFF_SHIFTS = 5
        private const val ERROR_DISPLAY_DURATION_MS = 7000L
        private val USERNAME_REGEX = Regex("^[a-zA-Z0-9._@-]+$")
        private const val USERNAME_MAX_LENGTH = 50
    }
}
