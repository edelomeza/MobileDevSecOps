package com.example.mobiledevsecops.ui.clientecrear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.usecase.CrearClienteResult
import com.example.mobiledevsecops.domain.usecase.CrearClienteUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClienteCrearUiState(
    val nombreCliente: String = "",
    val direccionCliente: String = "",
    val correo: String = "",
    val telefono: String = "",
    val nombreClienteError: String? = null,
    val direccionClienteError: String? = null,
    val correoError: String? = null,
    val telefonoError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ClienteCrearEvent {
    data object NavigateBack : ClienteCrearEvent()
    data object ClienteCreado : ClienteCrearEvent()
    data object Error : ClienteCrearEvent()
    data object SessionExpired : ClienteCrearEvent()
}

class ClienteCrearViewModel(
    private val crearClienteUseCase: CrearClienteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteCrearUiState())
    val uiState: StateFlow<ClienteCrearUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ClienteCrearEvent>()
    val events: SharedFlow<ClienteCrearEvent> = _events.asSharedFlow()

    fun onNombreClienteChanged(value: String) {
        _uiState.value = _uiState.value.copy(nombreCliente = value, nombreClienteError = null)
    }

    fun onDireccionClienteChanged(value: String) {
        _uiState.value = _uiState.value.copy(direccionCliente = value, direccionClienteError = null)
    }

    fun onCorreoChanged(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, correoError = null)
    }

    fun onTelefonoChanged(value: String) {
        _uiState.value = _uiState.value.copy(telefono = value, telefonoError = null)
    }

    fun onGuardarClicked() {
        val state = _uiState.value
        val validationErrors = crearClienteUseCase.validar(
            state.nombreCliente,
            state.direccionCliente.takeIf { it.isNotBlank() },
            state.correo,
            state.telefono
        )

        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                nombreClienteError = validationErrors["strNombreCliente"],
                direccionClienteError = validationErrors["strDireccionCliente"],
                correoError = validationErrors["strCorreoElectronico"],
                telefonoError = validationErrors["strNumeroTelefono"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = crearClienteUseCase(
                state.nombreCliente,
                state.direccionCliente.takeIf { it.isNotBlank() },
                state.correo,
                state.telefono
            )) {
                is CrearClienteResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ClienteCrearEvent.ClienteCreado)
                }
                is CrearClienteResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombreClienteError = result.errores["strNombreCliente"],
                        direccionClienteError = result.errores["strDireccionCliente"],
                        correoError = result.errores["strCorreoElectronico"],
                        telefonoError = result.errores["strNumeroTelefono"]
                    )
                }
                is CrearClienteResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ClienteCrearEvent.Error)
                }
                is CrearClienteResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ClienteCrearEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(ClienteCrearEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
