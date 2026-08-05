package com.example.mockapi.plugins

import com.example.mockapi.data.ClienteDatabase
import com.example.mockapi.data.EmpleadoDatabase
import com.example.mockapi.data.EstadoVentaCatalog
import com.example.mockapi.data.ProductoDatabase
import com.example.mockapi.data.ProductoDeleteResult
import com.example.mockapi.data.ProductoUpdateResult
import com.example.mockapi.data.TipoEmpleadoDatabase
import com.example.mockapi.data.UsuarioDatabase
import com.example.mockapi.data.VentaDatabase
import com.example.mockapi.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.CORS
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
    tipoEmpleadoDatabase: TipoEmpleadoDatabase,
    productoDatabase: ProductoDatabase,
    clienteDatabase: ClienteDatabase,
    ventaDatabase: VentaDatabase
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

        get("/api/v1/Usuario/buscar") {
            val texto = call.request.queryParameters["texto"] ?: ""
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = usuarioDatabase.countSearch(texto)
            val totalPages = if (totalCount == 0) 1 else (totalCount + pageSize - 1) / pageSize
            val items = usuarioDatabase.buscar(texto, page, pageSize)

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
                call.respond(HttpStatusCode.NoContent)
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

        get("/api/v1/Producto") {
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = productoDatabase.count()
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val items = productoDatabase.list(page, pageSize)

            call.respond(
                ProductoListResponse(
                    items = items.map { it.toDto() },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/api/v1/Producto/buscar") {
            val texto = call.request.queryParameters["texto"] ?: ""
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = productoDatabase.countSearch(texto)
            val totalPages = if (totalCount == 0) 1 else (totalCount + pageSize - 1) / pageSize
            val items = productoDatabase.buscar(texto, page, pageSize)

            call.respond(
                ProductoListResponse(
                    items = items.map { it.toDto() },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        post("/api/v1/Producto") {
            val request = call.receive<ProductCreateRequest>()
            val producto = productoDatabase.create(
                request.strNombreProducto,
                request.strURLImagen,
                request.strDescripcion,
                request.intNumeroExistencia,
                request.decPrecio
            )
            call.respond(status = HttpStatusCode.Created, message = producto.toDto())
        }

        put("/api/v1/Producto/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val request = call.receive<ProductUpdateRequest>()
            when (val result = productoDatabase.update(id, request)) {
                is ProductoUpdateResult.Success -> call.respond(result.producto.toDto())
                is ProductoUpdateResult.Conflict -> call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("El registro ha sido modificado por otro usuario")
                )
                is ProductoUpdateResult.NotFound -> call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("Producto no encontrado")
                )
            }
        }

        delete("/api/v1/Producto/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val request = call.receive<ProductDeleteRequest>()
            when (productoDatabase.delete(id, request.rowVersion)) {
                is ProductoDeleteResult.Deleted -> call.respond(
                    LogoutResponse(message = "Producto eliminado correctamente")
                )
                is ProductoDeleteResult.Conflict -> call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("El registro ha sido modificado por otro usuario")
                )
                is ProductoDeleteResult.NotFound -> call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("Producto no encontrado")
                )
            }
        }

        get("/api/v1/Cliente") {
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = clienteDatabase.count()
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val items = clienteDatabase.list(page, pageSize)

            call.respond(
                ClienteListResponse(
                    items = items.map { it.toDto() },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/api/v1/Cliente/buscar") {
            val texto = call.request.queryParameters["texto"] ?: ""
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = clienteDatabase.countSearch(texto)
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val items = clienteDatabase.buscar(texto, page, pageSize)

            call.respond(
                ClienteListResponse(
                    items = items.map { it.toDto() },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/api/v1/Cliente/autocomplete") {
            val texto = call.request.queryParameters["texto"] ?: ""
            val maxResultados = call.request.queryParameters["maxResultados"]?.toIntOrNull() ?: 10
            val items = clienteDatabase.autocomplete(texto, maxResultados)

            call.respond(
                ClienteListResponse(
                    items = items.map { it.toDto() },
                    totalCount = items.size,
                    pageNumber = 1,
                    pageSize = items.size,
                    totalPages = 1
                )
            )
        }

        get("/api/v1/EstadoVenta") {
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 50
            val estados = EstadoVentaCatalog.estados
            val totalCount = estados.size
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val items = estados.drop((page - 1) * pageSize).take(pageSize)

            call.respond(
                VenCatEstadoListResponse(
                    items = items.map {
                        VenCatEstadoDto(id = it.id, strValor = it.strValor, strDescripcion = it.strDescripcion)
                    },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/api/v1/Venta") {
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = ventaDatabase.count()
            val totalPages = (totalCount + pageSize - 1) / pageSize
            val items = ventaDatabase.list(page, pageSize)

            call.respond(
                VentaListResponse(
                    items = items.map { ventaDatabase.toDto(it) },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/api/v1/Venta/buscar") {
            val strClaveVenta = call.request.queryParameters["strClaveVenta"]
            val strNombreCliente = call.request.queryParameters["strNombreCliente"]
            val dteFechaInicio = call.request.queryParameters["dteFechaInicio"]
            val dteFechaFin = call.request.queryParameters["dteFechaFin"]
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 8
            val totalCount = ventaDatabase.countSearch(
                strClaveVenta, strNombreCliente, dteFechaInicio, dteFechaFin
            )
            val totalPages = if (totalCount == 0) 1 else (totalCount + pageSize - 1) / pageSize
            val items = ventaDatabase.buscar(
                strClaveVenta, strNombreCliente, dteFechaInicio, dteFechaFin, page, pageSize
            )

            call.respond(
                VentaListResponse(
                    items = items.map { ventaDatabase.toDto(it) },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        post("/api/v1/Venta") {
            val request = call.receive<VentaCreateRequest>()
            val venta = ventaDatabase.create(
                request.idCliCliente,
                request.idSegUsuario,
                request.dteFechaHoraCompra,
                request.strClaveVenta
            )
            call.respond(status = HttpStatusCode.Created, message = ventaDatabase.toDto(venta))
        }

        put("/api/v1/Venta/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val request = call.receive<VentaUpdateRequest>()
            val success = ventaDatabase.updateEstado(id, request.idVenCatEstado, request.rowVersion)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Venta no encontrada"))
            }
        }

        get("/api/v1/VentaDetalle") {
            val idVenVenta = call.request.queryParameters["idVenVenta"]?.toIntOrNull()
            val page = call.request.queryParameters["PageNumber"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["PageSize"]?.toIntOrNull() ?: 1000

            val allDetalles = if (idVenVenta != null) {
                ventaDatabase.listDetallesByVentaId(idVenVenta)
            } else {
                emptyList()
            }
            val totalCount = allDetalles.size
            val totalPages = if (totalCount == 0) 0 else (totalCount + pageSize - 1) / pageSize
            val from = (page - 1) * pageSize
            val items = allDetalles.drop(from).take(pageSize)

            call.respond(
                VentaDetalleListResponse(
                    items = items.map { ventaDatabase.toDetalleDto(it) },
                    totalCount = totalCount,
                    pageNumber = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        post("/api/v1/VentaDetalle") {
            val request = call.receive<VentaDetalleCreateRequest>()
            val detalle = ventaDatabase.createDetalle(
                request.idVenVenta,
                request.idProProducto,
                request.intPiezaVenta
            )
            if (detalle != null) {
                call.respond(status = HttpStatusCode.Created, message = ventaDatabase.toDetalleDto(detalle))
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("La venta o producto especificado no existe")
                )
            }
        }

        get("/api/v1/VentaDetalle/autocomplete") {
            val texto = call.request.queryParameters["texto"] ?: ""
            val maxResultados = call.request.queryParameters["maxResultados"]?.toIntOrNull() ?: 10
            val items = ventaDatabase.autocompleteProductos(texto, maxResultados)
            call.respond(items)
        }

        delete("/api/v1/VentaDetalle/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID invalido"))
            val success = ventaDatabase.deleteDetalle(id)
            if (success) {
                call.respond(LogoutResponse(message = "Detalle eliminado correctamente"))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Detalle no encontrado"))
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
