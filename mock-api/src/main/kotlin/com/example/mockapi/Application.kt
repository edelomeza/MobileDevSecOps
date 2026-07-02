package com.example.mockapi

import com.example.mockapi.data.UsuarioDatabase
import com.example.mockapi.plugins.configureRouting
import com.example.mockapi.plugins.configureSerialization
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val database = UsuarioDatabase()

    embeddedServer(Netty, port = 8080) {
        configureSerialization()
        configureRouting(database)
    }.start(wait = true)
}
