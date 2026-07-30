package com.example.mobiledevsecops.ui.clienteactualizar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.usecase.ActualizarClienteResult
import com.example.mobiledevsecops.domain.usecase.ActualizarClienteUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClienteActualizarParams(
    val id: Int,
    val nombreCliente: String,
    val direccionCliente: String?,
    val correo: String,
    val telefono: String,
    val rowVersion: String
)

data class ClienteActualizarUiState(
    val id: Int = 0,
    val nombreCliente: String = "",
    val direccionCliente: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rowVersion: String = "",
    val nombreClienteError: String? = null,
    val direccionClienteError: String? = null,
    val correoError: String? = null,
    val telefonoError: String? = null,
    val rowVersionError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ClienteActualizarEvent {
    data object NavigateBack : ClienteActualizarEvent()
    data object ClienteActualizado : ClienteActualizarEvent()
    data object Error : ClienteActualizarEvent()
    data object SessionExpired : ClienteActualizarEvent()
}

class ClienteActualizarViewModel(
    private val actualizarClienteUseCase: ActualizarClienteUseCase,
    private val params: ClienteActualizarParams
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ClienteActualizarUiState(
            id = params.id,
            nombreCliente = params.nombreCliente,
            direccionCliente = params.direccionCliente ?: "",
            correo = params.correo,
            telefono = params.telefono,
            rowVersion = params.rowVersion
        )
    )
    val uiState: StateFlow<ClienteActualizarUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ClienteActualizarEvent>()
    val events: SharedFlow<ClienteActualizarEvent> = _events.asSharedFlow()

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

    fun onActualizarClicked() {
        val state = _uiState.value
        val validationErrors = actualizarClienteUseCase.validar(
            state.id,
            state.nombreCliente,
            state.direccionCliente.takeIf { it.isNotBlank() },
            state.correo,
            state.telefono,
            state.rowVersion
        )

        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                nombreClienteError = validationErrors["strNombreCliente"],
                direccionClienteError = validationErrors["strDireccionCliente"],
                correoError = validationErrors["strCorreoElectronico"],
                telefonoError = validationErrors["strNumeroTelefono"],
                rowVersionError = validationErrors["rowVersion"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = actualizarClienteUseCase(
                state.id,
                state.nombreCliente,
                state.direccionCliente.takeIf { it.isNotBlank() },
                state.correo,
                state.telefono,
                state.rowVersion
            )) {
                is ActualizarClienteResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ClienteActualizarEvent.ClienteActualizado)
                }
                is ActualizarClienteResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombreClienteError = result.errores["strNombreCliente"],
                        direccionClienteError = result.errores["strDireccionCliente"],
                        correoError = result.errores["strCorreoElectronico"],
                        telefonoError = result.errores["strNumeroTelefono"],
                        rowVersionError = result.errores["rowVersion"]
                    )
                }
                is ActualizarClienteResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.mensaje)
                    _events.emit(ClienteActualizarEvent.Error)
                }
                is ActualizarClienteResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ClienteActualizarEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(ClienteActualizarEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
