package com.example.mobiledevsecops.ui.usuariocrear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.usecase.CrearUsuarioResult
import com.example.mobiledevsecops.domain.usecase.CrearUsuarioUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsuarioCrearUiState(
    val nombre: String = "",
    val pwd: String = "",
    val correo: String = "",
    val nombreError: String? = null,
    val pwdError: String? = null,
    val correoError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UsuarioCrearEvent {
    data object NavigateBack : UsuarioCrearEvent()
    data object UsuarioCreado : UsuarioCrearEvent()
    data object Error : UsuarioCrearEvent()
    data object SessionExpired : UsuarioCrearEvent()
}

class UsuarioCrearViewModel(
    private val crearUsuarioUseCase: CrearUsuarioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioCrearUiState())
    val uiState: StateFlow<UsuarioCrearUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UsuarioCrearEvent>()
    val events: SharedFlow<UsuarioCrearEvent> = _events.asSharedFlow()

    fun onNombreChanged(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, nombreError = null)
    }

    fun onPwdChanged(value: String) {
        _uiState.value = _uiState.value.copy(pwd = value, pwdError = null)
    }

    fun onCorreoChanged(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, correoError = null)
    }

    fun onGuardarClicked() {
        val state = _uiState.value
        val validationErrors = crearUsuarioUseCase.validar(state.nombre, state.pwd, state.correo)

        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                nombreError = validationErrors["strNombre"],
                pwdError = validationErrors["strPWD"],
                correoError = validationErrors["strCorreoElectronico"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = crearUsuarioUseCase(state.nombre, state.pwd, state.correo)) {
                is CrearUsuarioResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, pwd = "")
                    _events.emit(UsuarioCrearEvent.UsuarioCreado)
                }
                is CrearUsuarioResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombreError = result.errores["strNombre"],
                        pwdError = result.errores["strPWD"],
                        correoError = result.errores["strCorreoElectronico"]
                    )
                }
                is CrearUsuarioResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(UsuarioCrearEvent.Error)
                }
                is CrearUsuarioResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(UsuarioCrearEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(UsuarioCrearEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
