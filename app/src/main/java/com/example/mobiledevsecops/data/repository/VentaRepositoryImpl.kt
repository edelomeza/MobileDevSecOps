package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.VentaApi
import com.example.mobiledevsecops.data.remote.dto.VentaCreateRequest
import com.example.mobiledevsecops.data.remote.dto.VentaDetalleCreateRequest
import com.example.mobiledevsecops.data.remote.dto.VentaDetalleListResponse
import com.example.mobiledevsecops.data.remote.dto.VentaDto
import com.example.mobiledevsecops.data.remote.dto.VentaListResponse
import com.example.mobiledevsecops.domain.model.EstadoVenta
import com.example.mobiledevsecops.domain.model.ProductoAutocomplete
import com.example.mobiledevsecops.domain.model.Venta
import com.example.mobiledevsecops.domain.model.VentaDetalle
import com.example.mobiledevsecops.domain.model.VentaPage
import com.example.mobiledevsecops.domain.repository.VentaRepository

class VentaRepositoryImpl(
    private val ventaApi: VentaApi
) : VentaRepository {

    override suspend fun getVentas(page: Int, pageSize: Int): VentaPage {
        val response: VentaListResponse = ventaApi.getVentas(page, pageSize)
        return response.toDomain()
    }

    override suspend fun buscarVentas(
        strClaveVenta: String?,
        strNombreCliente: String?,
        dteFechaInicio: String?,
        dteFechaFin: String?,
        page: Int,
        pageSize: Int
    ): VentaPage {
        val response: VentaListResponse = ventaApi.buscarVentas(
            strClaveVenta = strClaveVenta,
            strNombreCliente = strNombreCliente,
            dteFechaInicio = dteFechaInicio,
            dteFechaFin = dteFechaFin,
            page = page,
            pageSize = pageSize
        )
        return response.toDomain()
    }

    override suspend fun crearVenta(
        idCliCliente: Int,
        idSegUsuario: Int,
        dteFechaHoraCompra: String,
        strClaveVenta: String
    ) {
        val request = VentaCreateRequest(
            idCliCliente = idCliCliente,
            idSegUsuario = idSegUsuario,
            dteFechaHoraCompra = dteFechaHoraCompra,
            strClaveVenta = strClaveVenta
        )
        ventaApi.crearVenta(request)
    }

    override suspend fun actualizarEstadoVenta(
        id: Int,
        idCliCliente: Int,
        idSegUsuario: Int,
        idVenCatEstado: Int,
        rowVersion: String?
    ) {
        ventaApi.actualizarEstadoVenta(id, idCliCliente, idSegUsuario, idVenCatEstado, rowVersion)
    }

    override suspend fun obtenerEstadosVenta(): List<EstadoVenta> {
        val response = ventaApi.obtenerEstadosVenta()
        return response.items.map { EstadoVenta(id = it.id, strValor = it.strValor) }
    }

    override suspend fun crearVentaDetalle(
        idVenVenta: Int,
        idProProducto: Int,
        intPiezaVenta: Int
    ): VentaDetalle {
        val response = ventaApi.crearVentaDetalle(
            VentaDetalleCreateRequest(
                idVenVenta = idVenVenta,
                idProProducto = idProProducto,
                intPiezaVenta = intPiezaVenta
            )
        )
        return VentaDetalle(
            id = response.id,
            idVenVenta = response.idVenVenta,
            idProProducto = response.idProProducto,
            strNombreProducto = response.strNombreProducto ?: "",
            decPrecio = response.decPrecio,
            intPiezaVenta = response.intPiezaVenta,
            decTotalVenta = response.decTotalVenta,
            rowVersion = response.rowVersion ?: ""
        )
    }

    override suspend fun buscarProductosAutocomplete(
        texto: String,
        maxResultados: Int
    ): List<ProductoAutocomplete> {
        val response = ventaApi.buscarProductosAutocomplete(texto, maxResultados)
        return response.map { ProductoAutocomplete(id = it.id, strTextoAutocomplete = it.strTextoAutocomplete) }
    }

    override suspend fun eliminarVentaDetalle(id: Int, rowVersion: String) {
        ventaApi.eliminarVentaDetalle(id, rowVersion)
    }

    override suspend fun getDetallesByVentaId(ventaId: Int): List<VentaDetalle> {
        val response = ventaApi.getDetallesByVentaId(ventaId)
        return response.items.filter { it.idVenVenta == ventaId }.map { dto ->
            VentaDetalle(
                id = dto.id,
                idVenVenta = dto.idVenVenta,
                idProProducto = dto.idProProducto,
                strNombreProducto = dto.strNombreProducto ?: "",
                decPrecio = dto.decPrecio,
                intPiezaVenta = dto.intPiezaVenta,
                decTotalVenta = dto.decTotalVenta,
                rowVersion = dto.rowVersion ?: ""
            )
        }
    }
}

private fun VentaListResponse.toDomain(): VentaPage = VentaPage(
    items = items.map { it.toDomain() },
    totalCount = totalCount,
    pageNumber = pageNumber,
    totalPages = totalPages
)

private fun VentaDto.toDomain(): Venta = Venta(
    id = id,
    idCliCliente = idCliCliente,
    idSegUsuario = idSegUsuario,
    idVenCatEstado = idVenCatEstado,
    strClaveVenta = strClaveVenta,
    dteFechaHoraCompra = dteFechaHoraCompra,
    strNombreCliente = strNombreCliente ?: "",
    strEstado = strEstado ?: "",
    rowVersion = rowVersion ?: ""
)
