package com.example.mobiledevsecops.ui.empleadocrear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.domain.model.EmpCatTipoEmpleado
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository
import com.example.mobiledevsecops.domain.usecase.CrearEmpleadoResult
import com.example.mobiledevsecops.domain.usecase.CrearEmpleadoUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmpleadoCrearUiState(
    val nombre: String = "",
    val aPaterno: String = "",
    val aMaterno: String = "",
    val curp: String = "",
    val idTipoEmpleado: Int? = null,
    val tiposEmpleado: List<EmpCatTipoEmpleado> = emptyList(),
    val nombreError: String? = null,
    val aPaternoError: String? = null,
    val aMaternoError: String? = null,
    val curpError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class EmpleadoCrearEvent {
    data object NavigateBack : EmpleadoCrearEvent()
    data object EmpleadoCreado : EmpleadoCrearEvent()
    data object Error : EmpleadoCrearEvent()
    data object SessionExpired : EmpleadoCrearEvent()
}

class EmpleadoCrearViewModel(
    private val crearEmpleadoUseCase: CrearEmpleadoUseCase,
    private val empleadoRepository: EmpleadoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmpleadoCrearUiState())
    val uiState: StateFlow<EmpleadoCrearUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EmpleadoCrearEvent>()
    val events: SharedFlow<EmpleadoCrearEvent> = _events.asSharedFlow()

    init {
        loadTiposEmpleado()
    }

    private fun loadTiposEmpleado() {
        viewModelScope.launch {
            try {
                val tipos = empleadoRepository.getTiposEmpleado()
                _uiState.value = _uiState.value.copy(tiposEmpleado = tipos)
            } catch (_: Exception) {
            }
        }
    }

    fun onNombreChanged(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, nombreError = null)
    }

    fun onAPaternoChanged(value: String) {
        _uiState.value = _uiState.value.copy(aPaterno = value, aPaternoError = null)
    }

    fun onAMaternoChanged(value: String) {
        _uiState.value = _uiState.value.copy(aMaterno = value, aMaternoError = null)
    }

    fun onCurpChanged(value: String) {
        _uiState.value = _uiState.value.copy(curp = value, curpError = null)
    }

    fun onTipoEmpleadoChanged(id: Int?) {
        _uiState.value = _uiState.value.copy(idTipoEmpleado = id)
    }

    fun onGuardarClicked() {
        val state = _uiState.value
        val validationErrors = crearEmpleadoUseCase.validar(
            state.nombre, state.aPaterno.ifBlank { null },
            state.aMaterno.ifBlank { null },
            state.curp.ifBlank { null },
            state.idTipoEmpleado
        )

        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                nombreError = validationErrors["strNombre"],
                aPaternoError = validationErrors["strAPaterno"],
                aMaternoError = validationErrors["strAMaterno"],
                curpError = validationErrors["strCURP"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = crearEmpleadoUseCase(
                state.nombre,
                state.aPaterno.ifBlank { null },
                state.aMaterno.ifBlank { null },
                state.curp.ifBlank { null },
                state.idTipoEmpleado
            )) {
                is CrearEmpleadoResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(EmpleadoCrearEvent.EmpleadoCreado)
                }
                is CrearEmpleadoResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombreError = result.errores["strNombre"],
                        aPaternoError = result.errores["strAPaterno"],
                        aMaternoError = result.errores["strAMaterno"],
                        curpError = result.errores["strCURP"]
                    )
                }
                is CrearEmpleadoResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(EmpleadoCrearEvent.Error)
                }
                is CrearEmpleadoResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(EmpleadoCrearEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(EmpleadoCrearEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
