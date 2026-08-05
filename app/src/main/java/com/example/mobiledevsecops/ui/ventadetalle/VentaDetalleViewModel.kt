package com.example.mobiledevsecops.ui.ventadetalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.model.EstadoVenta
import com.example.mobiledevsecops.domain.model.ProductoAutocomplete
import com.example.mobiledevsecops.domain.model.VentaDetalle
import com.example.mobiledevsecops.domain.usecase.ActualizarEstadoVentaResult
import com.example.mobiledevsecops.domain.usecase.ActualizarEstadoVentaUseCase
import com.example.mobiledevsecops.domain.usecase.BuscarProductosAutocompleteResult
import com.example.mobiledevsecops.domain.usecase.BuscarProductosAutocompleteUseCase
import com.example.mobiledevsecops.domain.usecase.CrearVentaDetalleResult
import com.example.mobiledevsecops.domain.usecase.CrearVentaDetalleUseCase
import com.example.mobiledevsecops.domain.usecase.EliminarVentaDetalleResult
import com.example.mobiledevsecops.domain.usecase.EliminarVentaDetalleUseCase
import com.example.mobiledevsecops.domain.usecase.ObtenerDetallesResult
import com.example.mobiledevsecops.domain.usecase.ObtenerDetallesVentaUseCase
import com.example.mobiledevsecops.domain.usecase.ObtenerEstadosVentaResult
import com.example.mobiledevsecops.domain.usecase.ObtenerEstadosVentaUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val MIN_BUSQUEDA_LENGTH = 2
private const val DEBOUNCE_MS = 300L

