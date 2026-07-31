package com.example.mobiledevsecops.domain.repository

import com.example.mobiledevsecops.domain.model.ProductoPage

interface ProductoRepository {
    suspend fun getProductos(page: Int, pageSize: Int = 10): ProductoPage
    suspend fun buscarProductos(texto: String, page: Int, pageSize: Int = 10): ProductoPage
    suspend fun crearProducto(
        strNombreProducto: String,
        strURLImagen: String?,
        strDescripcion: String?,
        intNumeroExistencia: Int,
        decPrecio: Double
    )
    suspend fun actualizarProducto(
        id: Int,
        strNombreProducto: String,
        strURLImagen: String?,
        strDescripcion: String?,
        intNumeroExistencia: Int,
        decPrecio: Double,
        rowVersion: String
    )
    suspend fun eliminarProducto(id: Int, rowVersion: String)
}
