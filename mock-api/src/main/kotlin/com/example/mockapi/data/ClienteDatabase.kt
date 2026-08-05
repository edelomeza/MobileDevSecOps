package com.example.mockapi.data

import com.example.mockapi.model.ClienteDto
import java.util.UUID

data class Cliente(
    val id: Int,
    var strNombreCliente: String,
    var strDireccionCliente: String?,
    var strCorreoElectronico: String,
    var strNumeroTelefono: String,
    val rowVersion: String = UUID.randomUUID().toString()
) {
    fun toDto() = ClienteDto(
        id = id,
        strNombreCliente = strNombreCliente,
        strDireccionCliente = strDireccionCliente,
        strCorreoElectronico = strCorreoElectronico,
        strNumeroTelefono = strNumeroTelefono,
        rowVersion = rowVersion
    )
}

class ClienteDatabase {
    private val clientes = mutableListOf(
        Cliente(1, "Juan Perez", "Av. Reforma 123", "juan@example.com", "555-0001"),
        Cliente(2, "Maria Garcia", "Calle 5 de Mayo 45", "maria@example.com", "555-0002"),
        Cliente(3, "Carlos Lopez", "Av. Insurgentes 789", "carlos@example.com", "555-0003"),
        Cliente(4, "Ana Martinez", "Calle Juarez 10", "ana@example.com", "555-0004"),
        Cliente(5, "Pedro Rodriguez", "Blvd. Miguel Hidalgo 22", "pedro@example.com", "555-0005"),
        Cliente(6, "Laura Hernandez", "Av. Universidad 333", "laura@example.com", "555-0006"),
        Cliente(7, "Miguel Sanchez", "Calle Morelos 18", "miguel@example.com", "555-0007"),
        Cliente(8, "Sofia Ramirez", "Av. Revolucion 900", "sofia@example.com", "555-0008"),
        Cliente(9, "Diego Torres", "Calle Hidalgo 34", "diego@example.com", "555-0009"),
        Cliente(10, "Valeria Flores", "Av. Juarez 210", "valeria@example.com", "555-0010")
    )

    private var nextId = 11

    fun count(): Int = clientes.size

    fun list(page: Int, pageSize: Int): List<Cliente> {
        val from = (page - 1) * pageSize
        return clientes.drop(from).take(pageSize)
    }

    fun buscar(texto: String, page: Int, pageSize: Int): List<Cliente> {
        val filtered = filtrados(texto)
        val from = (page - 1) * pageSize
        return filtered.drop(from).take(pageSize)
    }

    fun countSearch(texto: String): Int = filtrados(texto).size

    fun autocomplete(texto: String, maxResultados: Int): List<Cliente> {
        return filtrados(texto).take(maxResultados)
    }

    fun getById(id: Int): Cliente? = clientes.firstOrNull { it.id == id }

    private fun filtrados(texto: String): List<Cliente> {
        return clientes.filter {
            it.strNombreCliente.contains(texto, ignoreCase = true) ||
                it.strCorreoElectronico.contains(texto, ignoreCase = true)
        }
    }
}