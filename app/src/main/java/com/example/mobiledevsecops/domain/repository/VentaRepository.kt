package com.example.mobiledevsecops.domain.repository

import com.example.mobiledevsecops.domain.model.EstadoVenta
import com.example.mobiledevsecops.domain.model.ProductoAutocomplete
import com.example.mobiledevsecops.domain.model.VentaDetalle
import com.example.mobiledevsecops.domain.model.VentaPage

interface VentaRepository {
    suspend fun getVentas(page: Int, pageSize: Int = 10): VentaPage
    suspend fun buscarVentas(
        strClaveVenta: String?,
        strNombreCliente: String?,
        dteFechaInicio: String?,
        dteFechaFin: String?,
        page: Int,
        pageSize: Int = 10
    ): VentaPage
    suspend fun crearVenta(
        idCliCliente: Int,
        idSegUsuario: Int,
        dteFechaHoraCompra: String,
        strClaveVenta: String
    )
    suspend fun actualizarEstadoVenta(
        id: Int,
        idCliCliente: Int,
        idSegUsuario: Int,
        idVenCatEstado: Int,
        rowVersion: String?
    )
    suspend fun obtenerEstadosVenta(): List<EstadoVenta>
    suspend fun crearVentaDetalle(
        idVenVenta: Int,
        idProProducto: Int,
        intPiezaVenta: Int
    ): VentaDetalle
    suspend fun buscarProductosAutocomplete(
        texto: String,
        maxResultados: Int = 10
    ): List<ProductoAutocomplete>
    suspend fun eliminarVentaDetalle(id: Int, rowVersion: String)
    suspend fun getDetallesByVentaId(ventaId: Int): List<VentaDetalle>
}
