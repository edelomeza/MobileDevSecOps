package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.ProductoRepository

private const val MAX_NOMBRE_LENGTH = 50
private const val MAX_URL_LENGTH = 300
private const val MAX_DESCRIPCION_LENGTH = 250

sealed class ActualizarProductoResult {
    data object Success : ActualizarProductoResult()
    data class ValidationError(val errores: Map<String, String>) : ActualizarProductoResult()
    data class Error(val mensaje: String) : ActualizarProductoResult()
    data object SessionExpired : ActualizarProductoResult()
}

class ActualizarProductoUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(
        id: Int,
        strNombreProducto: String,
        strURLImagen: String?,
        strDescripcion: String?,
        intNumeroExistencia: Int,
        decPrecio: Double,
        rowVersion: String
    ): ActualizarProductoResult {
        val validationErrors = validar(
            id, strNombreProducto, strURLImagen, strDescripcion,
            intNumeroExistencia, decPrecio, rowVersion
        )
        if (validationErrors.isNotEmpty()) {
            return ActualizarProductoResult.ValidationError(validationErrors)
        }

        return try {
            productoRepository.actualizarProducto(
                id,
                strNombreProducto.trim(),
                strURLImagen?.trim()?.takeIf { it.isNotEmpty() },
                strDescripcion?.trim()?.takeIf { it.isNotEmpty() },
                intNumeroExistencia,
                decPrecio,
                rowVersion
            )
            ActualizarProductoResult.Success
        } catch (e: SessionExpiredException) {
            ActualizarProductoResult.SessionExpired
        } catch (e: ConflictException) {
            ActualizarProductoResult.Error("El registro ha sido modificado por otro usuario")
        } catch (e: Exception) {
            ActualizarProductoResult.Error("Error al actualizar producto")
        }
    }

    fun validar(
        id: Int,
        strNombreProducto: String,
        strURLImagen: String?,
        strDescripcion: String?,
        intNumeroExistencia: Int,
        decPrecio: Double,
        rowVersion: String
    ): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (id <= 0) {
            errores["id"] = "ID de producto inválido"
        }

        if (strNombreProducto.isBlank()) {
            errores["strNombreProducto"] = "El nombre del producto es obligatorio"
        } else if (strNombreProducto.trim().length > MAX_NOMBRE_LENGTH) {
            errores["strNombreProducto"] = "Máximo $MAX_NOMBRE_LENGTH caracteres"
        } else if (!Regex("^[\\p{L}0-9_ ]+$").matches(strNombreProducto.trim())) {
            errores["strNombreProducto"] = "Solo letras (incluye acentos/ñ), números y espacios"
        }

        if (strURLImagen != null && strURLImagen.trim().isNotEmpty()) {
            if (strURLImagen.trim().length > MAX_URL_LENGTH) {
                errores["strURLImagen"] = "Máximo $MAX_URL_LENGTH caracteres"
            } else if (!Regex("^https?://.+").matches(strURLImagen.trim())) {
                errores["strURLImagen"] = "Debe ser una URL válida (http:// o https://)"
            }
        }

        if (strDescripcion != null && strDescripcion.trim().length > MAX_DESCRIPCION_LENGTH) {
            errores["strDescripcion"] = "Máximo $MAX_DESCRIPCION_LENGTH caracteres"
        }

        if (intNumeroExistencia < 0) {
            errores["intNumeroExistencia"] = "La existencia no puede ser negativa"
        }

        if (decPrecio < 0) {
            errores["decPrecio"] = "El precio no puede ser negativo"
        }

        if (rowVersion.isBlank()) {
            errores["rowVersion"] = "Versión del registro inválida"
        }

        return errores
    }
}
