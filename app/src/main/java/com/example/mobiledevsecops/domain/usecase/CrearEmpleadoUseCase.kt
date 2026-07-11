package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository

private const val MAX_NOMBRE_LENGTH = 50
private const val MAX_APATERNO_LENGTH = 50
private const val MAX_AMATERNO_LENGTH = 50
private val CURP_REGEX = "^[A-Z]{1}[AEIOUX]{1}[A-Z]{2}[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])[HM]{1}(AS|BC|BS|CC|CH|CL|CM|CS|DF|DG|GR|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE)[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9]{1}[0-9]{1}$".toRegex()

sealed class CrearEmpleadoResult {
    data object Success : CrearEmpleadoResult()
    data class ValidationError(val errores: Map<String, String>) : CrearEmpleadoResult()
    data class Error(val mensaje: String) : CrearEmpleadoResult()
    data object SessionExpired : CrearEmpleadoResult()
}

class CrearEmpleadoUseCase(
    private val empleadoRepository: EmpleadoRepository
) {
    suspend operator fun invoke(
        strNombre: String,
        strAPaterno: String?,
        strAMaterno: String?,
        strCURP: String?,
        idEmpCatTipoEmpleado: Int?
    ): CrearEmpleadoResult {
        val validationErrors = validar(strNombre, strAPaterno, strAMaterno, strCURP, idEmpCatTipoEmpleado)
        if (validationErrors.isNotEmpty()) {
            return CrearEmpleadoResult.ValidationError(validationErrors)
        }

        return try {
            empleadoRepository.crearEmpleado(
                strNombre.trim(),
                strAPaterno?.trim()?.ifBlank { null },
                strAMaterno?.trim()?.ifBlank { null },
                strCURP?.trim()?.ifBlank { null }?.uppercase(),
                idEmpCatTipoEmpleado
            )
            CrearEmpleadoResult.Success
        } catch (e: SessionExpiredException) {
            CrearEmpleadoResult.SessionExpired
        } catch (e: ConflictException) {
            CrearEmpleadoResult.Error("El registro ya existe o fue modificado por otro usuario")
        } catch (e: Exception) {
            CrearEmpleadoResult.Error("Error al crear empleado")
        }
    }

    fun validar(
        strNombre: String,
        strAPaterno: String?,
        strAMaterno: String?,
        strCURP: String?,
        idEmpCatTipoEmpleado: Int?
    ): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        val nombre = strNombre.trim()
        if (nombre.isBlank()) {
            errores["strNombre"] = "El nombre es obligatorio"
        } else if (nombre.length > MAX_NOMBRE_LENGTH) {
            errores["strNombre"] = "Máximo $MAX_NOMBRE_LENGTH caracteres"
        } else if (!Regex("^[a-zA-Z0-9_ ]+$").matches(nombre)) {
            errores["strNombre"] = "Solo letras, números, guion bajo y espacios"
        }

        val aPaterno = strAPaterno?.trim()
        if (!aPaterno.isNullOrBlank()) {
            if (aPaterno.length > MAX_APATERNO_LENGTH) {
                errores["strAPaterno"] = "Máximo $MAX_APATERNO_LENGTH caracteres"
            } else if (!Regex("^[a-zA-ZáéíóúñÑ ]+$").matches(aPaterno)) {
                errores["strAPaterno"] = "Solo letras (incluye acentos/ñ) y espacios"
            }
        }

        val aMaterno = strAMaterno?.trim()
        if (!aMaterno.isNullOrBlank()) {
            if (aMaterno.length > MAX_AMATERNO_LENGTH) {
                errores["strAMaterno"] = "Máximo $MAX_AMATERNO_LENGTH caracteres"
            } else if (!Regex("^[a-zA-ZáéíóúñÑ ]+$").matches(aMaterno)) {
                errores["strAMaterno"] = "Solo letras (incluye acentos/ñ) y espacios"
            }
        }

        val curp = strCURP?.trim()
        if (!curp.isNullOrBlank()) {
            if (curp.length > 18) {
                errores["strCURP"] = "Máximo 18 caracteres"
            } else if (!CURP_REGEX.matches(curp.uppercase())) {
                errores["strCURP"] = "Formato de CURP inválido"
            }
        }

        return errores
    }
}
