package com.example.mobiledevsecops.shared.fixture

import com.example.mobiledevsecops.data.remote.dto.ProductoDto
import com.example.mobiledevsecops.data.remote.dto.ProductoListResponse
import com.example.mobiledevsecops.domain.model.Producto
import com.example.mobiledevsecops.domain.model.ProductoPage

object ProductoFixtures {

    val producto = Producto(
        id = 1,
        strNombreProducto = "Laptop HP",
        strURLImagen = "https://example.com/laptop.jpg",
        strDescripcion = "Laptop HP 15.6 pulgadas",
        intNumeroExistencia = 10,
        decPrecio = 12500.00,
        rowVersion = "AAAAAAAAB9E="
    )

    val productoDto = ProductoDto(
        id = 1,
        strNombreProducto = "Laptop HP",
        strURLImagen = "https://example.com/laptop.jpg",
        strDescripcion = "Laptop HP 15.6 pulgadas",
        intNumeroExistencia = 10,
        decPrecio = 12500.00,
        rowVersion = "AAAAAAAAB9E="
    )

    val productoPage = ProductoPage(
        items = listOf(producto),
        totalCount = 1,
        pageNumber = 1,
        totalPages = 1
    )

    val productoListResponse = ProductoListResponse(
        items = listOf(productoDto),
        totalCount = 1,
        pageNumber = 1,
        pageSize = 10,
        totalPages = 1
    )

    val productosMultiPage = (1..15).map { i ->
        Producto(
            id = i,
            strNombreProducto = "Producto $i",
            strURLImagen = if (i % 2 == 0) "https://example.com/$i.jpg" else null,
            strDescripcion = if (i % 3 == 0) "Descripción $i" else null,
            intNumeroExistencia = i * 5,
            decPrecio = i * 100.50,
            rowVersion = "AAAAAAAAB9E="
        )
    }
}
