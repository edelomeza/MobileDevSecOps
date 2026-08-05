package com.example.mobiledevsecops.shared.fixture

import com.example.mobiledevsecops.data.remote.dto.VentaDto
import com.example.mobiledevsecops.data.remote.dto.VentaListResponse
import com.example.mobiledevsecops.domain.model.Venta
import com.example.mobiledevsecops.domain.model.VentaPage

object VentaFixtures {

    val venta = Venta(
        id = 1,
        idCliCliente = 1,
        idSegUsuario = 1,
        idVenCatEstado = 2,
        strClaveVenta = "V001001001",
        dteFechaHoraCompra = "2026-07-01T10:15:00",
        strNombreCliente = "Juan Perez",
        strEstado = "Pagada",
        rowVersion = "AAAAAAAAB9E="
    )

    val ventaDto = VentaDto(
        id = 1,
        idCliCliente = 1,
        strNombreCliente = "Juan Perez",
        idSegUsuario = 1,
        idVenCatEstado = 2,
        strEstado = "Pagada",
        dteFechaHoraCompra = "2026-07-01T10:15:00",
        strClaveVenta = "V001001001",
        rowVersion = "AAAAAAAAB9E="
    )

    val ventaPage = VentaPage(
        items = listOf(venta),
        totalCount = 1,
        pageNumber = 1,
        totalPages = 1
    )

    val ventaListResponse = VentaListResponse(
        items = listOf(ventaDto),
        totalCount = 1,
        pageNumber = 1,
        pageSize = 10,
        totalPages = 1
    )

    val ventasMultiPage = (1..15).map { i ->
        Venta(
            id = i,
            idCliCliente = i,
            idSegUsuario = 1,
            idVenCatEstado = if (i % 2 == 0) 2 else 1,
            strClaveVenta = "V${i.toString().padStart(9, '0')}",
            dteFechaHoraCompra = "2026-07-${i.toString().padStart(2, '0')}T10:15:00",
            strNombreCliente = "Cliente $i",
            strEstado = if (i % 2 == 0) "Pagada" else "Abierta",
            rowVersion = "AAAAAAAAB9E="
        )
    }
}
