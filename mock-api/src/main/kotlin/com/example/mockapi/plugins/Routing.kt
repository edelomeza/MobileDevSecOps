package com.example.mockapi.plugins

import com.example.mockapi.data.EmpleadoDatabase
import com.example.mockapi.data.TipoEmpleadoDatabase
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

fun Application.configureRouting(
    usuarioDatabase: UsuarioDatabase,
    empleadoDatabase: EmpleadoDatabase,
    tipoEmpleadoDatabase: TipoEmpleadoDatabase
) {
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
            val totalCount = usuarioDatabase.count()
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val items = usuarioDatabase.list(page, pageSize)

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
            val usuario = usuarioDatabase.create(request.strNombre, request.strCorreoElectronico)
            call.respond(status = HttpStatusCode.Created, message = usuario.toDto())
        }

        put("/api/v1/Usuario/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val request = call.receive<UserUpdateRequest>()
            val updated = usuarioDatabase.update(id, request)
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
            if (usuarioDatabase.delete(id, request.rowVersion)) {
                call.respond(LogoutResponse(message = "Usuario eliminado correctamente"))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuario no encontrado"))
            }
        }

        get("/api/v1/TipoEmpleado") {
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 50
            val totalCount = tipoEmpleadoDatabase.count()
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val items = tipoEmpleadoDatabase.list(page, pageSize)

            call.respond(
                EmpCatTipoEmpleadoListResponse(
                    items = items.map { it.toDto() },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/api/v1/TipoEmpleado/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val tipo = tipoEmpleadoDatabase.getById(id)
            if (tipo != null) {
                call.respond(tipo.toDto())
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Tipo de empleado no encontrado"))
            }
        }

        get("/api/v1/Empleado") {
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = empleadoDatabase.count()
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val items = empleadoDatabase.list(page, pageSize)

            call.respond(
                EmpEmpleadoListResponse(
                    items = items.map { it.toDto() },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/api/v1/Empleado/buscar") {
            val texto = call.request.queryParameters["texto"]
            val idTipoEmpleado = call.request.queryParameters["idTipoEmpleado"]?.toIntOrNull()
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val items = empleadoDatabase.search(texto, idTipoEmpleado, page, pageSize)
            val totalCount = empleadoDatabase.searchCount(texto, idTipoEmpleado)
            val totalPages = (totalCount + pageSize - 1) / pageSize

            call.respond(
                EmpEmpleadoListResponse(
                    items = items.map { it.toDto() },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/api/v1/Empleado/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val emp = empleadoDatabase.getById(id)
            if (emp != null) {
                call.respond(emp.toDto())
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Empleado no encontrado"))
            }
        }

        post("/api/v1/Empleado") {
            val request = call.receive<EmpEmpleadoCreateRequest>()
            val emp = empleadoDatabase.create(
                request.strNombre,
                request.strAPaterno,
                request.strAMaterno,
                request.strCURP,
                request.idEmpCatTipoEmpleado
            )
            call.respond(status = HttpStatusCode.Created, message = emp.toDto())
        }

        put("/api/v1/Empleado/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val request = call.receive<EmpEmpleadoUpdateRequest>()
            val updated = empleadoDatabase.update(
                id, request.strNombre, request.strAPaterno,
                request.strAMaterno, request.strCURP,
                request.idEmpCatTipoEmpleado, request.rowVersion
            )
            if (updated != null) {
                call.respond(status = HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Empleado no encontrado"))
            }
        }

        delete("/api/v1/Empleado/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val request = call.receive<EmpEmpleadoDeleteRequest>()
            if (empleadoDatabase.delete(id, request.rowVersion)) {
                call.respond(LogoutResponse(message = "Empleado eliminado correctamente"))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Empleado no encontrado"))
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
