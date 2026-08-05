package com.example.mockapi.data

import com.example.mockapi.model.VentaDetalleDto
import com.example.mockapi.model.VentaDto
import java.util.UUID

data class VenCatEstado(
    val id: Int,
    val strValor: String,
    val strDescripcion: String
)

data class Venta(
    val id: Int,
    var idCliCliente: Int,
    var idSegUsuario: Int,
    var idVenCatEstado: Int,
    var dteFechaHoraCompra: String,
    var strClaveVenta: String,
    val rowVersion: String = UUID.randomUUID().toString()
)

data class VentaDetalle(
    val id: Int,
    val idVenVenta: Int,
    val idProProducto: Int,
    val intPiezaVenta: Int,
    val decTotalVenta: Double,
    val rowVersion: String = UUID.randomUUID().toString()
)

object EstadoVentaCatalog {
    val estados = listOf(
        VenCatEstado(1, "Abierta", "Venta en proceso"),
        VenCatEstado(2, "Pagada", "Venta pagada"),
        VenCatEstado(3, "Cancelada", "Venta cancelada")
    )

    fun getById(id: Int): VenCatEstado? = estados.firstOrNull { it.id == id }
}

class VentaDatabase(
    private val clienteDatabase: ClienteDatabase,
    private val productoDatabase: ProductoDatabase
) {
    private val ventas = mutableListOf(
        Venta(1, 1, 1, 2, "2026-07-01T10:15:00", "V001001001"),
        Venta(2, 2, 2, 2, "2026-07-03T11:30:00", "V001001002"),
        Venta(3, 3, 1, 1, "2026-07-05T09:45:00", "V001001003"),
        Venta(4, 4, 3, 2, "2026-07-08T14:20:00", "V001001004"),
        Venta(5, 5, 2, 3, "2026-07-10T16:05:00", "V001001005"),
        Venta(6, 6, 1, 1, "2026-07-12T12:00:00", "V001001006"),
        Venta(7, 7, 4, 2, "2026-07-15T10:40:00", "V001001007"),
        Venta(8, 8, 2, 2, "2026-07-18T17:25:00", "V001001008"),
        Venta(9, 9, 3, 1, "2026-07-20T13:10:00", "V001001009"),
        Venta(10, 10, 1, 2, "2026-07-22T15:50:00", "V001001010"),
        Venta(11, 1, 4, 2, "2026-07-25T11:35:00", "V001001011"),
        Venta(12, 2, 2, 1, "2026-07-28T18:00:00", "V001001012")
    )

    private val detalles = mutableListOf<VentaDetalle>()
    private var nextVentaId = 13
    private var nextDetalleId = 1

    fun count(): Int = ventas.size

    fun list(page: Int, pageSize: Int): List<Venta> {
        val from = (page - 1) * pageSize
        return ventas.drop(from).take(pageSize)
    }

    fun getById(id: Int): Venta? = ventas.firstOrNull { it.id == id }

    fun updateEstado(id: Int, idVenCatEstado: Int, rowVersion: String?): Boolean {
        val venta = ventas.firstOrNull { it.id == id } ?: return false
        venta.idVenCatEstado = idVenCatEstado
        return true
    }

    fun buscar(
        strClaveVenta: String?,
        strNombreCliente: String?,
        dteFechaInicio: String?,
        dteFechaFin: String?,
        page: Int,
        pageSize: Int
    ): List<Venta> {
        val from = (page - 1) * pageSize
        return filtradas(strClaveVenta, strNombreCliente, dteFechaInicio, dteFechaFin).drop(from).take(pageSize)
    }

    fun countSearch(
        strClaveVenta: String?,
        strNombreCliente: String?,
        dteFechaInicio: String?,
        dteFechaFin: String?
    ): Int = filtradas(strClaveVenta, strNombreCliente, dteFechaInicio, dteFechaFin).size

    fun create(
        idCliCliente: Int,
        idSegUsuario: Int,
        dteFechaHoraCompra: String,
        strClaveVenta: String
    ): Venta {
        val venta = Venta(
            id = nextVentaId++,
            idCliCliente = idCliCliente,
            idSegUsuario = idSegUsuario,
            idVenCatEstado = EstadoVentaCatalog.estados.first().id,
            dteFechaHoraCompra = dteFechaHoraCompra,
            strClaveVenta = strClaveVenta
        )
        ventas.add(venta)
        return venta
    }

    fun createDetalle(
        idVenVenta: Int,
        idProProducto: Int,
        intPiezaVenta: Int
    ): VentaDetalle? {
        val producto = productoDatabase.listAll().firstOrNull { it.id == idProProducto } ?: return null
        val precio = producto.precio
        val total = intPiezaVenta * precio

        val detalle = VentaDetalle(
            id = nextDetalleId++,
            idVenVenta = idVenVenta,
            idProProducto = idProProducto,
            intPiezaVenta = intPiezaVenta,
            decTotalVenta = total
        )
        detalles.add(detalle)
        return detalle
    }

    fun autocompleteProductos(texto: String, maxResultados: Int): List<com.example.mockapi.model.ProductoAutocompleteDto> {
        val productos = productoDatabase.listAll()
        return productos
            .filter { it.nombreProducto.contains(texto, ignoreCase = true) }
            .sortedBy { it.nombreProducto }
            .take(maxResultados)
            .map {
                com.example.mockapi.model.ProductoAutocompleteDto(
                    id = it.id,
                    strTextoAutocomplete = "${it.nombreProducto} | #: ${it.numeroExistencia} | $: ${it.precio}"
                )
            }
    }

    fun listDetallesByVentaId(ventaId: Int): List<VentaDetalle> {
        return detalles.filter { it.idVenVenta == ventaId }
    }

    fun deleteDetalle(id: Int): Boolean {
        return detalles.removeAll { it.id == id }
    }

    fun toDto(venta: Venta): VentaDto {
        val nombreCliente = clienteDatabase.getById(venta.idCliCliente)?.strNombreCliente ?: ""
        val estado = EstadoVentaCatalog.getById(venta.idVenCatEstado)
        return VentaDto(
            id = venta.id,
            strClaveVenta = venta.strClaveVenta,
            dteFechaHoraCompra = venta.dteFechaHoraCompra,
            strNombreCliente = nombreCliente,
            strEstado = estado?.strValor ?: "",
            rowVersion = venta.rowVersion
        )
    }

    fun toDetalleDto(detalle: VentaDetalle): VentaDetalleDto {
        val producto = productoDatabase.listAll().firstOrNull { it.id == detalle.idProProducto }
        return VentaDetalleDto(
            id = detalle.id,
            idVenVenta = detalle.idVenVenta,
            idProProducto = detalle.idProProducto,
            strNombreProducto = producto?.nombreProducto ?: "",
            decPrecio = producto?.precio ?: 0.0,
            intPiezaVenta = detalle.intPiezaVenta,
            decTotalVenta = detalle.decTotalVenta,
            rowVersion = detalle.rowVersion
        )
    }

    private fun filtradas(
        strClaveVenta: String?,
        strNombreCliente: String?,
        dteFechaInicio: String?,
        dteFechaFin: String?
    ): List<Venta> {
        val inicio = dteFechaInicio?.trim().orEmpty()
        val fin = dteFechaFin?.trim().orEmpty()

        return ventas.filter { venta ->
            val matchesClave = strClaveVenta.isNullOrBlank() ||
                venta.strClaveVenta.contains(strClaveVenta, ignoreCase = true)
            val matchesCliente = strNombreCliente.isNullOrBlank() ||
                (clienteDatabase.getById(venta.idCliCliente)?.strNombreCliente
                    ?.contains(strNombreCliente, ignoreCase = true) == true)
            val dia = venta.dteFechaHoraCompra.take(10)
            val matchesInicio = inicio.isEmpty() || dia >= inicio
            val matchesFin = fin.isEmpty() || dia <= fin

            (matchesClave || matchesCliente) && matchesInicio && matchesFin
        }
    }
}
