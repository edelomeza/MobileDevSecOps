package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.ClienteApi
import com.example.mobiledevsecops.data.remote.dto.ClienteCreateRequest
import com.example.mobiledevsecops.data.remote.dto.ClienteDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.ClienteListResponse
import com.example.mobiledevsecops.data.remote.dto.ClienteUpdateRequest
import com.example.mobiledevsecops.domain.model.Cliente
import com.example.mobiledevsecops.domain.model.ClientePage
import com.example.mobiledevsecops.domain.repository.ClienteRepository

class ClienteRepositoryImpl(
    private val clienteApi: ClienteApi
) : ClienteRepository {

    override suspend fun getClientes(page: Int, pageSize: Int): ClientePage {
        val response: ClienteListResponse = clienteApi.getClientes(page, pageSize)
        return response.toDomain()
    }

    override suspend fun buscarClientes(texto: String, page: Int, pageSize: Int): ClientePage {
        val response: ClienteListResponse = clienteApi.buscarClientes(texto, page, pageSize)
        return response.toDomain()
    }

    override suspend fun getClienteById(id: Int): Cliente {
        val dto = clienteApi.getClienteById(id)
        return dto.toDomain()
    }

    override suspend fun crearCliente(
        strNombreCliente: String,
        strDireccionCliente: String?,
        strCorreoElectronico: String,
        strNumeroTelefono: String
    ) {
        val request = ClienteCreateRequest(
            strNombreCliente = strNombreCliente,
            strDireccionCliente = strDireccionCliente,
            strCorreoElectronico = strCorreoElectronico,
            strNumeroTelefono = strNumeroTelefono
        )
        clienteApi.crearCliente(request)
    }

    override suspend fun actualizarCliente(
        id: Int,
        strNombreCliente: String,
        strDireccionCliente: String?,
        strCorreoElectronico: String,
        strNumeroTelefono: String,
        rowVersion: String
    ) {
        val request = ClienteUpdateRequest(
            id = id,
            strNombreCliente = strNombreCliente,
            strDireccionCliente = strDireccionCliente,
            strCorreoElectronico = strCorreoElectronico,
            strNumeroTelefono = strNumeroTelefono,
            rowVersion = rowVersion
        )
        clienteApi.actualizarCliente(request)
    }

    override suspend fun eliminarCliente(id: Int, rowVersion: String) {
        val request = ClienteDeleteRequest(
            id = id,
            rowVersion = rowVersion
        )
        clienteApi.eliminarCliente(request)
    }
}

private fun ClienteListResponse.toDomain(): ClientePage = ClientePage(
    items = items.map { it.toDomain() },
    totalCount = totalCount,
    pageNumber = pageNumber,
    totalPages = totalPages
)

private fun com.example.mobiledevsecops.data.remote.dto.ClienteDto.toDomain(): Cliente = Cliente(
    id = id,
    strNombreCliente = strNombreCliente,
    strDireccionCliente = strDireccionCliente,
    strCorreoElectronico = strCorreoElectronico,
    strNumeroTelefono = strNumeroTelefono,
    rowVersion = rowVersion ?: ""
)
