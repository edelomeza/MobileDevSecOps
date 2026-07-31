package com.example.mobiledevsecops.ui.productocrear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.usecase.CrearProductoResult
import com.example.mobiledevsecops.domain.usecase.CrearProductoUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductoCrearUiState(
    val nombreProducto: String = "",
    val urlImagen: String = "",
    val descripcion: String = "",
    val existencia: String = "",
    val precio: String = "",
    val nombreProductoError: String? = null,
    val urlImagenError: String? = null,
    val descripcionError: String? = null,
    val existenciaError: String? = null,
    val precioError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ProductoCrearEvent {
    data object NavigateBack : ProductoCrearEvent()
    data object ProductoCreado : ProductoCrearEvent()
    data object Error : ProductoCrearEvent()
    data object SessionExpired : ProductoCrearEvent()
}

class ProductoCrearViewModel(
    private val crearProductoUseCase: CrearProductoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoCrearUiState())
    val uiState: StateFlow<ProductoCrearUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductoCrearEvent>()
    val events: SharedFlow<ProductoCrearEvent> = _events.asSharedFlow()

    fun onNombreProductoChanged(value: String) {
        _uiState.value = _uiState.value.copy(nombreProducto = value, nombreProductoError = null)
    }

    fun onUrlImagenChanged(value: String) {
        _uiState.value = _uiState.value.copy(urlImagen = value, urlImagenError = null)
    }

    fun onDescripcionChanged(value: String) {
        _uiState.value = _uiState.value.copy(descripcion = value, descripcionError = null)
    }

    fun onExistenciaChanged(value: String) {
        _uiState.value = _uiState.value.copy(existencia = value, existenciaError = null)
    }

    fun onPrecioChanged(value: String) {
        _uiState.value = _uiState.value.copy(precio = value, precioError = null)
    }

    fun onGuardarClicked() {
        val state = _uiState.value
        val existencia = state.existencia.toIntOrNull() ?: -1
        val precio = state.precio.toDoubleOrNull() ?: -1.0

        val validationErrors = crearProductoUseCase.validar(
            state.nombreProducto,
            state.urlImagen.takeIf { it.isNotBlank() },
            state.descripcion.takeIf { it.isNotBlank() },
            existencia,
            precio
        )

        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                nombreProductoError = validationErrors["strNombreProducto"],
                urlImagenError = validationErrors["strURLImagen"],
                descripcionError = validationErrors["strDescripcion"],
                existenciaError = validationErrors["intNumeroExistencia"],
                precioError = validationErrors["decPrecio"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = crearProductoUseCase(
                state.nombreProducto,
                state.urlImagen.takeIf { it.isNotBlank() },
                state.descripcion.takeIf { it.isNotBlank() },
                existencia,
                precio
            )) {
                is CrearProductoResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoCrearEvent.ProductoCreado)
                }
                is CrearProductoResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombreProductoError = result.errores["strNombreProducto"],
                        urlImagenError = result.errores["strURLImagen"],
                        descripcionError = result.errores["strDescripcion"],
                        existenciaError = result.errores["intNumeroExistencia"],
                        precioError = result.errores["decPrecio"]
                    )
                }
                is CrearProductoResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoCrearEvent.Error)
                }
                is CrearProductoResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoCrearEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(ProductoCrearEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
