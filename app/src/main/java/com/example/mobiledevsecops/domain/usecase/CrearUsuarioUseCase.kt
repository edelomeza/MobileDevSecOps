package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.UsuarioRepository

private const val MAX_NOMBRE_LENGTH = 50
private const val PWD_MIN_LENGTH = 8
private const val PWD_MAX_LENGTH = 128
private const val MAX_CORREO_LENGTH = 50

sealed class CrearUsuarioResult {
    data object Success : CrearUsuarioResult()
    data class ValidationError(val errores: Map<String, String>) : CrearUsuarioResult()
    data class Error(val mensaje: String) : CrearUsuarioResult()
    data object SessionExpired : CrearUsuarioResult()
}

class CrearUsuarioUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(
        strNombre: String,
        strPWD: String,
        strCorreoElectronico: String
    ): CrearUsuarioResult {
        val validationErrors = validar(strNombre, strPWD, strCorreoElectronico)
        if (validationErrors.isNotEmpty()) {
            return CrearUsuarioResult.ValidationError(validationErrors)
        }

        return try {
            usuarioRepository.crearUsuario(strNombre, strPWD, strCorreoElectronico)
            CrearUsuarioResult.Success
        } catch (e: SessionExpiredException) {
            CrearUsuarioResult.SessionExpired
        } catch (e: ConflictException) {
            CrearUsuarioResult.Error("El registro ya existe o fue modificado por otro usuario")
        } catch (e: Exception) {
            CrearUsuarioResult.Error("Error al crear usuario")
        }
    }

    fun validar(
        strNombre: String,
        strPWD: String,
        strCorreoElectronico: String
    ): Map<String, String> {
        val errores = mutableMapOf<String, String>()

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

        return errores
    }
}
