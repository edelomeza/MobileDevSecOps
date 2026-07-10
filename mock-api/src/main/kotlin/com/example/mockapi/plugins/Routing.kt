package com.example.mockapi.plugins

import com.example.mockapi.data.UsuarioDatabase
import com.example.mockapi.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ErrorResponse(val message: String)

fun Application.configureRouting(database: UsuarioDatabase) {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    routing {
        post("/api/v1/Login/login") {
            val login = call.receive<LoginRequest>()
            val mockUser = System.getenv("MOCK_USER") ?: "admin"
            val mockPassword = System.getenv("MOCK_PASSWORD") ?: "Admin123!"
            if (login.User == mockUser && login.Password == mockPassword) {
                call.respond(
                    LoginResponse(
                        token = generateMockToken(login.User)
                    )
                )
            } else {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = ErrorResponse("Credenciales invalidas")
                )
            }
        }

        post("/api/v1/Logout/logout") {
            call.respond(LogoutResponse(message = "Sesion cerrada correctamente"))
        }

        get("/api/v1/Usuario") {
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = database.count()
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val items = database.list(page, pageSize)

            call.respond(
                UsuarioListResponse(
                    items = items.map { it.toDto() },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/api/v1/Usuario/buscar") {
            val texto = call.request.queryParameters["texto"] ?: ""
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = database.countSearch(texto)
            val totalPages = if (totalCount == 0) 1 else (totalCount + pageSize - 1) / pageSize
            val items = database.buscar(texto, page, pageSize)

            call.respond(
                UsuarioListResponse(
                    items = items.map { it.toDto() },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        post("/api/v1/Usuario") {
            val request = call.receive<UserCreateRequest>()
            val usuario = database.create(request.strNombre, request.strCorreoElectronico)
            call.respond(status = HttpStatusCode.Created, message = usuario.toDto())
        }

        put("/api/v1/Usuario/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val request = call.receive<UserUpdateRequest>()
            val updated = database.update(id, request)
            if (updated != null) {
                call.respond(updated.toDto())
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuario no encontrado"))
            }
        }

        delete("/api/v1/Usuario/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val request = call.receive<UserDeleteRequest>()
            if (database.delete(id, request.rowVersion)) {
                call.respond(LogoutResponse(message = "Usuario eliminado correctamente"))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuario no encontrado"))
            }
        }
    }
}

private fun generateMockToken(username: String): String {
    val header = buildJsonObject {
        put("alg", JsonPrimitive("HS256"))
        put("typ", JsonPrimitive("JWT"))
    }
    val now = System.currentTimeMillis() / 1000
    val payload = buildJsonObject {
        put("sub", JsonPrimitive("1234567890"))
        put("name", JsonPrimitive(username))
        put("iat", JsonPrimitive(now))
        put("exp", JsonPrimitive(now + 3600))
    }
    val encode = { s: String -> Base64.getUrlEncoder().withoutPadding().encodeToString(s.encodeToByteArray()) }
    val sig = Base64.getUrlEncoder().withoutPadding().encodeToString("mock-sig-$now".encodeToByteArray())
    return "${encode(header.toString())}.${encode(payload.toString())}.$sig"
}
