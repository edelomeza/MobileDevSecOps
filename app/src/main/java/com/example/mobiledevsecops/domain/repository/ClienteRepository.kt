package com.example.mobiledevsecops.domain.repository

import com.example.mobiledevsecops.domain.model.ClientePage

interface ClienteRepository {
    suspend fun getClientes(page: Int, pageSize: Int = 10): ClientePage
    suspend fun buscarClientes(texto: String, page: Int, pageSize: Int = 10): ClientePage
    suspend fun getClienteById(id: Int): com.example.mobiledevsecops.domain.model.Cliente
    suspend fun crearCliente(strNombreCliente: String, strDireccionCliente: String?, strCorreoElectronico: String, strNumeroTelefono: String)
    suspend fun actualizarCliente(id: Int, strNombreCliente: String, strDireccionCliente: String?, strCorreoElectronico: String, strNumeroTelefono: String, rowVersion: String)
    suspend fun eliminarCliente(id: Int, rowVersion: String)
}
