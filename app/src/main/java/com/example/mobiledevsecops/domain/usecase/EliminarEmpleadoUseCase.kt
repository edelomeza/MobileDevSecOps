package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository

sealed class EliminarEmpleadoResult {
    data object Success : EliminarEmpleadoResult()
    data class ValidationError(val errores: Map<String, String>) : EliminarEmpleadoResult()
    data class Error(val mensaje: String) : EliminarEmpleadoResult()
    data object SessionExpired : EliminarEmpleadoResult()
}

class EliminarEmpleadoUseCase(
    private val empleadoRepository: EmpleadoRepository
) {
    suspend operator fun invoke(
        id: Int,
        rowVersion: String
    ): EliminarEmpleadoResult {
        val validationErrors = validar(id, rowVersion)
        if (validationErrors.isNotEmpty()) {
            return EliminarEmpleadoResult.ValidationError(validationErrors)
        }

        return try {
            empleadoRepository.eliminarEmpleado(id, rowVersion)
            EliminarEmpleadoResult.Success
        } catch (e: SessionExpiredException) {
            EliminarEmpleadoResult.SessionExpired
        } catch (e: ConflictException) {
            EliminarEmpleadoResult.Error("El registro ha sido modificado por otro usuario")
        } catch (e: Exception) {
            EliminarEmpleadoResult.Error("Error al eliminar empleado")
        }
    }

    fun validar(id: Int, rowVersion: String): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (id <= 0) {
            errores["id"] = "ID de empleado inválido"
        }

        if (rowVersion.isBlank()) {
            errores["rowVersion"] = "Versión del registro inválida"
        }

        return errores
    }
}
