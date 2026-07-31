package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.ProductoRepository

private const val MAX_NOMBRE_LENGTH = 50
private const val MAX_URL_LENGTH = 300
private const val MAX_DESCRIPCION_LENGTH = 250

sealed class CrearProductoResult {
    data object Success : CrearProductoResult()
    data class ValidationError(val errores: Map<String, String>) : CrearProductoResult()
    data class Error(val mensaje: String) : CrearProductoResult()
    data object SessionExpired : CrearProductoResult()
}

class CrearProductoUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(
        strNombreProducto: String,
        strURLImagen: String?,
        strDescripcion: String?,
        intNumeroExistencia: Int,
        decPrecio: Double
    ): CrearProductoResult {
        val validationErrors = validar(
            strNombreProducto, strURLImagen, strDescripcion,
            intNumeroExistencia, decPrecio
        )
        if (validationErrors.isNotEmpty()) {
            return CrearProductoResult.ValidationError(validationErrors)
        }

        return try {
            productoRepository.crearProducto(
                strNombreProducto.trim(),
                strURLImagen?.trim()?.takeIf { it.isNotEmpty() },
                strDescripcion?.trim()?.takeIf { it.isNotEmpty() },
                intNumeroExistencia,
                decPrecio
            )
            CrearProductoResult.Success
        } catch (e: SessionExpiredException) {
            CrearProductoResult.SessionExpired
        } catch (e: ConflictException) {
            CrearProductoResult.Error("El registro ya existe o fue modificado por otro usuario")
        } catch (e: Exception) {
            CrearProductoResult.Error("Error al crear producto")
        }
    }

    fun validar(
        strNombreProducto: String,
        strURLImagen: String?,
        strDescripcion: String?,
        intNumeroExistencia: Int,
        decPrecio: Double
    ): Map<String, String> {
        val errores = mutableMapOf<String, String>()

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

        return errores
    }
}
