package com.example.mobiledevsecops.shared.fixture

import com.example.mobiledevsecops.domain.model.Producto

object ProductoFixtures {
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
