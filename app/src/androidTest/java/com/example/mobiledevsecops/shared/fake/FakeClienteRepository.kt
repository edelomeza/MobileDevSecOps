package com.example.mobiledevsecops.shared.fake

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.Cliente
import com.example.mobiledevsecops.domain.model.ClientePage
import com.example.mobiledevsecops.domain.repository.ClienteRepository

class FakeClienteRepository : ClienteRepository {

    var shouldThrowException = false
    var shouldThrowSessionExpired = false
    var shouldThrowConflict = false
    var exceptionMessage = "Error de conexión"

    private val _clientes = mutableListOf<Cliente>()
    val clientes: List<Cliente> get() = _clientes.toList()

    var currentPage = 1
    var totalPages = 1
    var totalCount = 0

    fun givenClientes(clientes: List<Cliente>) {
        _clientes.clear()
        _clientes.addAll(clientes)
        totalCount = clientes.size
        totalPages = maxOf(1, (clientes.size + 9) / 10)
    }

    override suspend fun getClientes(page: Int, pageSize: Int): ClientePage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        currentPage = page
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, _clientes.size)
        val items = if (start < _clientes.size) _clientes.subList(start, end) else emptyList()

        return ClientePage(
            items = items,
            totalCount = totalCount,
            pageNumber = page,
            totalPages = totalPages
        )
    }

    override suspend fun buscarClientes(texto: String, page: Int, pageSize: Int): ClientePage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        val filtered = _clientes.filter {
            it.strNombreCliente.contains(texto, ignoreCase = true) ||
                it.strCorreoElectronico.contains(texto, ignoreCase = true)
        }
        val filteredTotal = filtered.size
        val filteredTotalPages = maxOf(1, (filteredTotal + pageSize - 1) / pageSize)
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, filteredTotal)
        val items = if (start < filteredTotal) filtered.subList(start, end) else emptyList()

        return ClientePage(
            items = items,
            totalCount = filteredTotal,
            pageNumber = page,
            totalPages = filteredTotalPages
        )
    }

    override suspend fun getClienteById(id: Int): Cliente {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)
        return _clientes.first { it.id == id }
    }

    override suspend fun crearCliente(
        strNombreCliente: String,
        strDireccionCliente: String?,
        strCorreoElectronico: String,
        strNumeroTelefono: String
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        _clientes.add(
            Cliente(
                id = _clientes.size + 1,
                strNombreCliente = strNombreCliente,
                strDireccionCliente = strDireccionCliente,
                strCorreoElectronico = strCorreoElectronico,
                strNumeroTelefono = strNumeroTelefono,
                rowVersion = "AAAAAAAAB9E="
            )
        )
        totalCount = _clientes.size
    }

    override suspend fun actualizarCliente(
        id: Int,
        strNombreCliente: String,
        strDireccionCliente: String?,
        strCorreoElectronico: String,
        strNumeroTelefono: String,
        rowVersion: String
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        val index = _clientes.indexOfFirst { it.id == id }
        if (index != -1) {
            _clientes[index] = _clientes[index].copy(
                strNombreCliente = strNombreCliente,
                strDireccionCliente = strDireccionCliente,
                strCorreoElectronico = strCorreoElectronico,
                strNumeroTelefono = strNumeroTelefono
            )
        }
    }

    override suspend fun eliminarCliente(id: Int, rowVersion: String) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        _clientes.removeAll { it.id == id }
        totalCount = _clientes.size
    }
}
