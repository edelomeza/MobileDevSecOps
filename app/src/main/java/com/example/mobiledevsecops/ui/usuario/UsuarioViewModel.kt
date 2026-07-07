package com.example.mobiledevsecops.ui.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.data.remote.UsuarioApi
import com.example.mobiledevsecops.domain.model.Usuario
import com.example.mobiledevsecops.domain.repository.UsuarioRepository
import com.example.mobiledevsecops.domain.usecase.BuscarUsuariosUseCase
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
    val totalCount: Int = 0,
    val searchText: String = "",
    val isSearching: Boolean = false,
    val isSearchActive: Boolean = false
)

sealed class UsuarioEvent {
    data object NavigateBack : UsuarioEvent()
    data object SessionExpired : UsuarioEvent()
    data class ShowSnackbar(val message: String) : UsuarioEvent()
}

class UsuarioViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val buscarUsuariosUseCase: BuscarUsuariosUseCase
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
                    totalCount = result.totalCount,
                    isSearchActive = false,
                    isSearching = false
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

    fun onSearchTextChanged(texto: String) {
        _uiState.value = _uiState.value.copy(searchText = texto)
    }

    fun onBuscarClicked() {
        val texto = _uiState.value.searchText.trim()
        if (texto.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)

            when (val result = buscarUsuariosUseCase(texto, 1)) {
                is com.example.mobiledevsecops.domain.usecase.BuscarUsuariosResult.Success -> {
                    val items = result.page.items.take(UsuarioApi.PAGE_SIZE)
                    _uiState.value = _uiState.value.copy(
                        usuarios = items,
                        isSearching = false,
                        isSearchActive = true,
                        currentPage = result.page.pageNumber,
                        totalPages = result.page.totalPages,
                        totalCount = result.page.totalCount,
                        error = null
                    )
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarUsuariosResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isSearching = false
                    )
                    _events.emit(UsuarioEvent.ShowSnackbar(result.mensaje))
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarUsuariosResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSearching = false
                    )
                    _events.emit(UsuarioEvent.ShowSnackbar(result.mensaje))
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarUsuariosResult.SessionExpired -> {
                    _events.emit(UsuarioEvent.SessionExpired)
                }
            }
        }
    }

    fun onClearSearch() {
        _uiState.value = UsuarioUiState()
        loadPage(1)
    }

    fun goToNextPage() {
        val current = _uiState.value.currentPage
        if (current < _uiState.value.totalPages) {
            if (_uiState.value.isSearchActive) {
                searchPage(current + 1)
            } else {
                loadPage(current + 1)
            }
        }
    }

    fun goToPreviousPage() {
        val current = _uiState.value.currentPage
        if (current > 1) {
            if (_uiState.value.isSearchActive) {
                searchPage(current - 1)
            } else {
                loadPage(current - 1)
            }
        }
    }

    private fun searchPage(page: Int) {
        val texto = _uiState.value.searchText.trim()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = buscarUsuariosUseCase(texto, page)) {
                is com.example.mobiledevsecops.domain.usecase.BuscarUsuariosResult.Success -> {
                    val items = result.page.items.take(UsuarioApi.PAGE_SIZE)
                    _uiState.value = _uiState.value.copy(
                        usuarios = items,
                        isLoading = false,
                        currentPage = result.page.pageNumber,
                        totalPages = result.page.totalPages,
                        totalCount = result.page.totalCount,
                        error = null
                    )
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarUsuariosResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                    _events.emit(UsuarioEvent.ShowSnackbar(result.mensaje))
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarUsuariosResult.SessionExpired -> {
                    _events.emit(UsuarioEvent.SessionExpired)
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarUsuariosResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                    _events.emit(UsuarioEvent.ShowSnackbar(result.mensaje))
                }
            }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(UsuarioEvent.NavigateBack)
        }
    }
}
