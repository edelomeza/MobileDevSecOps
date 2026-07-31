package com.example.mobiledevsecops.ui.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.Producto
import com.example.mobiledevsecops.domain.repository.ProductoRepository
import com.example.mobiledevsecops.domain.usecase.BuscarProductosResult
import com.example.mobiledevsecops.domain.usecase.BuscarProductosUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductoUiState(
    val productos: List<Producto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val searchText: String = "",
    val isSearching: Boolean = false,
    val isSearchActive: Boolean = false
)

sealed class ProductoEvent {
    data object NavigateBack : ProductoEvent()
    data object SessionExpired : ProductoEvent()
    data class ShowSnackbar(val message: String) : ProductoEvent()
}

class ProductoViewModel(
    private val productoRepository: ProductoRepository,
    private val buscarProductosUseCase: BuscarProductosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductoEvent>()
    val events: SharedFlow<ProductoEvent> = _events.asSharedFlow()

    init {
        loadPage(1)
    }

    fun loadPage(page: Int) {
        if (page < 1) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = productoRepository.getProductos(page)
                _uiState.value = _uiState.value.copy(
                    productos = result.items,
                    isLoading = false,
                    error = null,
                    currentPage = result.pageNumber,
                    totalPages = result.totalPages,
                    totalCount = result.totalCount,
                    isSearchActive = false,
                    isSearching = false
                )
            } catch (e: SessionExpiredException) {
                _events.emit(ProductoEvent.SessionExpired)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar productos"
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
            when (val result = buscarProductosUseCase(texto, 1)) {
                is BuscarProductosResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        productos = result.page.items,
                        isSearching = false,
                        isSearchActive = true,
                        currentPage = result.page.pageNumber,
                        totalPages = result.page.totalPages,
                        totalCount = result.page.totalCount,
                        error = null
                    )
                }
                is BuscarProductosResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(isSearching = false)
                    _events.emit(ProductoEvent.ShowSnackbar(result.mensaje))
                }
                is BuscarProductosResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSearching = false)
                    _events.emit(ProductoEvent.ShowSnackbar(result.mensaje))
                }
                is BuscarProductosResult.SessionExpired -> {
                    _events.emit(ProductoEvent.SessionExpired)
                }
            }
        }
    }

    fun onClearSearch() {
        _uiState.value = ProductoUiState()
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
            when (val result = buscarProductosUseCase(texto, page)) {
                is BuscarProductosResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        productos = result.page.items,
                        isLoading = false,
                        currentPage = result.page.pageNumber,
                        totalPages = result.page.totalPages,
                        totalCount = result.page.totalCount,
                        error = null
                    )
                }
                is BuscarProductosResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoEvent.ShowSnackbar(result.mensaje))
                }
                is BuscarProductosResult.SessionExpired -> {
                    _events.emit(ProductoEvent.SessionExpired)
                }
                is BuscarProductosResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoEvent.ShowSnackbar(result.mensaje))
                }
            }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(ProductoEvent.NavigateBack)
        }
    }
}
