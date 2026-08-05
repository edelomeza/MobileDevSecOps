package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.VentaDetalle
import com.example.mobiledevsecops.domain.repository.VentaRepository

sealed class CrearVentaDetalleResult {
    data class Success(val detalle: VentaDetalle) : CrearVentaDetalleResult()
    data class ValidationError(val mensaje: String) : CrearVentaDetalleResult()
    data class Error(val mensaje: String) : CrearVentaDetalleResult()
    data object SessionExpired : CrearVentaDetalleResult()
}

class CrearVentaDetalleUseCase(
    private val ventaRepository: VentaRepository
) {
    suspend operator fun invoke(
        idVenVenta: Int,
        idProProducto: Int,
        intPiezaVenta: Int
    ): CrearVentaDetalleResult {
        if (idVenVenta <= 0) {
            return CrearVentaDetalleResult.ValidationError("Venta inválida")
        }
        if (idProProducto <= 0) {
            return CrearVentaDetalleResult.ValidationError("Debe seleccionar un producto")
        }
        if (intPiezaVenta <= 0) {
            return CrearVentaDetalleResult.ValidationError("Las piezas deben ser mayores a 0")
        }

        return try {
            val detalle = ventaRepository.crearVentaDetalle(idVenVenta, idProProducto, intPiezaVenta)
            CrearVentaDetalleResult.Success(detalle)
        } catch (e: SessionExpiredException) {
            CrearVentaDetalleResult.SessionExpired
        } catch (e: ConflictException) {
            CrearVentaDetalleResult.Error("El registro fue modificado por otro usuario")
        } catch (e: IllegalArgumentException) {
            CrearVentaDetalleResult.Error(e.message ?: "Error de validación")
        } catch (e: Exception) {
            CrearVentaDetalleResult.Error("Error al crear el detalle de venta")
        }
    }
}
