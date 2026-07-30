package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.ClientePage
import com.example.mobiledevsecops.domain.repository.ClienteRepository

private const val MIN_SEARCH_LENGTH = 2
private const val MAX_SEARCH_LENGTH = 100

sealed class BuscarClientesResult {
    data class Success(val page: ClientePage) : BuscarClientesResult()
    data class ValidationError(val mensaje: String) : BuscarClientesResult()
    data class Error(val mensaje: String) : BuscarClientesResult()
    data object SessionExpired : BuscarClientesResult()
}

class BuscarClientesUseCase(
    private val clienteRepository: ClienteRepository
) {
    suspend operator fun invoke(texto: String, page: Int, pageSize: Int = 8): BuscarClientesResult {
        val trimmed = texto.trim()

        if (trimmed.length < MIN_SEARCH_LENGTH) {
            return BuscarClientesResult.ValidationError(
                "El texto de búsqueda debe tener al menos $MIN_SEARCH_LENGTH caracteres"
            )
        }

        if (trimmed.length > MAX_SEARCH_LENGTH) {
            return BuscarClientesResult.ValidationError(
                "El texto de búsqueda no puede exceder $MAX_SEARCH_LENGTH caracteres"
            )
        }

        return try {
            val result = clienteRepository.buscarClientes(trimmed, page, pageSize)
            BuscarClientesResult.Success(result)
        } catch (e: SessionExpiredException) {
            BuscarClientesResult.SessionExpired
        } catch (e: Exception) {
            BuscarClientesResult.Error("Error al buscar clientes")
        }
    }
}
