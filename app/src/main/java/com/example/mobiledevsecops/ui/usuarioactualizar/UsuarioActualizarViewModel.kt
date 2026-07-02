package com.example.mobiledevsecops.ui.usuarioactualizar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.usecase.ActualizarUsuarioResult
import com.example.mobiledevsecops.domain.usecase.ActualizarUsuarioUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsuarioActualizarUiState(
    val id: Int = 0,
    val nombre: String = "",
    val pwd: String = "",
    val correo: String = "",
    val rowVersion: String = "",
    val nombreError: String? = null,
    val pwdError: String? = null,
    val correoError: String? = null,
    val rowVersionError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UsuarioActualizarEvent {
    data object NavigateBack : UsuarioActualizarEvent()
    data object UsuarioActualizado : UsuarioActualizarEvent()
    data object Error : UsuarioActualizarEvent()
    data object SessionExpired : UsuarioActualizarEvent()
}

class UsuarioActualizarViewModel(
    private val actualizarUsuarioUseCase: ActualizarUsuarioUseCase,
    id: Int,
    nombre: String,
    correo: String,
    rowVersion: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UsuarioActualizarUiState(
            id = id,
            nombre = nombre,
            correo = correo,
            rowVersion = rowVersion
        )
    )
    val uiState: StateFlow<UsuarioActualizarUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UsuarioActualizarEvent>()
    val events: SharedFlow<UsuarioActualizarEvent> = _events.asSharedFlow()

    fun onNombreChanged(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, nombreError = null)
    }

    fun onPwdChanged(value: String) {
        _uiState.value = _uiState.value.copy(pwd = value, pwdError = null)
    }

    fun onCorreoChanged(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, correoError = null)
    }

    fun onActualizarClicked() {
        val state = _uiState.value
        val validationErrors = actualizarUsuarioUseCase.validar(
            state.id, state.nombre, state.pwd, state.correo, state.rowVersion
        )

        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                nombreError = validationErrors["strNombre"],
                pwdError = validationErrors["strPWD"],
                correoError = validationErrors["strCorreoElectronico"],
                rowVersionError = validationErrors["rowVersion"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = actualizarUsuarioUseCase(
                state.id, state.nombre, state.pwd, state.correo, state.rowVersion
            )) {
                is ActualizarUsuarioResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, pwd = "")
                    _events.emit(UsuarioActualizarEvent.UsuarioActualizado)
                }
                is ActualizarUsuarioResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombreError = result.errores["strNombre"],
                        pwdError = result.errores["strPWD"],
                        correoError = result.errores["strCorreoElectronico"],
                        rowVersionError = result.errores["rowVersion"]
                    )
                }
                is ActualizarUsuarioResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.mensaje)
                    _events.emit(UsuarioActualizarEvent.Error)
                }
                is ActualizarUsuarioResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(UsuarioActualizarEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(UsuarioActualizarEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
