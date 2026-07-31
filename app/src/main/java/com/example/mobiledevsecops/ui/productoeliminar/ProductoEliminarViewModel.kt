package com.example.mobiledevsecops.ui.productoeliminar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.usecase.EliminarProductoResult
import com.example.mobiledevsecops.domain.usecase.EliminarProductoUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class ProductoEliminarUiState(
    val id: Int = 0,
    val nombreProducto: String = "",
    val urlImagen: String = "",
    val descripcion: String = "",
    val existencia: String = "",
    val precio: String = "",
    val rowVersion: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ProductoEliminarEvent {
    data object NavigateBack : ProductoEliminarEvent()
    data object ProductoEliminado : ProductoEliminarEvent()
    data object Error : ProductoEliminarEvent()
    data object SessionExpired : ProductoEliminarEvent()
}

class ProductoEliminarViewModel(
    private val eliminarProductoUseCase: EliminarProductoUseCase,
    private val params: ProductoEliminarParams
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProductoEliminarUiState(
            id = params.id,
            nombreProducto = params.strNombreProducto,
            urlImagen = params.strURLImagen ?: "",
            descripcion = params.strDescripcion ?: "",
            existencia = params.intNumeroExistencia.toString(),
            precio = String.format(Locale.US, "%.2f", params.decPrecio),
            rowVersion = params.rowVersion
        )
    )
    val uiState: StateFlow<ProductoEliminarUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductoEliminarEvent>()
    val events: SharedFlow<ProductoEliminarEvent> = _events.asSharedFlow()

    fun onEliminarClicked() {
        val state = _uiState.value
        val validationErrors = eliminarProductoUseCase.validar(state.id, state.rowVersion)

        if (validationErrors.isNotEmpty()) {
            viewModelScope.launch {
                _events.emit(ProductoEliminarEvent.Error)
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = eliminarProductoUseCase(state.id, state.rowVersion)) {
                is EliminarProductoResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoEliminarEvent.ProductoEliminado)
                }
                is EliminarProductoResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoEliminarEvent.Error)
                }
                is EliminarProductoResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.mensaje)
                    _events.emit(ProductoEliminarEvent.Error)
                }
                is EliminarProductoResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoEliminarEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(ProductoEliminarEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
