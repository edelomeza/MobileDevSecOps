package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.ProductoApi
import com.example.mobiledevsecops.data.remote.dto.ProductCreateRequest
import com.example.mobiledevsecops.data.remote.dto.ProductDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.ProductUpdateRequest
import com.example.mobiledevsecops.data.remote.dto.ProductoListResponse
import com.example.mobiledevsecops.domain.model.Producto
import com.example.mobiledevsecops.domain.model.ProductoPage
import com.example.mobiledevsecops.domain.repository.ProductoRepository

class ProductoRepositoryImpl(
    private val productoApi: ProductoApi
) : ProductoRepository {

    override suspend fun getProductos(page: Int, pageSize: Int): ProductoPage {
        val response: ProductoListResponse = productoApi.getProductos(page, pageSize)
        return response.toDomain()
    }

    override suspend fun buscarProductos(texto: String, page: Int, pageSize: Int): ProductoPage {
        val response: ProductoListResponse = productoApi.buscarProductos(texto, page, pageSize)
        return response.toDomain()
    }

    override suspend fun crearProducto(
        strNombreProducto: String,
        strURLImagen: String?,
        strDescripcion: String?,
        intNumeroExistencia: Int,
        decPrecio: Double
    ) {
        val request = ProductCreateRequest(
            strNombreProducto = strNombreProducto,
            strURLImagen = strURLImagen,
            strDescripcion = strDescripcion,
            intNumeroExistencia = intNumeroExistencia,
            decPrecio = decPrecio
        )
        productoApi.crearProducto(request)
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
        val request = ProductUpdateRequest(
            id = id,
            strNombreProducto = strNombreProducto,
            strURLImagen = strURLImagen,
            strDescripcion = strDescripcion,
            intNumeroExistencia = intNumeroExistencia,
            decPrecio = decPrecio,
            rowVersion = rowVersion
        )
        productoApi.actualizarProducto(request)
    }

    override suspend fun eliminarProducto(id: Int, rowVersion: String) {
        val request = ProductDeleteRequest(
            id = id,
            rowVersion = rowVersion
        )
        productoApi.eliminarProducto(request)
    }
}

private fun ProductoListResponse.toDomain(): ProductoPage = ProductoPage(
    items = items.map { productoDto ->
        Producto(
            id = productoDto.id,
            strNombreProducto = productoDto.strNombreProducto,
            strURLImagen = productoDto.strURLImagen,
            strDescripcion = productoDto.strDescripcion,
            intNumeroExistencia = productoDto.intNumeroExistencia,
            decPrecio = productoDto.decPrecio,
            rowVersion = productoDto.rowVersion ?: ""
        )
    },
    totalCount = totalCount,
    pageNumber = pageNumber,
    totalPages = totalPages
)
