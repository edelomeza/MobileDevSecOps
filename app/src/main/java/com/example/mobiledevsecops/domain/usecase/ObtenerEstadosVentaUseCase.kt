package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.EstadoVenta
import com.example.mobiledevsecops.domain.repository.VentaRepository

sealed class ObtenerEstadosVentaResult {
    data class Success(val estados: List<EstadoVenta>) : ObtenerEstadosVentaResult()
    data object SessionExpired : ObtenerEstadosVentaResult()
}

class ObtenerEstadosVentaUseCase(
    private val ventaRepository: VentaRepository
) {
    suspend operator fun invoke(): ObtenerEstadosVentaResult {
        return try {
            val estados = ventaRepository.obtenerEstadosVenta()
            ObtenerEstadosVentaResult.Success(estados)
        } catch (e: SessionExpiredException) {
            ObtenerEstadosVentaResult.SessionExpired
        }
    }
}
