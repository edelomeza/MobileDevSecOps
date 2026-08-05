package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.VentaRepository
import com.example.mobiledevsecops.util.Fechas

sealed class CrearVentaResult {
    data object Success : CrearVentaResult()
    data class ValidationError(val errores: Map<String, String>) : CrearVentaResult()
    data class Error(val mensaje: String) : CrearVentaResult()
    data object SessionExpired : CrearVentaResult()
}

class CrearVentaUseCase(
    private val ventaRepository: VentaRepository
) {
    suspend operator fun invoke(
        idCliCliente: Int,
        idSegUsuario: Int
    ): CrearVentaResult {
        val validationErrors = validar(idCliCliente, idSegUsuario)
        if (validationErrors.isNotEmpty()) {
            return CrearVentaResult.ValidationError(validationErrors)
        }

        return try {
            ventaRepository.crearVenta(
                idCliCliente = idCliCliente,
                idSegUsuario = idSegUsuario,
                dteFechaHoraCompra = Fechas.ahoraIso(),
                strClaveVenta = Fechas.generarClaveVenta()
            )
            CrearVentaResult.Success
        } catch (e: SessionExpiredException) {
            CrearVentaResult.SessionExpired
        } catch (e: ConflictException) {
            CrearVentaResult.Error("El registro ya existe o fue modificado por otro usuario")
        } catch (e: Exception) {
            CrearVentaResult.Error("Error al crear la venta")
        }
    }

    fun validar(idCliCliente: Int, idSegUsuario: Int): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (idCliCliente <= 0) {
            errores["idCliCliente"] = "Debe seleccionar un cliente"
        }

        if (idSegUsuario <= 0) {
            errores["idSegUsuario"] = "Debe seleccionar un usuario"
        }

        return errores
    }
}