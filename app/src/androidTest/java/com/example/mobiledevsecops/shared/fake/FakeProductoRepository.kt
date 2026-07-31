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

    private val _productos = mutableListOf<Producto>()
    val productos: List<Producto> get() = _productos.toList()

    var totalCount = 0
    var totalPages = 1

    fun givenProductos(productos: List<Producto>) {
        _productos.clear()
        _productos.addAll(productos)
        totalCount = productos.size
        totalPages = maxOf(1, (productos.size + 7) / 8)
    }

    override suspend fun getProductos(page: Int, pageSize: Int): ProductoPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception("Error")
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, _productos.size)
        val items = if (start < _productos.size) _productos.subList(start, end) else emptyList()
        totalPages = maxOf(1, (totalCount + pageSize - 1) / pageSize)
        return ProductoPage(items, totalCount, page, totalPages)
    }

    override suspend fun buscarProductos(texto: String, page: Int, pageSize: Int): ProductoPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception("Error")
        val filtered = _productos.filter { it.strNombreProducto.contains(texto, ignoreCase = true) }
        val ftotal = filtered.size
        val fpages = maxOf(1, (ftotal + pageSize - 1) / pageSize)
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, ftotal)
        val items = if (start < ftotal) filtered.subList(start, end) else emptyList()
        return ProductoPage(items, ftotal, page, fpages)
    }

    override suspend fun crearProducto(nombre: String, url: String?, desc: String?, existencia: Int, precio: Double) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception("Error")
        _productos.add(Producto(_productos.size + 1, nombre, url, desc, existencia, precio, "AAAAAAAAB9E="))
        totalCount = _productos.size
    }

    override suspend fun actualizarProducto(id: Int, nombre: String, url: String?, desc: String?, existencia: Int, precio: Double, rowVersion: String) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception("Error")
        val index = _productos.indexOfFirst { it.id == id }
        if (index != -1) {
            _productos[index] = _productos[index].copy(
                strNombreProducto = nombre,
                strURLImagen = url,
                strDescripcion = desc,
                intNumeroExistencia = existencia,
                decPrecio = precio
            )
        }
    }

    override suspend fun eliminarProducto(id: Int, rowVersion: String) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception("Error")
        _productos.removeAll { it.id == id }
        totalCount = _productos.size
    }
}
