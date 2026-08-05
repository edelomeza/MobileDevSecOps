package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.VentaPage
import com.example.mobiledevsecops.domain.repository.VentaRepository

private const val MAX_SEARCH_LENGTH = 100

sealed class BuscarVentasResult {
    data class Success(val page: VentaPage) : BuscarVentasResult()
    data class ValidationError(val mensaje: String) : BuscarVentasResult()
    data class Error(val mensaje: String) : BuscarVentasResult()
    data object SessionExpired : BuscarVentasResult()
}

class BuscarVentasUseCase(
    private val ventaRepository: VentaRepository
) {
    suspend operator fun invoke(
        texto: String,
        dteFechaInicio: String?,
        dteFechaFin: String?,
        page: Int,
        pageSize: Int = 8
    ): BuscarVentasResult {
        val error = validar(texto, dteFechaInicio, dteFechaFin)
        if (error != null) {
            return BuscarVentasResult.ValidationError(error)
        }

        val trimmed = texto.trim().ifBlank { null }

        return try {
            val result = ventaRepository.buscarVentas(
                strClaveVenta = trimmed,
                strNombreCliente = trimmed,
                dteFechaInicio = dteFechaInicio?.trim()?.ifBlank { null },
                dteFechaFin = dteFechaFin?.trim()?.ifBlank { null },
                page = page,
                pageSize = pageSize
            )
            BuscarVentasResult.Success(result)
        } catch (e: SessionExpiredException) {
            BuscarVentasResult.SessionExpired
        } catch (e: Exception) {
            BuscarVentasResult.Error("Error al buscar ventas")
        }
    }

    fun validar(texto: String, dteFechaInicio: String?, dteFechaFin: String?): String? {
        val trimmed = texto.trim()
        val inicio = dteFechaInicio?.trim().orEmpty()
        val fin = dteFechaFin?.trim().orEmpty()

        if (trimmed.isEmpty() && inicio.isEmpty() && fin.isEmpty()) {
            return "Debe especificar al menos un criterio de búsqueda"
        }

        if (trimmed.length > MAX_SEARCH_LENGTH) {
            return "El texto de búsqueda no puede exceder $MAX_SEARCH_LENGTH caracteres"
        }

        if (inicio.isNotEmpty() && fin.isNotEmpty() && inicio > fin) {
            return "La fecha desde no puede ser mayor que la fecha hasta"
        }

        return null
    }
}