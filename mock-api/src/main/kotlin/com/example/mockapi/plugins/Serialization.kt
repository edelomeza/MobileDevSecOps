package com.example.mockapi.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.InternalServerError,
                text = """{"message":"${cause.message ?: "Error interno"}"}"""
            )
        }
    }
}
