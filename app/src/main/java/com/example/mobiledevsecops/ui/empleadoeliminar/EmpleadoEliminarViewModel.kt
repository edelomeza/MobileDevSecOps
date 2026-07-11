package com.example.mobiledevsecops.ui.empleadoeliminar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository
import com.example.mobiledevsecops.domain.usecase.EliminarEmpleadoResult
import com.example.mobiledevsecops.domain.usecase.EliminarEmpleadoUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmpleadoEliminarParams(
    val id: Int,
    val rowVersion: String
)

data class EmpleadoEliminarUiState(
    val id: Int = 0,
    val nombre: String = "",
    val aPaterno: String = "",
    val aMaterno: String = "",
    val curp: String = "",
    val idTipoEmpleado: Int? = null,
    val rowVersion: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val idError: String? = null,
    val rowVersionError: String? = null,
    val loadError: String? = null
)

sealed class EmpleadoEliminarEvent {
    data object NavigateBack : EmpleadoEliminarEvent()
    data object EmpleadoEliminado : EmpleadoEliminarEvent()
    data object Error : EmpleadoEliminarEvent()
    data object SessionExpired : EmpleadoEliminarEvent()
}

class EmpleadoEliminarViewModel(
    private val eliminarEmpleadoUseCase: EliminarEmpleadoUseCase,
    private val empleadoRepository: EmpleadoRepository,
    params: EmpleadoEliminarParams
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EmpleadoEliminarUiState(id = params.id, rowVersion = params.rowVersion)
    )
    val uiState: StateFlow<EmpleadoEliminarUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EmpleadoEliminarEvent>()
    val events: SharedFlow<EmpleadoEliminarEvent> = _events.asSharedFlow()

    init {
        loadEmpleado(params.id)
    }

    private fun loadEmpleado(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val empleado = empleadoRepository.getEmpleadoById(id)
                _uiState.value = EmpleadoEliminarUiState(
                    id = empleado.id,
                    nombre = empleado.strNombre,
                    aPaterno = empleado.strAPaterno ?: "",
                    aMaterno = empleado.strAMaterno ?: "",
                    curp = empleado.strCURP ?: "",
                    idTipoEmpleado = empleado.idEmpCatTipoEmpleado,
                    rowVersion = empleado.rowVersion.ifBlank { _uiState.value.rowVersion },
                    isLoading = false
                )
            } catch (e: SessionExpiredException) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(EmpleadoEliminarEvent.SessionExpired)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, loadError = "Error al cargar datos del empleado")
            }
        }
    }

    fun onEliminarClicked() {
        val state = _uiState.value
        val validationErrors = eliminarEmpleadoUseCase.validar(state.id, state.rowVersion)

        if (validationErrors.isNotEmpty()) {
            val rvError = validationErrors["rowVersion"]
            val iError = validationErrors["id"]
            _uiState.value = _uiState.value.copy(
                idError = iError,
                rowVersionError = rvError,
                error = rvError ?: iError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = eliminarEmpleadoUseCase(state.id, state.rowVersion)) {
                is EliminarEmpleadoResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(EmpleadoEliminarEvent.EmpleadoEliminado)
                }
                is EliminarEmpleadoResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        idError = result.errores["id"],
                        rowVersionError = result.errores["rowVersion"]
                    )
                }
                is EliminarEmpleadoResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.mensaje)
                    _events.emit(EmpleadoEliminarEvent.Error)
                }
                is EliminarEmpleadoResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(EmpleadoEliminarEvent.SessionExpired)
                }
            }
        }
    }

    fun onCancelarClicked() {
        viewModelScope.launch {
            _events.emit(EmpleadoEliminarEvent.NavigateBack)
        }
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(error = null, loadError = null)
    }
}
