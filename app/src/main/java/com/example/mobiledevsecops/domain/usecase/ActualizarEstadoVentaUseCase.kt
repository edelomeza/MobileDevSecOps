package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.VentaRepository

sealed class ActualizarEstadoVentaResult {
    data object Success : ActualizarEstadoVentaResult()
    data class Error(val mensaje: String) : ActualizarEstadoVentaResult()
    data object SessionExpired : ActualizarEstadoVentaResult()
}

class ActualizarEstadoVentaUseCase(
    private val ventaRepository: VentaRepository
) {
    suspend operator fun invoke(
        id: Int,
        idCliCliente: Int,
        idSegUsuario: Int,
        idVenCatEstado: Int,
        rowVersion: String?
    ): ActualizarEstadoVentaResult {
        if (idVenCatEstado <= 0) {
            return ActualizarEstadoVentaResult.Error("Debe seleccionar un estado")
        }

        return try {
            ventaRepository.actualizarEstadoVenta(id, idCliCliente, idSegUsuario, idVenCatEstado, rowVersion)
            ActualizarEstadoVentaResult.Success
        } catch (e: SessionExpiredException) {
            ActualizarEstadoVentaResult.SessionExpired
        } catch (e: ConflictException) {
            ActualizarEstadoVentaResult.Error("El registro fue modificado por otro usuario")
        } catch (e: Exception) {
            ActualizarEstadoVentaResult.Error("Error al actualizar el estado")
        }
    }
}
