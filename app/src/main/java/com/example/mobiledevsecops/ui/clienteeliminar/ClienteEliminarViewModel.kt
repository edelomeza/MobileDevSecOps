package com.example.mobiledevsecops.ui.clienteeliminar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.usecase.EliminarClienteResult
import com.example.mobiledevsecops.domain.usecase.EliminarClienteUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClienteEliminarParams(
    val id: Int,
    val nombreCliente: String,
    val direccionCliente: String?,
    val correo: String,
    val telefono: String,
    val rowVersion: String
)

data class ClienteEliminarUiState(
    val id: Int = 0,
    val nombreCliente: String = "",
    val direccionCliente: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rowVersion: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val idError: String? = null,
    val rowVersionError: String? = null
)

sealed class ClienteEliminarEvent {
    data object NavigateBack : ClienteEliminarEvent()
    data object ClienteEliminado : ClienteEliminarEvent()
    data object Error : ClienteEliminarEvent()
    data object SessionExpired : ClienteEliminarEvent()
}

class ClienteEliminarViewModel(
    private val eliminarClienteUseCase: EliminarClienteUseCase,
    private val params: ClienteEliminarParams
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ClienteEliminarUiState(
            id = params.id,
            nombreCliente = params.nombreCliente,
            direccionCliente = params.direccionCliente ?: "",
            correo = params.correo,
            telefono = params.telefono,
            rowVersion = params.rowVersion
        )
    )
    val uiState: StateFlow<ClienteEliminarUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ClienteEliminarEvent>()
    val events: SharedFlow<ClienteEliminarEvent> = _events.asSharedFlow()

    fun onEliminarClicked() {
        val state = _uiState.value
        val validationErrors = eliminarClienteUseCase.validar(state.id, state.rowVersion)

        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                idError = validationErrors["id"],
                rowVersionError = validationErrors["rowVersion"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = eliminarClienteUseCase(state.id, state.rowVersion)) {
                is EliminarClienteResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ClienteEliminarEvent.ClienteEliminado)
                }
                is EliminarClienteResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        idError = result.errores["id"],
                        rowVersionError = result.errores["rowVersion"]
                    )
                }
                is EliminarClienteResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.mensaje)
                    _events.emit(ClienteEliminarEvent.Error)
                }
                is EliminarClienteResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ClienteEliminarEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(ClienteEliminarEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
