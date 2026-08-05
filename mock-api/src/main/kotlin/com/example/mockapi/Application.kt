package com.example.mockapi

import com.example.mockapi.data.ClienteDatabase
import com.example.mockapi.data.EmpleadoDatabase
import com.example.mockapi.data.ProductoDatabase
import com.example.mockapi.data.TipoEmpleadoDatabase
import com.example.mockapi.data.UsuarioDatabase
import com.example.mockapi.data.VentaDatabase
import com.example.mockapi.plugins.configureRouting
import com.example.mockapi.plugins.configureSerialization
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val usuarioDatabase = UsuarioDatabase()
    val empleadoDatabase = EmpleadoDatabase()
    val tipoEmpleadoDatabase = TipoEmpleadoDatabase()
    val productoDatabase = ProductoDatabase()
    val clienteDatabase = ClienteDatabase()
    val ventaDatabase = VentaDatabase(clienteDatabase, productoDatabase)

    embeddedServer(Netty, port = 8080) {
        configureSerialization()
        configureRouting(
            usuarioDatabase,
            empleadoDatabase,
            tipoEmpleadoDatabase,
            productoDatabase,
            clienteDatabase,
            ventaDatabase
        )
    }.start(wait = true)
}
