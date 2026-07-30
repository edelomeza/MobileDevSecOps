package com.example.mobiledevsecops.ui.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.data.remote.ClienteApi
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.Cliente
import com.example.mobiledevsecops.domain.repository.ClienteRepository
import com.example.mobiledevsecops.domain.usecase.BuscarClientesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClienteUiState(
    val clientes: List<Cliente> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val searchText: String = "",
    val isSearching: Boolean = false,
    val isSearchActive: Boolean = false
)

sealed class ClienteEvent {
    data object NavigateBack : ClienteEvent()
    data object SessionExpired : ClienteEvent()
    data class ShowSnackbar(val message: String) : ClienteEvent()
}

class ClienteViewModel(
    private val clienteRepository: ClienteRepository,
    private val buscarClientesUseCase: BuscarClientesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ClienteEvent>()
    val events: SharedFlow<ClienteEvent> = _events.asSharedFlow()

    init {
        loadPage(1)
    }

    fun loadPage(page: Int) {
        if (page < 1) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = clienteRepository.getClientes(page)
                val items = result.items.take(ClienteApi.PAGE_SIZE)
                _uiState.value = _uiState.value.copy(
                    clientes = items,
                    isLoading = false,
                    error = null,
                    currentPage = result.pageNumber,
                    totalPages = result.totalPages,
                    totalCount = result.totalCount,
                    isSearchActive = false,
                    isSearching = false
                )
            } catch (e: SessionExpiredException) {
                _events.emit(ClienteEvent.SessionExpired)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar clientes"
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

            when (val result = buscarClientesUseCase(texto, 1)) {
                is com.example.mobiledevsecops.domain.usecase.BuscarClientesResult.Success -> {
                    val items = result.page.items.take(ClienteApi.PAGE_SIZE)
                    _uiState.value = _uiState.value.copy(
                        clientes = items,
                        isSearching = false,
                        isSearchActive = true,
                        currentPage = result.page.pageNumber,
                        totalPages = result.page.totalPages,
                        totalCount = result.page.totalCount,
                        error = null
                    )
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarClientesResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(isSearching = false)
                    _events.emit(ClienteEvent.ShowSnackbar(result.mensaje))
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarClientesResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSearching = false)
                    _events.emit(ClienteEvent.ShowSnackbar(result.mensaje))
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarClientesResult.SessionExpired -> {
                    _events.emit(ClienteEvent.SessionExpired)
                }
            }
        }
    }

    fun onClearSearch() {
        _uiState.value = ClienteUiState()
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
            when (val result = buscarClientesUseCase(texto, page)) {
                is com.example.mobiledevsecops.domain.usecase.BuscarClientesResult.Success -> {
                    val items = result.page.items.take(ClienteApi.PAGE_SIZE)
                    _uiState.value = _uiState.value.copy(
                        clientes = items,
                        isLoading = false,
                        currentPage = result.page.pageNumber,
                        totalPages = result.page.totalPages,
                        totalCount = result.page.totalCount,
                        error = null
                    )
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarClientesResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ClienteEvent.ShowSnackbar(result.mensaje))
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarClientesResult.SessionExpired -> {
                    _events.emit(ClienteEvent.SessionExpired)
                }
                is com.example.mobiledevsecops.domain.usecase.BuscarClientesResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ClienteEvent.ShowSnackbar(result.mensaje))
                }
            }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(ClienteEvent.NavigateBack)
        }
    }
}
