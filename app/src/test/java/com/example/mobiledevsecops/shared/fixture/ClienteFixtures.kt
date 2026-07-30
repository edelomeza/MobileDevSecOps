package com.example.mobiledevsecops.shared.fixture

import com.example.mobiledevsecops.data.remote.dto.ClienteDto
import com.example.mobiledevsecops.data.remote.dto.ClienteListResponse
import com.example.mobiledevsecops.domain.model.Cliente
import com.example.mobiledevsecops.domain.model.ClientePage

object ClienteFixtures {

    val cliente = Cliente(
        id = 1,
        strNombreCliente = "Juan Pérez",
        strDireccionCliente = "Calle 123",
        strCorreoElectronico = "juan@example.com",
        strNumeroTelefono = "5512345678",
        rowVersion = "AAAAAAAAB9E="
    )

    val clienteDto = ClienteDto(
        id = 1,
        strNombreCliente = "Juan Pérez",
        strDireccionCliente = "Calle 123",
        strCorreoElectronico = "juan@example.com",
        strNumeroTelefono = "5512345678",
        rowVersion = "AAAAAAAAB9E="
    )

    val clientePage = ClientePage(
        items = listOf(cliente),
        totalCount = 1,
        pageNumber = 1,
        totalPages = 1
    )

    val clienteListResponse = ClienteListResponse(
        items = listOf(clienteDto),
        totalCount = 1,
        pageNumber = 1,
        pageSize = 10,
        totalPages = 1
    )

    val clientesMultiPage = (1..15).map { i ->
        Cliente(
            id = i,
            strNombreCliente = "Cliente $i",
            strDireccionCliente = if (i % 2 == 0) "Dirección $i" else null,
            strCorreoElectronico = "cliente$i@example.com",
            strNumeroTelefono = "551234${(i * 1111).toString().padStart(4, '0').takeLast(4)}",
            rowVersion = "AAAAAAAAB9E="
        )
    }

    val clientesConDireccionNulaMultiPage = (1..16).map { i ->
        Cliente(
            id = i,
            strNombreCliente = "Cliente $i",
            strDireccionCliente = null,
            strCorreoElectronico = "cliente$i@example.com",
            strNumeroTelefono = "5512345678",
            rowVersion = "AAAAAAAAB9E="
        )
    }
}
