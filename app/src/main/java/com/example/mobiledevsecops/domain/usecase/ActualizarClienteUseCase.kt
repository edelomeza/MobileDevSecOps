package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.ClienteRepository

private const val MAX_NOMBRE_LENGTH = 100
private const val MAX_DIRECCION_LENGTH = 200
private const val MAX_CORREO_LENGTH = 100
private const val TELEFONO_LENGTH = 10

sealed class ActualizarClienteResult {
    data object Success : ActualizarClienteResult()
    data class ValidationError(val errores: Map<String, String>) : ActualizarClienteResult()
    data class Error(val mensaje: String) : ActualizarClienteResult()
    data object SessionExpired : ActualizarClienteResult()
}

class ActualizarClienteUseCase(
    private val clienteRepository: ClienteRepository
) {
    suspend operator fun invoke(
        id: Int,
        strNombreCliente: String,
        strDireccionCliente: String?,
        strCorreoElectronico: String,
        strNumeroTelefono: String,
        rowVersion: String
    ): ActualizarClienteResult {
        val validationErrors = validar(id, strNombreCliente, strDireccionCliente, strCorreoElectronico, strNumeroTelefono, rowVersion)
        if (validationErrors.isNotEmpty()) {
            return ActualizarClienteResult.ValidationError(validationErrors)
        }

        return try {
            clienteRepository.actualizarCliente(
                id,
                strNombreCliente.trim(),
                strDireccionCliente?.trim()?.takeIf { it.isNotEmpty() },
                strCorreoElectronico.trim(),
                strNumeroTelefono.trim(),
                rowVersion
            )
            ActualizarClienteResult.Success
        } catch (e: SessionExpiredException) {
            ActualizarClienteResult.SessionExpired
        } catch (e: ConflictException) {
            ActualizarClienteResult.Error("El registro ha sido modificado por otro usuario")
        } catch (e: Exception) {
            ActualizarClienteResult.Error("Error al actualizar cliente")
        }
    }

    fun validar(
        id: Int,
        strNombreCliente: String,
        strDireccionCliente: String?,
        strCorreoElectronico: String,
        strNumeroTelefono: String,
        rowVersion: String
    ): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (id <= 0) {
            errores["id"] = "ID de cliente inválido"
        }

        if (strNombreCliente.isBlank()) {
            errores["strNombreCliente"] = "El nombre del cliente es obligatorio"
        } else if (strNombreCliente.trim().length > MAX_NOMBRE_LENGTH) {
            errores["strNombreCliente"] = "Máximo $MAX_NOMBRE_LENGTH caracteres"
        } else if (!Regex("^[\\p{L}0-9_ ]+$").matches(strNombreCliente.trim())) {
            errores["strNombreCliente"] = "Solo letras (incluye acentos/ñ), números y espacios"
        }

        if (strDireccionCliente != null && strDireccionCliente.trim().length > MAX_DIRECCION_LENGTH) {
            errores["strDireccionCliente"] = "Máximo $MAX_DIRECCION_LENGTH caracteres"
        }

        if (strCorreoElectronico.isBlank()) {
            errores["strCorreoElectronico"] = "El correo es obligatorio"
        } else if (strCorreoElectronico.trim().length > MAX_CORREO_LENGTH) {
            errores["strCorreoElectronico"] = "Máximo $MAX_CORREO_LENGTH caracteres"
        } else if (!Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(strCorreoElectronico.trim())) {
            errores["strCorreoElectronico"] = "Formato de correo inválido"
        }

        if (strNumeroTelefono.isBlank()) {
            errores["strNumeroTelefono"] = "El teléfono es obligatorio"
        } else if (!Regex("^[0-9]{$TELEFONO_LENGTH}$").matches(strNumeroTelefono.trim())) {
            errores["strNumeroTelefono"] = "Debe tener exactamente $TELEFONO_LENGTH dígitos"
        }

        if (rowVersion.isBlank()) {
            errores["rowVersion"] = "Versión del registro inválida"
        }

        return errores
    }
}
