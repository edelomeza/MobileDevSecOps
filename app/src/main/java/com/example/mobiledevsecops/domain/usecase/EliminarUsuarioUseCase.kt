package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.UsuarioRepository

sealed class EliminarUsuarioResult {
    data object Success : EliminarUsuarioResult()
    data class ValidationError(val errores: Map<String, String>) : EliminarUsuarioResult()
    data class Error(val mensaje: String) : EliminarUsuarioResult()
    data object SessionExpired : EliminarUsuarioResult()
}

class EliminarUsuarioUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(id: Int, rowVersion: String): EliminarUsuarioResult {
        val validationErrors = validar(id, rowVersion)
        if (validationErrors.isNotEmpty()) {
            return EliminarUsuarioResult.ValidationError(validationErrors)
        }
        return try {
            usuarioRepository.eliminarUsuario(id, rowVersion)
            EliminarUsuarioResult.Success
        } catch (e: SessionExpiredException) {
            EliminarUsuarioResult.SessionExpired
        } catch (e: ConflictException) {
            EliminarUsuarioResult.Error("El registro ha sido modificado por otro usuario")
        } catch (e: Exception) {
            EliminarUsuarioResult.Error("Error al eliminar usuario")
        }
    }

    fun validar(id: Int, rowVersion: String): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        if (id <= 0) errores["id"] = "ID de usuario inválido"
        if (rowVersion.isBlank()) errores["rowVersion"] = "Versión del registro inválida"
        return errores
    }
}
