package com.example.mobiledevsecops.ui.empleado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.data.remote.EmpleadoApi
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.EmpCatTipoEmpleado
import com.example.mobiledevsecops.domain.model.Empleado
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmpleadoUiState(
    val empleados: List<Empleado> = emptyList(),
    val tiposEmpleado: List<EmpCatTipoEmpleado> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val searchText: String = "",
    val selectedTipoEmpleadoId: Int? = null,
    val isSearching: Boolean = false
)

sealed class EmpleadoEvent {
    data object NavigateBack : EmpleadoEvent()
    data object SessionExpired : EmpleadoEvent()
}

class EmpleadoViewModel(
    private val empleadoRepository: EmpleadoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmpleadoUiState())
    val uiState: StateFlow<EmpleadoUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EmpleadoEvent>()
    val events: SharedFlow<EmpleadoEvent> = _events.asSharedFlow()

    init {
        loadTiposEmpleado()
        loadPage(1)
    }

    fun loadTiposEmpleado() {
        viewModelScope.launch {
            try {
                val tipos = empleadoRepository.getTiposEmpleado()
                _uiState.value = _uiState.value.copy(tiposEmpleado = tipos)
            } catch (_: Exception) {
            }
        }
    }

    fun loadPage(page: Int) {
        if (page < 1) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val state = _uiState.value

                val result = if (state.isSearching || state.selectedTipoEmpleadoId != null) {
                    empleadoRepository.buscarEmpleados(
                        texto = state.searchText.ifBlank { null },
                        idTipoEmpleado = state.selectedTipoEmpleadoId,
                        page = page
                    )
                } else {
                    empleadoRepository.getEmpleados(page)
                }

                val items = result.items.take(EmpleadoApi.PAGE_SIZE)
                _uiState.value = _uiState.value.copy(
                    empleados = items,
                    isLoading = false,
                    error = null,
                    currentPage = result.pageNumber,
                    totalPages = result.totalPages,
                    totalCount = result.totalCount
                )
            } catch (e: SessionExpiredException) {
                _events.emit(EmpleadoEvent.SessionExpired)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar empleados"
                )
            }
        }
    }

    fun onSearchTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(searchText = value, isSearching = value.isNotBlank())
    }

    fun onSearch() {
        loadPage(1)
    }

    fun onTipoEmpleadoFilterChanged(id: Int?) {
        _uiState.value = _uiState.value.copy(selectedTipoEmpleadoId = id)
        loadPage(1)
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchText = "",
            selectedTipoEmpleadoId = null,
            isSearching = false
        )
        loadPage(1)
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
            _events.emit(EmpleadoEvent.NavigateBack)
        }
    }
}
