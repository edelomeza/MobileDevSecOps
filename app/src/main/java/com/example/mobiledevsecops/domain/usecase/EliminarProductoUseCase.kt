package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.ProductoRepository

sealed class EliminarProductoResult {
    data object Success : EliminarProductoResult()
    data class ValidationError(val errores: Map<String, String>) : EliminarProductoResult()
    data class Error(val mensaje: String) : EliminarProductoResult()
    data object SessionExpired : EliminarProductoResult()
}

class EliminarProductoUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(id: Int, rowVersion: String): EliminarProductoResult {
        val validationErrors = validar(id, rowVersion)
        if (validationErrors.isNotEmpty()) {
            return EliminarProductoResult.ValidationError(validationErrors)
        }
        return try {
            productoRepository.eliminarProducto(id, rowVersion)
            EliminarProductoResult.Success
        } catch (e: SessionExpiredException) {
            EliminarProductoResult.SessionExpired
        } catch (e: ConflictException) {
            EliminarProductoResult.Error("El registro ha sido modificado por otro usuario")
        } catch (e: Exception) {
            EliminarProductoResult.Error("Error al eliminar producto")
        }
    }

    fun validar(id: Int, rowVersion: String): Map<String, String> {
        val errores = mutableMapOf<String, String>()
        if (id <= 0) errores["id"] = "ID de producto inválido"
        if (rowVersion.isBlank()) errores["rowVersion"] = "Versión del registro inválida"
        return errores
    }
}
