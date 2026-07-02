package com.example.mobiledevsecops.ui.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.domain.usecase.LogoutUseCase
import com.example.mobiledevsecops.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IndexUiState(
    val isLoggingOut: Boolean = false
)

sealed class IndexEvent {
    data object NavigateToLogin : IndexEvent()
    data object NavigateToUsuario : IndexEvent()
}

class IndexViewModel(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(IndexUiState())
    val uiState: StateFlow<IndexUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<IndexEvent>()
    val events: SharedFlow<IndexEvent> = _events.asSharedFlow()

    fun onLogoutClicked() {
        _uiState.value = _uiState.value.copy(isLoggingOut = true)

        viewModelScope.launch {
            if (BuildConfig.DEBUG) Logger.d("Initiating logout")
            logoutUseCase()
            if (BuildConfig.DEBUG) Logger.i("Logout completed, navigating to Login")
            _events.emit(IndexEvent.NavigateToLogin)
        }
    }

    fun onNavigateToUsuario() {
        if (BuildConfig.DEBUG) Logger.d("Navegando a Usuario sin verificar token local")
        viewModelScope.launch {
            _events.emit(IndexEvent.NavigateToUsuario)
        }
    }
}