data class VentaDetalleUiState(
    val ventaId: Int = 0,
    val strClaveVenta: String = "",
    val dteFechaHoraCompra: String = "",
    val strNombreCliente: String = "",
    val strEstadoActual: String = "",
    val idCliCliente: Int = 0,
    val idSegUsuario: Int = 0,
    val idVenCatEstado: Int = 0,
    val rowVersion: String = "",
    val estadosVenta: List<EstadoVenta> = emptyList(),
    val estadoSeleccionadoId: Int = 0,
    val productoSearchText: String = "",
    val productoResultados: List<ProductoAutocomplete> = emptyList(),
    val productoBuscando: Boolean = false,
    val productoSeleccionadoId: Int = 0,
    val productoSeleccionadoNombre: String = "",
    val intPiezaVenta: String = "",
    val detalles: List<VentaDetalle> = emptyList(),
    val isLoading: Boolean = false,
    val isGuardandoEstado: Boolean = false,
    val isAgregandoProducto: Boolean = false,
    val isEliminando: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val detalleAEliminar: VentaDetalle? = null,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class VentaDetalleEvent {
    data object NavigateBack : VentaDetalleEvent()
    data object SessionExpired : VentaDetalleEvent()
    data class ShowSnackbar(val message: String) : VentaDetalleEvent()
}

class VentaDetalleViewModel(
    private val actualizarEstadoVentaUseCase: ActualizarEstadoVentaUseCase,
    private val crearVentaDetalleUseCase: CrearVentaDetalleUseCase,
    private val eliminarVentaDetalleUseCase: EliminarVentaDetalleUseCase,
    private val buscarProductosAutocompleteUseCase: BuscarProductosAutocompleteUseCase,
    private val obtenerEstadosVentaUseCase: ObtenerEstadosVentaUseCase,
    private val obtenerDetallesVentaUseCase: ObtenerDetallesVentaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentaDetalleUiState())
    val uiState: StateFlow<VentaDetalleUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VentaDetalleEvent>()
    val events: SharedFlow<VentaDetalleEvent> = _events.asSharedFlow()

    private var productoSearchJob: Job? = null

    fun inicializar(
        ventaId: Int,
        strClaveVenta: String,
        dteFechaHoraCompra: String,
        strNombreCliente: String,
        strEstado: String,
        idCliCliente: Int,
        idSegUsuario: Int,
        idVenCatEstado: Int,
        rowVersion: String
    ) {
        _uiState.value = _uiState.value.copy(
            ventaId = ventaId,
            strClaveVenta = strClaveVenta,
            dteFechaHoraCompra = dteFechaHoraCompra,
            strNombreCliente = strNombreCliente,
            strEstadoActual = strEstado,
            idCliCliente = idCliCliente,
            idSegUsuario = idSegUsuario,
            idVenCatEstado = idVenCatEstado,
            estadoSeleccionadoId = idVenCatEstado,
            rowVersion = rowVersion
        )
        cargarEstadosVenta()
        cargarDetalles()
    }

    private fun cargarDetalles() {
        val ventaId = _uiState.value.ventaId
        if (ventaId <= 0) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = obtenerDetallesVentaUseCase(ventaId)) {
                is ObtenerDetallesResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        detalles = result.detalles
                    )
                }
                is ObtenerDetallesResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.mensaje
                    )
                }
                is ObtenerDetallesResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(VentaDetalleEvent.SessionExpired)
                }
            }
        }
    }

    private fun cargarEstadosVenta() {
        viewModelScope.launch {
            when (val result = obtenerEstadosVentaUseCase()) {
                is ObtenerEstadosVentaResult.Success -> {
                    _uiState.value = _uiState.value.copy(estadosVenta = result.estados)
                }
                is ObtenerEstadosVentaResult.SessionExpired -> {
                    _events.emit(VentaDetalleEvent.SessionExpired)
                }
            }
        }
    }

    fun onEstadoSeleccionado(estadoId: Int) {
        if (estadoId == _uiState.value.estadoSeleccionadoId) return
        _uiState.value = _uiState.value.copy(estadoSeleccionadoId = estadoId)
        actualizarEstado()
    }

    private fun actualizarEstado() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGuardandoEstado = true)
            when (val result = actualizarEstadoVentaUseCase(
                id = state.ventaId,
                idCliCliente = state.idCliCliente,
                idSegUsuario = state.idSegUsuario,
                idVenCatEstado = state.estadoSeleccionadoId,
                rowVersion = state.rowVersion
            )) {
                is ActualizarEstadoVentaResult.Success -> {
                    val nombreEstado = state.estadosVenta.firstOrNull { it.id == state.estadoSeleccionadoId }?.strValor ?: ""
                    _uiState.value = _uiState.value.copy(
                        isGuardandoEstado = false,
                        strEstadoActual = nombreEstado
                    )
                    _events.emit(VentaDetalleEvent.ShowSnackbar("Estado actualizado"))
                }
                is ActualizarEstadoVentaResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGuardandoEstado = false,
                        estadoSeleccionadoId = state.idVenCatEstado,
                        error = result.mensaje
                    )
                }
                is ActualizarEstadoVentaResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isGuardandoEstado = false)
                    _events.emit(VentaDetalleEvent.SessionExpired)
                }
            }
        }
    }

    fun onProductoSearchChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            productoSearchText = value,
            productoSeleccionadoId = 0,
            productoSeleccionadoNombre = ""
        )
        buscarProductosDebounced(value)
    }

    fun onProductoSeleccionado(producto: ProductoAutocomplete) {
        productoSearchJob?.cancel()
        val nombre = producto.strTextoAutocomplete.substringBefore(" |").trim()
        _uiState.value = _uiState.value.copy(
            productoSearchText = nombre,
            productoSeleccionadoId = producto.id,
            productoSeleccionadoNombre = nombre,
            productoResultados = emptyList(),
            productoBuscando = false
        )
    }

    fun onProductoLimpiado() {
        productoSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            productoSearchText = "",
            productoSeleccionadoId = 0,
            productoSeleccionadoNombre = "",
            productoResultados = emptyList(),
            productoBuscando = false
        )
    }

    fun onPiezasChanged(value: String) {
        _uiState.value = _uiState.value.copy(intPiezaVenta = value)
    }

    fun onAgregarProducto() {
        val state = _uiState.value
        if (state.productoSeleccionadoId <= 0) {
            _uiState.value = state.copy(error = "Debe seleccionar un producto")
            return
        }
        val piezas = state.intPiezaVenta.toIntOrNull()
        if (piezas == null || piezas <= 0) {
            _uiState.value = state.copy(error = "Debe ingresar un número válido de piezas")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAgregandoProducto = true, error = null)
            when (val result = crearVentaDetalleUseCase(
                idVenVenta = state.ventaId,
                idProProducto = state.productoSeleccionadoId,
                intPiezaVenta = piezas
            )) {
                is CrearVentaDetalleResult.Success -> {
                    val nuevosDetalles = state.detalles + result.detalle
                    _uiState.value = _uiState.value.copy(
                        isAgregandoProducto = false,
                        detalles = nuevosDetalles,
                        productoSearchText = "",
                        productoSeleccionadoId = 0,
                        productoSeleccionadoNombre = "",
                        intPiezaVenta = "",
                        successMessage = "Producto agregado"
                    )
                }
                is CrearVentaDetalleResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isAgregandoProducto = false,
                        error = result.mensaje
                    )
                }
                is CrearVentaDetalleResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAgregandoProducto = false,
                        error = result.mensaje
                    )
                }
                is CrearVentaDetalleResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isAgregandoProducto = false)
                    _events.emit(VentaDetalleEvent.SessionExpired)
                }
            }
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun onDismissSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun onCerrarClicked() {
        viewModelScope.launch {
            _events.emit(VentaDetalleEvent.NavigateBack)
        }
    }

    fun onEliminarClicked(detalle: VentaDetalle) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            detalleAEliminar = detalle
        )
    }

    fun onConfirmarEliminar() {
        val detalle = _uiState.value.detalleAEliminar ?: return
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            isEliminando = true,
            detalleAEliminar = null
        )

        viewModelScope.launch {
            when (val result = eliminarVentaDetalleUseCase(detalle.id, detalle.rowVersion)) {
                is EliminarVentaDetalleResult.Success -> {
                    val nuevosDetalles = _uiState.value.detalles.filter { it.id != detalle.id }
                    _uiState.value = _uiState.value.copy(
                        isEliminando = false,
                        detalles = nuevosDetalles,
                        successMessage = "Producto eliminado"
                    )
                }
                is EliminarVentaDetalleResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isEliminando = false,
                        error = result.mensaje
                    )
                }
                is EliminarVentaDetalleResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isEliminando = false)
                    _events.emit(VentaDetalleEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarEliminar() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            detalleAEliminar = null
        )
    }

    private fun buscarProductosDebounced(value: String) {
        productoSearchJob?.cancel()
        val texto = value.trim()

        if (texto.length < MIN_BUSQUEDA_LENGTH) {
            _uiState.value = _uiState.value.copy(
                productoResultados = emptyList(),
                productoBuscando = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(productoBuscando = true)

        productoSearchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            when (val result = buscarProductosAutocompleteUseCase(texto)) {
                is BuscarProductosAutocompleteResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        productoResultados = result.productos,
                        productoBuscando = false
                    )
                }
                is BuscarProductosAutocompleteResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(
                        productoResultados = emptyList(),
                        productoBuscando = false
                    )
                    _events.emit(VentaDetalleEvent.SessionExpired)
                }
            }
        }
    }
}
