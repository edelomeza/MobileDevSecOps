package com.example.mobiledevsecops.ui.venta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.data.remote.VentaApi
import com.example.mobiledevsecops.domain.model.Venta
import com.example.mobiledevsecops.domain.repository.VentaRepository
import com.example.mobiledevsecops.domain.usecase.BuscarVentasResult
import com.example.mobiledevsecops.domain.usecase.BuscarVentasUseCase
import com.example.mobiledevsecops.util.Fechas
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VentaUiState(
    val ventas: List<Venta> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val searchText: String = "",
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val isSearching: Boolean = false,
    val isSearchActive: Boolean = false
)

sealed class VentaEvent {
    data object NavigateBack : VentaEvent()
    data object SessionExpired : VentaEvent()
    data class ShowSnackbar(val message: String) : VentaEvent()
}

class VentaViewModel(
    private val ventaRepository: VentaRepository,
    private val buscarVentasUseCase: BuscarVentasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentaUiState())
    val uiState: StateFlow<VentaUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VentaEvent>()
    val events: SharedFlow<VentaEvent> = _events.asSharedFlow()

    init {
        loadPage(1)
    }

    fun loadPage(page: Int) {
        if (page < 1) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = ventaRepository.getVentas(page)
                val items = result.items.take(VentaApi.PAGE_SIZE)
                _uiState.value = _uiState.value.copy(
                    ventas = items,
                    isLoading = false,
                    error = null,
                    currentPage = result.pageNumber,
                    totalPages = result.totalPages,
                    totalCount = result.totalCount,
                    isSearchActive = false,
                    isSearching = false
                )
            } catch (e: SessionExpiredException) {
                _events.emit(VentaEvent.SessionExpired)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar ventas"
                )
            }
        }
    }

    fun onSearchTextChanged(texto: String) {
        _uiState.value = _uiState.value.copy(searchText = texto)
    }

    fun onFechaInicioChanged(millis: Long?) {
        val fecha = millis?.let { Fechas.isoFechaDesdeMillis(it) }
        _uiState.value = _uiState.value.copy(fechaInicio = fecha)
    }

    fun onFechaFinChanged(millis: Long?) {
        val fecha = millis?.let { Fechas.isoFechaDesdeMillis(it) }
        _uiState.value = _uiState.value.copy(fechaFin = fecha)
    }

    fun onBuscarClicked() {
        val state = _uiState.value
        val texto = state.searchText.trim()
        if (texto.isEmpty() && state.fechaInicio.isNullOrBlank() && state.fechaFin.isNullOrBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)

            when (val result = buscarVentasUseCase(texto, state.fechaInicio, state.fechaFin, 1)) {
                is BuscarVentasResult.Success -> {
                    val items = result.page.items.take(VentaApi.PAGE_SIZE)
                    _uiState.value = _uiState.value.copy(
                        ventas = items,
                        isSearching = false,
                        isSearchActive = true,
                        currentPage = result.page.pageNumber,
                        totalPages = result.page.totalPages,
                        totalCount = result.page.totalCount,
                        error = null
                    )
                }
                is BuscarVentasResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(isSearching = false)
                    _events.emit(VentaEvent.ShowSnackbar(result.mensaje))
                }
                is BuscarVentasResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSearching = false)
                    _events.emit(VentaEvent.ShowSnackbar(result.mensaje))
                }
                is BuscarVentasResult.SessionExpired -> {
                    _events.emit(VentaEvent.SessionExpired)
                }
            }
        }
    }

    fun onClearSearch() {
        _uiState.value = VentaUiState()
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
        val state = _uiState.value
        val texto = state.searchText.trim()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = buscarVentasUseCase(texto, state.fechaInicio, state.fechaFin, page)) {
                is BuscarVentasResult.Success -> {
                    val items = result.page.items.take(VentaApi.PAGE_SIZE)
                    _uiState.value = _uiState.value.copy(
                        ventas = items,
                        isLoading = false,
                        currentPage = result.page.pageNumber,
                        totalPages = result.page.totalPages,
                        totalCount = result.page.totalCount,
                        error = null
                    )
                }
                is BuscarVentasResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(VentaEvent.ShowSnackbar(result.mensaje))
                }
                is BuscarVentasResult.SessionExpired -> {
                    _events.emit(VentaEvent.SessionExpired)
                }
                is BuscarVentasResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(VentaEvent.ShowSnackbar(result.mensaje))
                }
            }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(VentaEvent.NavigateBack)
        }
    }
}