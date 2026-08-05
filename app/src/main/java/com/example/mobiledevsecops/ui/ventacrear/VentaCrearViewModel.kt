package com.example.mobiledevsecops.ui.ventacrear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.model.Cliente
import com.example.mobiledevsecops.domain.model.Usuario
import com.example.mobiledevsecops.domain.usecase.BuscarClientesResult
import com.example.mobiledevsecops.domain.usecase.BuscarClientesUseCase
import com.example.mobiledevsecops.domain.usecase.BuscarUsuariosResult
import com.example.mobiledevsecops.domain.usecase.BuscarUsuariosUseCase
import com.example.mobiledevsecops.domain.usecase.CrearVentaResult
import com.example.mobiledevsecops.domain.usecase.CrearVentaUseCase
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

data class VentaCrearUiState(
    val clienteSearchText: String = "",
    val clienteResultados: List<Cliente> = emptyList(),
    val clienteBuscando: Boolean = false,
    val clienteSeleccionadoId: Int = 0,
    val clienteError: String? = null,
    val usuarioSearchText: String = "",
    val usuarioResultados: List<Usuario> = emptyList(),
    val usuarioBuscando: Boolean = false,
    val usuarioSeleccionadoId: Int = 0,
    val usuarioError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class VentaCrearEvent {
    data object NavigateBack : VentaCrearEvent()
    data object VentaCreada : VentaCrearEvent()
    data object Error : VentaCrearEvent()
    data object SessionExpired : VentaCrearEvent()
    data class ShowSnackbar(val message: String) : VentaCrearEvent()
}

class VentaCrearViewModel(
    private val crearVentaUseCase: CrearVentaUseCase,
    private val buscarClientesUseCase: BuscarClientesUseCase,
    private val buscarUsuariosUseCase: BuscarUsuariosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentaCrearUiState())
    val uiState: StateFlow<VentaCrearUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VentaCrearEvent>()
    val events: SharedFlow<VentaCrearEvent> = _events.asSharedFlow()

    private var clienteSearchJob: Job? = null
    private var usuarioSearchJob: Job? = null

    fun onClienteSearchChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            clienteSearchText = value,
            clienteSeleccionadoId = 0,
            clienteError = null
        )
        buscarClientesDebounced(value)
    }

    fun onClienteSeleccionado(cliente: Cliente) {
        clienteSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            clienteSearchText = cliente.strNombreCliente,
            clienteSeleccionadoId = cliente.id,
            clienteResultados = emptyList(),
            clienteBuscando = false,
            clienteError = null
        )
    }

    fun onClienteLimpiado() {
        clienteSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            clienteSearchText = "",
            clienteSeleccionadoId = 0,
            clienteResultados = emptyList(),
            clienteBuscando = false,
            clienteError = null
        )
    }

    fun onUsuarioSearchChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            usuarioSearchText = value,
            usuarioSeleccionadoId = 0,
            usuarioError = null
        )
        buscarUsuariosDebounced(value)
    }

    fun onUsuarioSeleccionado(usuario: Usuario) {
        usuarioSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            usuarioSearchText = usuario.strNombre,
            usuarioSeleccionadoId = usuario.id,
            usuarioResultados = emptyList(),
            usuarioBuscando = false,
            usuarioError = null
        )
    }

    fun onUsuarioLimpiado() {
        usuarioSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            usuarioSearchText = "",
            usuarioSeleccionadoId = 0,
            usuarioResultados = emptyList(),
            usuarioBuscando = false,
            usuarioError = null
        )
    }

    fun onGuardarClicked() {
        val state = _uiState.value
        val validationErrors = crearVentaUseCase.validar(
            state.clienteSeleccionadoId,
            state.usuarioSeleccionadoId
        )

        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                clienteError = validationErrors["idCliCliente"],
                usuarioError = validationErrors["idSegUsuario"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = crearVentaUseCase(
                state.clienteSeleccionadoId,
                state.usuarioSeleccionadoId
            )) {
                is CrearVentaResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(VentaCrearEvent.VentaCreada)
                }
                is CrearVentaResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        clienteError = result.errores["idCliCliente"],
                        usuarioError = result.errores["idSegUsuario"]
                    )
                }
                is CrearVentaResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(VentaCrearEvent.Error)
                }
                is CrearVentaResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(VentaCrearEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(VentaCrearEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun buscarClientesDebounced(value: String) {
        clienteSearchJob?.cancel()
        val texto = value.trim()

        if (texto.length < MIN_BUSQUEDA_LENGTH) {
            _uiState.value = _uiState.value.copy(
                clienteResultados = emptyList(),
                clienteBuscando = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(clienteBuscando = true)

        clienteSearchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            when (val result = buscarClientesUseCase(texto, 1)) {
                is BuscarClientesResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        clienteResultados = result.page.items,
                        clienteBuscando = false
                    )
                }
                is BuscarClientesResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(
                        clienteResultados = emptyList(),
                        clienteBuscando = false
                    )
                    _events.emit(VentaCrearEvent.SessionExpired)
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        clienteResultados = emptyList(),
                        clienteBuscando = false
                    )
                }
            }
        }
    }

    private fun buscarUsuariosDebounced(value: String) {
        usuarioSearchJob?.cancel()
        val texto = value.trim()

        if (texto.length < MIN_BUSQUEDA_LENGTH) {
            _uiState.value = _uiState.value.copy(
                usuarioResultados = emptyList(),
                usuarioBuscando = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(usuarioBuscando = true)

        usuarioSearchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            when (val result = buscarUsuariosUseCase(texto, 1)) {
                is BuscarUsuariosResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        usuarioResultados = result.page.items,
                        usuarioBuscando = false
                    )
                }
                is BuscarUsuariosResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(
                        usuarioResultados = emptyList(),
                        usuarioBuscando = false
                    )
                    _events.emit(VentaCrearEvent.SessionExpired)
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        usuarioResultados = emptyList(),
                        usuarioBuscando = false
                    )
                }
            }
        }
    }
}