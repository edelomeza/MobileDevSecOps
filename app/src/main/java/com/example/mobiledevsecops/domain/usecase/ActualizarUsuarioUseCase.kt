package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.UsuarioRepository

private const val MAX_NOMBRE_LENGTH = 50
private const val PWD_MIN_LENGTH = 8
private const val PWD_MAX_LENGTH = 128
private const val MAX_CORREO_LENGTH = 50

sealed class ActualizarUsuarioResult {
    data object Success : ActualizarUsuarioResult()
    data class ValidationError(val errores: Map<String, String>) : ActualizarUsuarioResult()
    data class Error(val mensaje: String) : ActualizarUsuarioResult()
    data object SessionExpired : ActualizarUsuarioResult()
}

class ActualizarUsuarioUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(
        id: Int,
        strNombre: String,
        strPWD: String,
        strCorreoElectronico: String,
        rowVersion: String
    ): ActualizarUsuarioResult {
        val validationErrors = validar(id, strNombre, strPWD, strCorreoElectronico, rowVersion)
        if (validationErrors.isNotEmpty()) {
            return ActualizarUsuarioResult.ValidationError(validationErrors)
        }

        return try {
            usuarioRepository.actualizarUsuario(id, strNombre, strPWD, strCorreoElectronico, rowVersion)
            ActualizarUsuarioResult.Success
        } catch (e: SessionExpiredException) {
            ActualizarUsuarioResult.SessionExpired
        } catch (e: ConflictException) {
            ActualizarUsuarioResult.Error("El registro ha sido modificado por otro usuario")
        } catch (e: Exception) {
            ActualizarUsuarioResult.Error("Error al actualizar usuario")
        }
    }

    fun validar(
        id: Int,
        strNombre: String,
        strPWD: String,
        strCorreoElectronico: String,
        rowVersion: String
    ): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (id <= 0) {
            errores["id"] = "ID de usuario inválido"
        }

        if (strNombre.isBlank()) {
            errores["strNombre"] = "El nombre es obligatorio"
        } else if (strNombre.length > MAX_NOMBRE_LENGTH) {
            errores["strNombre"] = "Máximo $MAX_NOMBRE_LENGTH caracteres"
        } else if (!Regex("^[\\p{L}0-9_ ]+$").matches(strNombre)) {
            errores["strNombre"] = "Solo letras (incluye acentos/ñ), números y espacios"
        }

        if (strPWD.isBlank()) {
            errores["strPWD"] = "La contraseña es obligatoria"
        } else {
            val pwdErrors = mutableListOf<String>()
            if (strPWD.length < PWD_MIN_LENGTH) pwdErrors.add("mínimo $PWD_MIN_LENGTH caracteres")
            if (!Regex(".*[A-Z].*").matches(strPWD)) pwdErrors.add("1 mayúscula")
            if (!Regex(".*[0-9].*").matches(strPWD)) pwdErrors.add("1 dígito")
            if (!Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*").matches(strPWD)) pwdErrors.add("1 caracter especial")
            if (strPWD.contains(" ")) pwdErrors.add("sin espacios")
            if (strPWD.length > PWD_MAX_LENGTH) pwdErrors.add("máximo $PWD_MAX_LENGTH caracteres")
            if (pwdErrors.isNotEmpty()) {
                errores["strPWD"] = "Debe tener: ${pwdErrors.joinToString(", ")}"
            }
        }

        if (strCorreoElectronico.isBlank()) {
            errores["strCorreoElectronico"] = "El correo es obligatorio"
        } else if (strCorreoElectronico.length > MAX_CORREO_LENGTH) {
            errores["strCorreoElectronico"] = "Máximo $MAX_CORREO_LENGTH caracteres"
        } else if (!Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(strCorreoElectronico)) {
            errores["strCorreoElectronico"] = "Formato de correo inválido"
        }

        if (rowVersion.isBlank()) {
            errores["rowVersion"] = "Versión del registro inválida"
        }

        return errores
    }
}
