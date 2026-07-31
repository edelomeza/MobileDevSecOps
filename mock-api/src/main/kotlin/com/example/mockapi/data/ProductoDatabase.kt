package com.example.mockapi.data

import com.example.mockapi.model.ProductoDto
import com.example.mockapi.model.ProductUpdateRequest
import java.util.UUID

data class Producto(
    val id: Int,
    var nombreProducto: String,
    var urlImagen: String?,
    var descripcion: String?,
    var numeroExistencia: Int,
    var precio: Double,
    val rowVersion: String = UUID.randomUUID().toString()
) {
    fun toDto() = ProductoDto(
        id = id,
        strNombreProducto = nombreProducto,
        strURLImagen = urlImagen,
        strDescripcion = descripcion,
        intNumeroExistencia = numeroExistencia,
        decPrecio = precio,
        rowVersion = rowVersion
    )
}

sealed class ProductoUpdateResult {
    data class Success(val producto: Producto) : ProductoUpdateResult()
    data object NotFound : ProductoUpdateResult()
    data object Conflict : ProductoUpdateResult()
}

sealed class ProductoDeleteResult {
    data object Deleted : ProductoDeleteResult()
    data object NotFound : ProductoDeleteResult()
    data object Conflict : ProductoDeleteResult()
}

class ProductoDatabase {
    private val productos = mutableListOf(
        Producto(1, "Laptop HP", "https://example.com/laptop.jpg", "Laptop HP 15.6 pulgadas", 10, 12500.00),
        Producto(2, "Mouse Inalámbrico", "https://example.com/mouse.jpg", "Mouse ergonómico USB", 50, 250.50),
        Producto(3, "Teclado Mecánico", "https://example.com/teclado.jpg", "Teclado RGB switches azules", 30, 899.99),
        Producto(4, "Monitor 27", "https://example.com/monitor.jpg", "Monitor IPS 4K 27 pulgadas", 15, 5400.00),
        Producto(5, "Audífonos Bluetooth", null, "Audífonos con cancelación de ruido", 25, 1200.00),
        Producto(6, "Webcam HD", null, "Cámara web 1080p con micrófono", 40, 450.00),
        Producto(7, "Hub USB-C", null, "Hub 7 puertos USB-C", 60, 350.00),
        Producto(8, "SSD 1TB", "https://example.com/ssd.jpg", "Disco sólido NVMe 1TB", 20, 1800.00),
        Producto(9, "Memoria RAM 16GB", null, "Memoria DDR5 16GB 5600MHz", 35, 950.00),
        Producto(10, "Cargador Laptop", null, "Cargador universal 65W USB-C", 45, 320.00),
        Producto(11, "Tablet Android", "https://example.com/tablet.jpg", "Tablet 10 pulgadas 128GB", 12, 3200.00),
        Producto(12, "Impresora Multifuncional", null, "Impresora láser WiFi", 8, 2800.00),
        Producto(13, "Router WiFi 6", null, "Router AX3000 doble banda", 22, 1100.00),
        Producto(14, "Disco Duro Externo 2TB", null, "Disco portátil USB 3.0", 18, 850.00),
        Producto(15, "Silla Ergonómica", "https://example.com/silla.jpg", "Silla de oficina ajustable", 5, 4500.00),
        Producto(16, "Escritorio Eléctrico", null, "Escritorio ajustable altura motorizado", 3, 6800.00),
        Producto(17, "Micrófono USB", null, "Micrófono condensador para streaming", 14, 780.00),
        Producto(18, "Cable HDMI 2m", null, "Cable HDMI 2.1 ultra alta velocidad", 100, 85.50),
        Producto(19, "UPS 1500VA", null, "Respaldo de energía para equipo", 7, 2100.00),
        Producto(20, "Base para Laptop", "https://example.com/base.jpg", "Base ajustable con ventilación", 28, 290.00)
    )

    private var nextId = 21

    fun count(): Int = productos.size

    fun list(page: Int, pageSize: Int): List<Producto> {
        val from = (page - 1) * pageSize
        return productos.drop(from).take(pageSize)
    }

    fun create(
        nombreProducto: String,
        urlImagen: String?,
        descripcion: String?,
        existencia: Int,
        precio: Double
    ): Producto {
        val producto = Producto(
            id = nextId++,
            nombreProducto = nombreProducto,
            urlImagen = urlImagen,
            descripcion = descripcion,
            numeroExistencia = existencia,
            precio = precio
        )
        productos.add(producto)
        return producto
    }

    fun update(id: Int, request: ProductUpdateRequest): ProductoUpdateResult {
        val index = productos.indexOfFirst { it.id == id }
        if (index == -1) return ProductoUpdateResult.NotFound
        val current = productos[index]
        if (current.rowVersion != request.rowVersion) return ProductoUpdateResult.Conflict
        val updated = current.copy(
            nombreProducto = request.strNombreProducto,
            urlImagen = request.strURLImagen,
            descripcion = request.strDescripcion,
            numeroExistencia = request.intNumeroExistencia,
            precio = request.decPrecio,
            rowVersion = UUID.randomUUID().toString()
        )
        productos[index] = updated
        return ProductoUpdateResult.Success(updated)
    }

    fun delete(id: Int, rowVersion: String): ProductoDeleteResult {
        val index = productos.indexOfFirst { it.id == id }
        if (index == -1) return ProductoDeleteResult.NotFound
        if (productos[index].rowVersion != rowVersion) return ProductoDeleteResult.Conflict
        productos.removeAt(index)
        return ProductoDeleteResult.Deleted
    }

    fun buscar(texto: String, page: Int, pageSize: Int): List<Producto> {
        val filtered = productos.filter {
            it.nombreProducto.contains(texto, ignoreCase = true)
        }
        val from = (page - 1) * pageSize
        return filtered.drop(from).take(pageSize)
    }

    fun countSearch(texto: String): Int {
        return productos.count {
            it.nombreProducto.contains(texto, ignoreCase = true)
        }
    }
}
