package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.repository.VentaRepository

sealed class EliminarVentaDetalleResult {
    data object Success : EliminarVentaDetalleResult()
    data class Error(val mensaje: String) : EliminarVentaDetalleResult()
    data object SessionExpired : EliminarVentaDetalleResult()
}

class EliminarVentaDetalleUseCase(
    private val ventaRepository: VentaRepository
) {
    suspend operator fun invoke(id: Int, rowVersion: String): EliminarVentaDetalleResult {
        if (id <= 0) {
            return EliminarVentaDetalleResult.Error("Detalle inválido")
        }

        return try {
            ventaRepository.eliminarVentaDetalle(id, rowVersion)
            EliminarVentaDetalleResult.Success
        } catch (e: SessionExpiredException) {
            EliminarVentaDetalleResult.SessionExpired
        } catch (e: ConflictException) {
            EliminarVentaDetalleResult.Error("El registro fue modificado por otro usuario")
        } catch (e: Exception) {
            EliminarVentaDetalleResult.Error("Error al eliminar el detalle de venta")
        }
    }
}
