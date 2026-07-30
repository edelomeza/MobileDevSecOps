package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.ClienteRepository

sealed class EliminarClienteResult {
    data object Success : EliminarClienteResult()
    data class ValidationError(val errores: Map<String, String>) : EliminarClienteResult()
    data class Error(val mensaje: String) : EliminarClienteResult()
    data object SessionExpired : EliminarClienteResult()
}

class EliminarClienteUseCase(
    private val clienteRepository: ClienteRepository
) {
    suspend operator fun invoke(id: Int, rowVersion: String): EliminarClienteResult {
        val validationErrors = validar(id, rowVersion)
        if (validationErrors.isNotEmpty()) {
            return EliminarClienteResult.ValidationError(validationErrors)
        }
        return try {
            clienteRepository.eliminarCliente(id, rowVersion)
            EliminarClienteResult.Success
        } catch (e: SessionExpiredException) {
            EliminarClienteResult.SessionExpired
        } catch (e: ConflictException) {
            EliminarClienteResult.Error("El registro ha sido modificado por otro usuario")
        } catch (e: Exception) {
            EliminarClienteResult.Error("Error al eliminar cliente")
        }
    }

    fun validar(id: Int, rowVersion: String): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        if (id <= 0) errores["id"] = "ID de cliente inválido"
        if (rowVersion.isBlank()) errores["rowVersion"] = "Versión del registro inválida"
        return errores
    }
}
