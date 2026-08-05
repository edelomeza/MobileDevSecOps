package com.example.mobiledevsecops.shared.fake

import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.EstadoVenta
import com.example.mobiledevsecops.domain.model.ProductoAutocomplete
import com.example.mobiledevsecops.domain.model.Venta
import com.example.mobiledevsecops.domain.model.VentaDetalle
import com.example.mobiledevsecops.domain.model.VentaPage
import com.example.mobiledevsecops.domain.repository.VentaRepository

class FakeVentaRepository : VentaRepository {

    var shouldThrowException = false
    var shouldThrowSessionExpired = false
    var exceptionMessage = "Error de conexión"

    private val _ventas = mutableListOf<Venta>()
    val ventas: List<Venta> get() = _ventas.toList()

    var currentPage = 1
    var totalPages = 1
    var totalCount = 0

    var lastIdCliCliente = 0
    var lastIdSegUsuario = 0
    var lastDteFechaHoraCompra: String? = null
    var lastStrClaveVenta: String? = null

    var lastActualizarEstadoId = 0
    var lastActualizarEstadoIdVenCatEstado = 0
    var lastCrearDetalleIdVenVenta = 0
    var lastCrearDetalleIdProProducto = 0
    var lastCrearDetalleIntPiezaVenta = 0
    var lastBuscarProductosTexto: String? = null

    private val _estadosVenta = mutableListOf(
        EstadoVenta(1, "Abierta"),
        EstadoVenta(2, "Pagada"),
        EstadoVenta(3, "Cancelada")
    )
    val estadosVenta: List<EstadoVenta> get() = _estadosVenta.toList()

    private val _detalles = mutableListOf<VentaDetalle>()
    val detalles: List<VentaDetalle> get() = _detalles.toList()

    private val _productosAutocomplete = mutableListOf<ProductoAutocomplete>()
    val productosAutocomplete: List<ProductoAutocomplete> get() = _productosAutocomplete.toList()

    fun givenVentas(ventas: List<Venta>) {
        _ventas.clear()
        _ventas.addAll(ventas)
        totalCount = ventas.size
        totalPages = maxOf(1, (ventas.size + 7) / 8)
    }

    fun givenProductosAutocomplete(productos: List<ProductoAutocomplete>) {
        _productosAutocomplete.clear()
        _productosAutocomplete.addAll(productos)
    }

    fun givenDetalles(detalles: List<VentaDetalle>) {
        _detalles.clear()
        _detalles.addAll(detalles)
    }

    override suspend fun getVentas(page: Int, pageSize: Int): VentaPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        currentPage = page
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, _ventas.size)
        val items = if (start < _ventas.size) _ventas.subList(start, end) else emptyList()

        return VentaPage(
            items = items,
            totalCount = totalCount,
            pageNumber = page,
            totalPages = totalPages
        )
    }

    override suspend fun buscarVentas(
        strClaveVenta: String?,
        strNombreCliente: String?,
        dteFechaInicio: String?,
        dteFechaFin: String?,
        page: Int,
        pageSize: Int
    ): VentaPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        val filtered = _ventas.filter { venta ->
            val matchTexto = strClaveVenta.isNullOrBlank() ||
                venta.strClaveVenta.contains(strClaveVenta, ignoreCase = true) ||
                venta.strNombreCliente.contains(strClaveVenta, ignoreCase = true)
            val fecha = venta.dteFechaHoraCompra?.take(10) ?: ""
            val matchInicio = dteFechaInicio.isNullOrBlank() || fecha >= dteFechaInicio
            val matchFin = dteFechaFin.isNullOrBlank() || fecha <= dteFechaFin
            matchTexto && matchInicio && matchFin
        }
        val filteredTotal = filtered.size
        val filteredTotalPages = maxOf(1, (filteredTotal + pageSize - 1) / pageSize)
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, filteredTotal)
        val items = if (start < filteredTotal) filtered.subList(start, end) else emptyList()

        return VentaPage(
            items = items,
            totalCount = filteredTotal,
            pageNumber = page,
            totalPages = filteredTotalPages
        )
    }

    override suspend fun crearVenta(
        idCliCliente: Int,
        idSegUsuario: Int,
        dteFechaHoraCompra: String,
        strClaveVenta: String
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        lastIdCliCliente = idCliCliente
        lastIdSegUsuario = idSegUsuario
        lastDteFechaHoraCompra = dteFechaHoraCompra
        lastStrClaveVenta = strClaveVenta

        _ventas.add(
            Venta(
                id = _ventas.size + 1,
                idCliCliente = idCliCliente,
                idSegUsuario = idSegUsuario,
                idVenCatEstado = 1,
                strClaveVenta = strClaveVenta,
                dteFechaHoraCompra = dteFechaHoraCompra,
                strNombreCliente = "Cliente $idCliCliente",
                strEstado = "Abierta",
                rowVersion = "AAAAAAAAB9E="
            )
        )
        totalCount = _ventas.size
    }

    override suspend fun actualizarEstadoVenta(
        id: Int,
        idCliCliente: Int,
        idSegUsuario: Int,
        idVenCatEstado: Int,
        rowVersion: String?
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        lastActualizarEstadoId = id
        lastActualizarEstadoIdVenCatEstado = idVenCatEstado
    }

    override suspend fun obtenerEstadosVenta(): List<EstadoVenta> {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)
        return _estadosVenta
    }

    override suspend fun crearVentaDetalle(
        idVenVenta: Int,
        idProProducto: Int,
        intPiezaVenta: Int
    ): VentaDetalle {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        lastCrearDetalleIdVenVenta = idVenVenta
        lastCrearDetalleIdProProducto = idProProducto
        lastCrearDetalleIntPiezaVenta = intPiezaVenta

        val detalle = VentaDetalle(
            id = _detalles.size + 1,
            idVenVenta = idVenVenta,
            idProProducto = idProProducto,
            strNombreProducto = "Producto $idProProducto",
            decPrecio = 100.0,
            intPiezaVenta = intPiezaVenta,
            decTotalVenta = intPiezaVenta * 100.0
        )
        _detalles.add(detalle)
        return detalle
    }

    override suspend fun buscarProductosAutocomplete(
        texto: String,
        maxResultados: Int
    ): List<ProductoAutocomplete> {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        lastBuscarProductosTexto = texto
        return _productosAutocomplete.filter {
            it.strTextoAutocomplete.contains(texto, ignoreCase = true)
        }.take(maxResultados)
    }

    var lastEliminarDetalleId = 0
    var lastEliminarDetalleRowVersion: String? = null

    override suspend fun eliminarVentaDetalle(id: Int, rowVersion: String) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        lastEliminarDetalleId = id
        lastEliminarDetalleRowVersion = rowVersion
    }

    override suspend fun getDetallesByVentaId(ventaId: Int): List<VentaDetalle> {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        return _detalles.filter { it.idVenVenta == ventaId }
    }
}
