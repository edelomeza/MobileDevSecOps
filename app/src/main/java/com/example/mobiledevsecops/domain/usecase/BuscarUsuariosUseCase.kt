package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.UsuarioPage
import com.example.mobiledevsecops.domain.repository.UsuarioRepository

private const val MIN_SEARCH_LENGTH = 2
private const val MAX_SEARCH_LENGTH = 100

sealed class BuscarUsuariosResult {
    data class Success(val page: UsuarioPage) : BuscarUsuariosResult()
    data class ValidationError(val mensaje: String) : BuscarUsuariosResult()
    data class Error(val mensaje: String) : BuscarUsuariosResult()
    data object SessionExpired : BuscarUsuariosResult()
}

class BuscarUsuariosUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(texto: String, page: Int, pageSize: Int = 8): BuscarUsuariosResult {
        val trimmed = texto.trim()

        if (trimmed.length < MIN_SEARCH_LENGTH) {
            return BuscarUsuariosResult.ValidationError(
                "El texto de búsqueda debe tener al menos $MIN_SEARCH_LENGTH caracteres"
            )
        }

        if (trimmed.length > MAX_SEARCH_LENGTH) {
            return BuscarUsuariosResult.ValidationError(
                "El texto de búsqueda no puede exceder $MAX_SEARCH_LENGTH caracteres"
            )
        }

        return try {
            val result = usuarioRepository.buscarUsuarios(trimmed, page, pageSize)
            BuscarUsuariosResult.Success(result)
        } catch (e: SessionExpiredException) {
            BuscarUsuariosResult.SessionExpired
        } catch (e: Exception) {
            BuscarUsuariosResult.Error("Error al buscar usuarios")
        }
    }
}
