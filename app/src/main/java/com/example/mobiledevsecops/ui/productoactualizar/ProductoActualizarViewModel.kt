package com.example.mobiledevsecops.ui.productoactualizar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.usecase.ActualizarProductoResult
import com.example.mobiledevsecops.domain.usecase.ActualizarProductoUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductoActualizarUiState(
    val id: Int = 0,
    val nombreProducto: String = "",
    val urlImagen: String = "",
    val descripcion: String = "",
    val existencia: String = "",
    val precio: String = "",
    val rowVersion: String = "",
    val nombreProductoError: String? = null,
    val urlImagenError: String? = null,
    val descripcionError: String? = null,
    val existenciaError: String? = null,
    val precioError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ProductoActualizarEvent {
    data object NavigateBack : ProductoActualizarEvent()
    data object ProductoActualizado : ProductoActualizarEvent()
    data object Error : ProductoActualizarEvent()
    data object SessionExpired : ProductoActualizarEvent()
}

class ProductoActualizarViewModel(
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val params: ProductoActualizarParams
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProductoActualizarUiState(
            id = params.id,
            nombreProducto = params.strNombreProducto,
            urlImagen = params.strURLImagen ?: "",
            descripcion = params.strDescripcion ?: "",
            existencia = params.intNumeroExistencia.toString(),
            precio = params.decPrecio.toString(),
            rowVersion = params.rowVersion
        )
    )
    val uiState: StateFlow<ProductoActualizarUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductoActualizarEvent>()
    val events: SharedFlow<ProductoActualizarEvent> = _events.asSharedFlow()

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

    fun onActualizarClicked() {
        val state = _uiState.value
        val existencia = state.existencia.toIntOrNull() ?: -1
        val precio = state.precio.toDoubleOrNull() ?: -1.0

        val validationErrors = actualizarProductoUseCase.validar(
            state.id,
            state.nombreProducto,
            state.urlImagen.takeIf { it.isNotBlank() },
            state.descripcion.takeIf { it.isNotBlank() },
            existencia,
            precio,
            state.rowVersion
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

            when (val result = actualizarProductoUseCase(
                state.id,
                state.nombreProducto,
                state.urlImagen.takeIf { it.isNotBlank() },
                state.descripcion.takeIf { it.isNotBlank() },
                existencia,
                precio,
                state.rowVersion
            )) {
                is ActualizarProductoResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoActualizarEvent.ProductoActualizado)
                }
                is ActualizarProductoResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombreProductoError = result.errores["strNombreProducto"],
                        urlImagenError = result.errores["strURLImagen"],
                        descripcionError = result.errores["strDescripcion"],
                        existenciaError = result.errores["intNumeroExistencia"],
                        precioError = result.errores["decPrecio"]
                    )
                }
                is ActualizarProductoResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.mensaje)
                    _events.emit(ProductoActualizarEvent.Error)
                }
                is ActualizarProductoResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ProductoActualizarEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(ProductoActualizarEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
