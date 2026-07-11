package com.example.mobiledevsecops.ui.empleadoactualizar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.EmpCatTipoEmpleado
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository
import com.example.mobiledevsecops.domain.usecase.ActualizarEmpleadoResult
import com.example.mobiledevsecops.domain.usecase.ActualizarEmpleadoUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmpleadoActualizarParams(
    val id: Int,
    val rowVersion: String
)

data class EmpleadoActualizarUiState(
    val id: Int = 0,
    val nombre: String = "",
    val aPaterno: String = "",
    val aMaterno: String = "",
    val curp: String = "",
    val idTipoEmpleado: Int? = null,
    val rowVersion: String = "",
    val tiposEmpleado: List<EmpCatTipoEmpleado> = emptyList(),
    val nombreError: String? = null,
    val aPaternoError: String? = null,
    val aMaternoError: String? = null,
    val curpError: String? = null,
    val rowVersionError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loadError: String? = null
)

sealed class EmpleadoActualizarEvent {
    data object NavigateBack : EmpleadoActualizarEvent()
    data object EmpleadoActualizado : EmpleadoActualizarEvent()
    data object Error : EmpleadoActualizarEvent()
    data object SessionExpired : EmpleadoActualizarEvent()
}

class EmpleadoActualizarViewModel(
    private val actualizarEmpleadoUseCase: ActualizarEmpleadoUseCase,
    private val empleadoRepository: EmpleadoRepository,
    params: EmpleadoActualizarParams
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EmpleadoActualizarUiState(id = params.id, rowVersion = params.rowVersion)
    )
    val uiState: StateFlow<EmpleadoActualizarUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EmpleadoActualizarEvent>()
    val events: SharedFlow<EmpleadoActualizarEvent> = _events.asSharedFlow()

    init {
        loadEmpleado(params.id)
    }

    private fun loadEmpleado(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val empleado = empleadoRepository.getEmpleadoById(id)
                val tipos = empleadoRepository.getTiposEmpleado()
                _uiState.value = EmpleadoActualizarUiState(
                    id = empleado.id,
                    nombre = empleado.strNombre,
                    aPaterno = empleado.strAPaterno ?: "",
                    aMaterno = empleado.strAMaterno ?: "",
                    curp = empleado.strCURP ?: "",
                    idTipoEmpleado = empleado.idEmpCatTipoEmpleado,
                    rowVersion = empleado.rowVersion.ifBlank { _uiState.value.rowVersion },
                    tiposEmpleado = tipos,
                    isLoading = false
                )
            } catch (e: SessionExpiredException) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(EmpleadoActualizarEvent.SessionExpired)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, loadError = "Error al cargar datos del empleado")
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

    fun onActualizarClicked() {
        val state = _uiState.value
        val validationErrors = actualizarEmpleadoUseCase.validar(
            state.id,
            state.nombre,
            state.aPaterno.ifBlank { null },
            state.aMaterno.ifBlank { null },
            state.curp.ifBlank { null },
            state.idTipoEmpleado,
            state.rowVersion
        )

        if (validationErrors.isNotEmpty()) {
            val rowVerror = validationErrors["rowVersion"]
            val idError = validationErrors["id"]
            _uiState.value = _uiState.value.copy(
                nombreError = validationErrors["strNombre"],
                aPaternoError = validationErrors["strAPaterno"],
                aMaternoError = validationErrors["strAMaterno"],
                curpError = validationErrors["strCURP"],
                rowVersionError = rowVerror,
                error = rowVerror ?: idError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = actualizarEmpleadoUseCase(
                state.id,
                state.nombre,
                state.aPaterno.ifBlank { null },
                state.aMaterno.ifBlank { null },
                state.curp.ifBlank { null },
                state.idTipoEmpleado,
                state.rowVersion
            )) {
                is ActualizarEmpleadoResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(EmpleadoActualizarEvent.EmpleadoActualizado)
                }
                is ActualizarEmpleadoResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombreError = result.errores["strNombre"],
                        aPaternoError = result.errores["strAPaterno"],
                        aMaternoError = result.errores["strAMaterno"],
                        curpError = result.errores["strCURP"],
                        rowVersionError = result.errores["rowVersion"]
                    )
                }
                is ActualizarEmpleadoResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.mensaje)
                    _events.emit(EmpleadoActualizarEvent.Error)
                }
                is ActualizarEmpleadoResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(EmpleadoActualizarEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(EmpleadoActualizarEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null, loadError = null)
    }
}
