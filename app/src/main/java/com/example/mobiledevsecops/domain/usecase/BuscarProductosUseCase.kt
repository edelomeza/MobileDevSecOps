package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.ProductoPage
import com.example.mobiledevsecops.domain.repository.ProductoRepository

private const val MIN_SEARCH_LENGTH = 2
private const val MAX_SEARCH_LENGTH = 100

sealed class BuscarProductosResult {
    data class Success(val page: ProductoPage) : BuscarProductosResult()
    data class ValidationError(val mensaje: String) : BuscarProductosResult()
    data class Error(val mensaje: String) : BuscarProductosResult()
    data object SessionExpired : BuscarProductosResult()
}

class BuscarProductosUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(texto: String, page: Int, pageSize: Int = 8): BuscarProductosResult {
        val trimmed = texto.trim()

        if (trimmed.length < MIN_SEARCH_LENGTH) {
            return BuscarProductosResult.ValidationError(
                "El texto de búsqueda debe tener al menos $MIN_SEARCH_LENGTH caracteres"
            )
        }

        if (trimmed.length > MAX_SEARCH_LENGTH) {
            return BuscarProductosResult.ValidationError(
                "El texto de búsqueda no puede exceder $MAX_SEARCH_LENGTH caracteres"
            )
        }

        return try {
            val result = productoRepository.buscarProductos(trimmed, page, pageSize)
            BuscarProductosResult.Success(result)
        } catch (e: SessionExpiredException) {
            BuscarProductosResult.SessionExpired
        } catch (e: Exception) {
            BuscarProductosResult.Error("Error al buscar productos")
        }
    }
}
