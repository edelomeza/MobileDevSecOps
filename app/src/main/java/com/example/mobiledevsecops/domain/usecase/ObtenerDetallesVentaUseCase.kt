package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.VentaDetalle
import com.example.mobiledevsecops.domain.repository.VentaRepository

sealed class ObtenerDetallesResult {
    data class Success(val detalles: List<VentaDetalle>) : ObtenerDetallesResult()
    data class Error(val mensaje: String) : ObtenerDetallesResult()
    data object SessionExpired : ObtenerDetallesResult()
}

class ObtenerDetallesVentaUseCase(
    private val ventaRepository: VentaRepository
) {
    suspend operator fun invoke(ventaId: Int): ObtenerDetallesResult {
        if (ventaId <= 0) {
            return ObtenerDetallesResult.Error("Venta inválida")
        }

        return try {
            val detalles = ventaRepository.getDetallesByVentaId(ventaId)
            ObtenerDetallesResult.Success(detalles)
        } catch (e: SessionExpiredException) {
            ObtenerDetallesResult.SessionExpired
        } catch (e: Exception) {
            ObtenerDetallesResult.Error("Error al cargar los detalles de venta")
        }
    }
}
