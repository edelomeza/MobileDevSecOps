package com.example.mobiledevsecops.ui.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.data.remote.UsuarioApi
import com.example.mobiledevsecops.domain.model.Usuario
import com.example.mobiledevsecops.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsuarioUiState(
    val usuarios: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0
)

sealed class UsuarioEvent {
    data object NavigateBack : UsuarioEvent()
    data object SessionExpired : UsuarioEvent()
}

class UsuarioViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UsuarioEvent>()
    val events: SharedFlow<UsuarioEvent> = _events.asSharedFlow()

    init {
        loadPage(1)
    }

    fun loadPage(page: Int) {
        if (page < 1) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = usuarioRepository.getUsuarios(page)
                val items = result.items.take(UsuarioApi.PAGE_SIZE)
                _uiState.value = _uiState.value.copy(
                    usuarios = items,
                    isLoading = false,
                    error = null,
                    currentPage = result.pageNumber,
                    totalPages = result.totalPages,
                    totalCount = result.totalCount
                )
            } catch (e: SessionExpiredException) {
                _events.emit(UsuarioEvent.SessionExpired)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar usuarios"
                )
            }
        }
    }

    fun goToNextPage() {
        val current = _uiState.value.currentPage
        if (current < _uiState.value.totalPages) {
            loadPage(current + 1)
        }
    }

    fun goToPreviousPage() {
        val current = _uiState.value.currentPage
        if (current > 1) {
            loadPage(current - 1)
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(UsuarioEvent.NavigateBack)
        }
    }
}
