package com.example.mobiledevsecops.shared.fake

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.Producto
import com.example.mobiledevsecops.domain.model.ProductoPage
import com.example.mobiledevsecops.domain.repository.ProductoRepository

class FakeProductoRepository : ProductoRepository {

    var shouldThrowException = false
    var shouldThrowSessionExpired = false
    var shouldThrowConflict = false
    var exceptionMessage = "Error de conexión"

    private val _productos = mutableListOf<Producto>()
    val productos: List<Producto> get() = _productos.toList()

    var currentPage = 1
    var totalPages = 1
    var totalCount = 0

    fun givenProductos(productos: List<Producto>) {
        _productos.clear()
        _productos.addAll(productos)
        totalCount = productos.size
        totalPages = maxOf(1, (productos.size + 7) / 8)
    }

    override suspend fun getProductos(page: Int, pageSize: Int): ProductoPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        currentPage = page
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, _productos.size)
        val items = if (start < _productos.size) _productos.subList(start, end) else emptyList()
        totalPages = maxOf(1, (totalCount + pageSize - 1) / pageSize)

        return ProductoPage(
            items = items,
            totalCount = totalCount,
            pageNumber = page,
            totalPages = totalPages
        )
    }

    override suspend fun buscarProductos(texto: String, page: Int, pageSize: Int): ProductoPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        val filtered = _productos.filter {
            it.strNombreProducto.contains(texto, ignoreCase = true)
        }
        val filteredTotal = filtered.size
        val filteredTotalPages = maxOf(1, (filteredTotal + pageSize - 1) / pageSize)
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, filteredTotal)
        val items = if (start < filteredTotal) filtered.subList(start, end) else emptyList()

        return ProductoPage(
            items = items,
            totalCount = filteredTotal,
            pageNumber = page,
            totalPages = filteredTotalPages
        )
    }

    override suspend fun crearProducto(
        strNombreProducto: String,
        strURLImagen: String?,
        strDescripcion: String?,
        intNumeroExistencia: Int,
        decPrecio: Double
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        _productos.add(
            Producto(
                id = _productos.size + 1,
                strNombreProducto = strNombreProducto,
                strURLImagen = strURLImagen,
                strDescripcion = strDescripcion,
                intNumeroExistencia = intNumeroExistencia,
                decPrecio = decPrecio,
                rowVersion = "AAAAAAAAB9E="
            )
        )
        totalCount = _productos.size
    }

    override suspend fun actualizarProducto(
        id: Int,
        strNombreProducto: String,
        strURLImagen: String?,
        strDescripcion: String?,
        intNumeroExistencia: Int,
        decPrecio: Double,
        rowVersion: String
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        val index = _productos.indexOfFirst { it.id == id }
        if (index != -1) {
            _productos[index] = _productos[index].copy(
                strNombreProducto = strNombreProducto,
                strURLImagen = strURLImagen,
                strDescripcion = strDescripcion,
                intNumeroExistencia = intNumeroExistencia,
                decPrecio = decPrecio
            )
        }
    }

    override suspend fun eliminarProducto(id: Int, rowVersion: String) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        _productos.removeAll { it.id == id }
        totalCount = _productos.size
    }
}
