package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.ClienteRepository

private const val MAX_NOMBRE_LENGTH = 100
private const val MAX_DIRECCION_LENGTH = 200
private const val MAX_CORREO_LENGTH = 100
private const val TELEFONO_LENGTH = 10

sealed class CrearClienteResult {
    data object Success : CrearClienteResult()
    data class ValidationError(val errores: Map<String, String>) : CrearClienteResult()
    data class Error(val mensaje: String) : CrearClienteResult()
    data object SessionExpired : CrearClienteResult()
}

class CrearClienteUseCase(
    private val clienteRepository: ClienteRepository
) {
    suspend operator fun invoke(
        strNombreCliente: String,
        strDireccionCliente: String?,
        strCorreoElectronico: String,
        strNumeroTelefono: String
    ): CrearClienteResult {
        val validationErrors = validar(strNombreCliente, strDireccionCliente, strCorreoElectronico, strNumeroTelefono)
        if (validationErrors.isNotEmpty()) {
            return CrearClienteResult.ValidationError(validationErrors)
        }

        return try {
            clienteRepository.crearCliente(
                strNombreCliente.trim(),
                strDireccionCliente?.trim()?.takeIf { it.isNotEmpty() },
                strCorreoElectronico.trim(),
                strNumeroTelefono.trim()
            )
            CrearClienteResult.Success
        } catch (e: SessionExpiredException) {
            CrearClienteResult.SessionExpired
        } catch (e: ConflictException) {
            CrearClienteResult.Error("El registro ya existe o fue modificado por otro usuario")
        } catch (e: Exception) {
            CrearClienteResult.Error("Error al crear cliente")
        }
    }

    fun validar(
        strNombreCliente: String,
        strDireccionCliente: String?,
        strCorreoElectronico: String,
        strNumeroTelefono: String
    ): Map<String, String> {
        val errores = mutableMapOf<String, String>()

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

        return errores
    }
}
