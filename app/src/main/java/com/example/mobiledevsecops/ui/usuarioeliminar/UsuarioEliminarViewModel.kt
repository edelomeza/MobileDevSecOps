package com.example.mobiledevsecops.ui.usuarioeliminar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.usecase.EliminarUsuarioResult
import com.example.mobiledevsecops.domain.usecase.EliminarUsuarioUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsuarioEliminarUiState(
    val id: Int = 0,
    val nombre: String = "",
    val correo: String = "",
    val rowVersion: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val idError: String? = null,
    val rowVersionError: String? = null
)

sealed class UsuarioEliminarEvent {
    data object NavigateBack : UsuarioEliminarEvent()
    data object UsuarioEliminado : UsuarioEliminarEvent()
    data object Error : UsuarioEliminarEvent()
    data object SessionExpired : UsuarioEliminarEvent()
}

class UsuarioEliminarViewModel(
    private val eliminarUsuarioUseCase: EliminarUsuarioUseCase,
    id: Int,
    nombre: String,
    correo: String,
    rowVersion: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UsuarioEliminarUiState(
            id = id,
            nombre = nombre,
            correo = correo,
            rowVersion = rowVersion
        )
    )
    val uiState: StateFlow<UsuarioEliminarUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UsuarioEliminarEvent>()
    val events: SharedFlow<UsuarioEliminarEvent> = _events.asSharedFlow()

    fun onEliminarClicked() {
        val state = _uiState.value
        val validationErrors = eliminarUsuarioUseCase.validar(state.id, state.rowVersion)

        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                idError = validationErrors["id"],
                rowVersionError = validationErrors["rowVersion"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = eliminarUsuarioUseCase(state.id, state.rowVersion)) {
                is EliminarUsuarioResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(UsuarioEliminarEvent.UsuarioEliminado)
                }
                is EliminarUsuarioResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        idError = result.errores["id"],
                        rowVersionError = result.errores["rowVersion"]
                    )
                }
                is EliminarUsuarioResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.mensaje)
                    _events.emit(UsuarioEliminarEvent.Error)
                }
                is EliminarUsuarioResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(UsuarioEliminarEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(UsuarioEliminarEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
