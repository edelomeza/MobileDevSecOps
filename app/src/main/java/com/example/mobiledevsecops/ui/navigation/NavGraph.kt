package com.example.mobiledevsecops.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mobiledevsecops.ui.cliente.ClienteScreen
import com.example.mobiledevsecops.ui.clienteactualizar.ClienteActualizarParams
import com.example.mobiledevsecops.ui.clienteactualizar.ClienteActualizarScreen
import com.example.mobiledevsecops.ui.clientecrear.ClienteCrearScreen
import com.example.mobiledevsecops.ui.clienteeliminar.ClienteEliminarParams
import com.example.mobiledevsecops.ui.clienteeliminar.ClienteEliminarScreen
import com.example.mobiledevsecops.ui.empleado.EmpleadoScreen
import com.example.mobiledevsecops.ui.empleadoactualizar.EmpleadoActualizarScreen
import com.example.mobiledevsecops.ui.empleadocrear.EmpleadoCrearScreen
import com.example.mobiledevsecops.ui.empleadoeliminar.EmpleadoEliminarScreen
import com.example.mobiledevsecops.ui.index.IndexScreen
import com.example.mobiledevsecops.ui.login.LoginScreen
import com.example.mobiledevsecops.ui.producto.ProductoScreen
import com.example.mobiledevsecops.ui.productoactualizar.ProductoActualizarParams
import com.example.mobiledevsecops.ui.productoactualizar.ProductoActualizarScreen
import com.example.mobiledevsecops.ui.productocrear.ProductoCrearScreen
import com.example.mobiledevsecops.ui.productoeliminar.ProductoEliminarParams
import com.example.mobiledevsecops.ui.productoeliminar.ProductoEliminarScreen
import com.example.mobiledevsecops.ui.usuario.UsuarioScreen
import com.example.mobiledevsecops.ui.usuarioactualizar.UsuarioActualizarScreen
import com.example.mobiledevsecops.ui.usuariocrear.UsuarioCrearScreen
import com.example.mobiledevsecops.ui.usuarioeliminar.UsuarioEliminarScreen
import com.example.mobiledevsecops.ui.venta.VentaScreen
import com.example.mobiledevsecops.ui.ventacrear.VentaCrearScreen
import com.example.mobiledevsecops.ui.ventadetalle.VentaDetalleScreen

object Routes {
    const val LOGIN = "login"
    const val INDEX = "index"
    const val USUARIO = "usuario/{page}"
    const val USUARIO_CREAR = "usuario/crear"
    const val USUARIO_ACTUALIZAR = "usuario/actualizar/{id}"
    const val USUARIO_ELIMINAR = "usuario/eliminar/{id}"
    const val EMPLEADO = "empleado/{page}"
    const val EMPLEADO_CREAR = "empleado/crear"
    const val EMPLEADO_ACTUALIZAR = "empleado/actualizar/{id}/{rowVersion}"
    const val EMPLEADO_ELIMINAR = "empleado/eliminar/{id}/{rowVersion}"
    const val CLIENTE = "cliente/{page}"
    const val CLIENTE_CREAR = "cliente/crear"
    const val CLIENTE_ACTUALIZAR = "cliente/actualizar/{id}"
    const val CLIENTE_ELIMINAR = "cliente/eliminar/{id}"
    const val PRODUCTO = "producto/{page}"
    const val PRODUCTO_CREAR = "producto/crear"
    const val PRODUCTO_ACTUALIZAR = "producto/actualizar/{id}"
    const val PRODUCTO_ELIMINAR = "producto/eliminar/{id}"
    const val VENTA = "venta/{page}"
    const val VENTA_CREAR = "venta/crear"
    const val VENTA_DETALLE = "venta/detalle/{ventaId}/{strClaveVenta}/{dteFechaHoraCompra}/{strNombreCliente}/{strEstado}/{idCliCliente}/{idSegUsuario}/{idVenCatEstado}/{rowVersion}"

    fun navToUsuario(page: Int = 1) = "usuario/$page"
    fun navToActualizar(id: Int) = "usuario/actualizar/$id"
    fun navToEliminar(id: Int) = "usuario/eliminar/$id"
    fun navToEmpleado(page: Int = 1) = "empleado/$page"
    fun navToActualizarEmpleado(id: Int, rowVersion: String) = "empleado/actualizar/$id/${java.net.URLEncoder.encode(rowVersion, "UTF-8")}"
    fun navToEliminarEmpleado(id: Int, rowVersion: String) = "empleado/eliminar/$id/${java.net.URLEncoder.encode(rowVersion, "UTF-8")}"
    fun navToCliente(page: Int = 1) = "cliente/$page"
    fun navToActualizarCliente(id: Int) = "cliente/actualizar/$id"
    fun navToEliminarCliente(id: Int) = "cliente/eliminar/$id"
    fun navToVenta(page: Int = 1) = "venta/$page"
    fun navToVentaDetalle(
        ventaId: Int,
        strClaveVenta: String,
        dteFechaHoraCompra: String,
        strNombreCliente: String,
        strEstado: String,
        idCliCliente: Int,
        idSegUsuario: Int,
        idVenCatEstado: Int,
        rowVersion: String
    ) = "venta/detalle/$ventaId/${java.net.URLEncoder.encode(strClaveVenta, "UTF-8")}" +
        "/${java.net.URLEncoder.encode(dteFechaHoraCompra, "UTF-8")}" +
        "/${java.net.URLEncoder.encode(strNombreCliente, "UTF-8")}" +
        "/${java.net.URLEncoder.encode(strEstado, "UTF-8")}" +
        "/$idCliCliente/$idSegUsuario/$idVenCatEstado" +
        "/${java.net.URLEncoder.encode(rowVersion, "UTF-8")}"
}

object ProductoRoutes {
    fun navToProducto(page: Int = 1) = "producto/$page"
    fun navToActualizarProducto(id: Int) = "producto/actualizar/$id"
    fun navToEliminarProducto(id: Int) = "producto/eliminar/$id"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        loginGraph(navController)
        indexGraph(navController)
        usuarioGraph(navController)
        empleadoGraph(navController)
        clienteListaGraph(navController)
        clienteCrudGraph(navController)
        productoListaGraph(navController)
        productoCrudGraph(navController)
        ventaListaGraph(navController)
        ventaCrudGraph(navController)
        ventaDetalleGraph(navController)
    }
}

private fun NavGraphBuilder.loginGraph(navController: NavHostController) {
    composable(Routes.LOGIN) {
        LoginScreen(
            onLoginSuccess = {
                navController.navigate(Routes.INDEX) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.indexGraph(navController: NavHostController) {
    composable(Routes.INDEX) {
        IndexScreen(
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.INDEX) { inclusive = true }
                }
            },
            onNavigateToUsuario = {
                navController.navigate(Routes.navToUsuario())
            },
            onNavigateToEmpleado = {
                navController.navigate(Routes.navToEmpleado())
            },
            onNavigateToCliente = {
                navController.navigate(Routes.navToCliente())
            },
            onNavigateToProducto = {
                navController.navigate(ProductoRoutes.navToProducto())
            },
            onNavigateToVenta = {
                navController.navigate(Routes.navToVenta())
            }
        )
    }
}

private fun NavGraphBuilder.usuarioGraph(navController: NavHostController) {
    composable(
        route = Routes.USUARIO,
        arguments = listOf(navArgument("page") { type = NavType.IntType })
    ) { backStackEntry ->
        val reloadSignal = backStackEntry.savedStateHandle.get<Boolean>("reloadUsuarios") ?: false
        if (reloadSignal) {
            backStackEntry.savedStateHandle["reloadUsuarios"] = false
        }

        val operationResult = backStackEntry.savedStateHandle.get<String>("operationResult") ?: ""
        if (operationResult.isNotEmpty()) {
            backStackEntry.savedStateHandle["operationResult"] = ""
        }

        UsuarioScreen(
            reloadSignal = reloadSignal,
            operationResult = operationResult,
            onNavigateBack = { navController.popBackStack() },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToCreate = {
                navController.navigate(Routes.USUARIO_CREAR)
            },
            onNavigateToEdit = { id, nombre, correo, rowVersion ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("edit_nombre", nombre)
                    set("edit_correo", correo)
                    set("edit_rowVersion", rowVersion)
                }
                navController.navigate(Routes.navToActualizar(id))
            },
            onNavigateToDelete = { id, nombre, correo, rowVersion ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("delete_nombre", nombre)
                    set("delete_correo", correo)
                    set("delete_rowVersion", rowVersion)
                }
                navController.navigate(Routes.navToEliminar(id))
            }
        )
    }
    composable(Routes.USUARIO_CREAR) {
        UsuarioCrearScreen(
            onNavigateBack = { navController.popBackStack() },
            onUsuarioCreado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadUsuarios", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    composable(
        route = Routes.USUARIO_ACTUALIZAR,
        arguments = listOf(
            navArgument("id") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val prevHandle = navController.previousBackStackEntry?.savedStateHandle
        val nombre = prevHandle?.get<String>("edit_nombre")?.also { prevHandle.remove<String>("edit_nombre") } ?: ""
        val correo = prevHandle?.get<String>("edit_correo")?.also { prevHandle.remove<String>("edit_correo") } ?: ""
        val rowVersion = prevHandle?.get<String>("edit_rowVersion")?.also { prevHandle.remove<String>("edit_rowVersion") } ?: ""
        UsuarioActualizarScreen(
            id = id,
            nombre = nombre,
            correo = correo,
            rowVersion = rowVersion,
            onNavigateBack = { navController.popBackStack() },
            onUsuarioActualizado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadUsuarios", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    composable(
        route = Routes.USUARIO_ELIMINAR,
        arguments = listOf(
            navArgument("id") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val prevHandle = navController.previousBackStackEntry?.savedStateHandle
        val nombre = prevHandle?.get<String>("delete_nombre")?.also { prevHandle.remove<String>("delete_nombre") } ?: ""
        val correo = prevHandle?.get<String>("delete_correo")?.also { prevHandle.remove<String>("delete_correo") } ?: ""
        val rowVersion = prevHandle?.get<String>("delete_rowVersion")?.also { prevHandle.remove<String>("delete_rowVersion") } ?: ""
        UsuarioEliminarScreen(
            id = id,
            nombre = nombre,
            correo = correo,
            rowVersion = rowVersion,
            onNavigateBack = { navController.popBackStack() },
            onUsuarioEliminado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadUsuarios", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.empleadoGraph(navController: NavHostController) {
    composable(
        route = Routes.EMPLEADO,
        arguments = listOf(navArgument("page") { type = NavType.IntType })
    ) { backStackEntry ->
        val reloadSignal = backStackEntry.savedStateHandle.get<Boolean>("reloadEmpleados") ?: false
        if (reloadSignal) {
            backStackEntry.savedStateHandle["reloadEmpleados"] = false
        }

        val operationResult = backStackEntry.savedStateHandle.get<String>("operationResult") ?: ""
        if (operationResult.isNotEmpty()) {
            backStackEntry.savedStateHandle["operationResult"] = ""
        }

        EmpleadoScreen(
            reloadSignal = reloadSignal,
            operationResult = operationResult,
            onNavigateBack = { navController.popBackStack() },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToCreate = {
                navController.navigate(Routes.EMPLEADO_CREAR)
            },
            onNavigateToEdit = { id, rowVersion ->
                navController.navigate(Routes.navToActualizarEmpleado(id, rowVersion))
            },
            onNavigateToDelete = { id, rowVersion ->
                navController.navigate(Routes.navToEliminarEmpleado(id, rowVersion))
            }
        )
    }
    composable(Routes.EMPLEADO_CREAR) {
        EmpleadoCrearScreen(
            onNavigateBack = { navController.popBackStack() },
            onEmpleadoCreado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadEmpleados", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    composable(
        route = Routes.EMPLEADO_ACTUALIZAR,
        arguments = listOf(
            navArgument("id") { type = NavType.IntType },
            navArgument("rowVersion") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val rowVersion = backStackEntry.arguments?.getString("rowVersion") ?: ""
        EmpleadoActualizarScreen(
            id = id,
            rowVersion = rowVersion,
            onNavigateBack = { navController.popBackStack() },
            onEmpleadoActualizado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadEmpleados", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    composable(
        route = Routes.EMPLEADO_ELIMINAR,
        arguments = listOf(
            navArgument("id") { type = NavType.IntType },
            navArgument("rowVersion") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val rowVersion = backStackEntry.arguments?.getString("rowVersion") ?: ""
        EmpleadoEliminarScreen(
            id = id,
            rowVersion = rowVersion,
            onNavigateBack = { navController.popBackStack() },
            onEmpleadoEliminado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadEmpleados", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.clienteListaGraph(navController: NavHostController) {
    composable(
        route = Routes.CLIENTE,
        arguments = listOf(navArgument("page") { type = NavType.IntType })
    ) { backStackEntry ->
        val reloadSignal = backStackEntry.savedStateHandle.get<Boolean>("reloadClientes") ?: false
        if (reloadSignal) {
            backStackEntry.savedStateHandle["reloadClientes"] = false
        }

        val operationResult = backStackEntry.savedStateHandle.get<String>("operationResult") ?: ""
        if (operationResult.isNotEmpty()) {
            backStackEntry.savedStateHandle["operationResult"] = ""
        }

        ClienteScreen(
            reloadSignal = reloadSignal,
            operationResult = operationResult,
            onNavigateBack = { navController.popBackStack() },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToCreate = {
                navController.navigate(Routes.CLIENTE_CREAR)
            },
            onNavigateToEdit = { id, nombreCliente, direccionCliente, correo, telefono, rowVersion ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("edit_nombreCliente", nombreCliente)
                    set("edit_direccionCliente", direccionCliente)
                    set("edit_correo", correo)
                    set("edit_telefono", telefono)
                    set("edit_rowVersion", rowVersion)
                }
                navController.navigate(Routes.navToActualizarCliente(id))
            },
            onNavigateToDelete = { id, nombreCliente, direccionCliente, correo, telefono, rowVersion ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("delete_nombreCliente", nombreCliente)
                    set("delete_direccionCliente", direccionCliente)
                    set("delete_correo", correo)
                    set("delete_telefono", telefono)
                    set("delete_rowVersion", rowVersion)
                }
                navController.navigate(Routes.navToEliminarCliente(id))
            }
        )
    }
}

private fun NavGraphBuilder.clienteCrudGraph(navController: NavHostController) {
    composable(Routes.CLIENTE_CREAR) {
        ClienteCrearScreen(
            onNavigateBack = { navController.popBackStack() },
            onClienteCreado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadClientes", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    composable(
        route = Routes.CLIENTE_ACTUALIZAR,
        arguments = listOf(
            navArgument("id") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val prevHandle = navController.previousBackStackEntry?.savedStateHandle
        val nombreCliente = prevHandle?.get<String>("edit_nombreCliente")?.also { prevHandle.remove<String>("edit_nombreCliente") } ?: ""
        val direccionCliente = prevHandle?.get<String>("edit_direccionCliente")?.also { prevHandle.remove<String>("edit_direccionCliente") }
        val correo = prevHandle?.get<String>("edit_correo")?.also { prevHandle.remove<String>("edit_correo") } ?: ""
        val telefono = prevHandle?.get<String>("edit_telefono")?.also { prevHandle.remove<String>("edit_telefono") } ?: ""
        val rowVersion = prevHandle?.get<String>("edit_rowVersion")?.also { prevHandle.remove<String>("edit_rowVersion") } ?: ""
        ClienteActualizarScreen(
            params = ClienteActualizarParams(
                id = id,
                nombreCliente = nombreCliente,
                direccionCliente = direccionCliente,
                correo = correo,
                telefono = telefono,
                rowVersion = rowVersion
            ),
            onNavigateBack = { navController.popBackStack() },
            onClienteActualizado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadClientes", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    composable(
        route = Routes.CLIENTE_ELIMINAR,
        arguments = listOf(
            navArgument("id") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val prevHandle = navController.previousBackStackEntry?.savedStateHandle
        val nombreCliente = prevHandle?.get<String>("delete_nombreCliente")?.also { prevHandle.remove<String>("delete_nombreCliente") } ?: ""
        val direccionCliente = prevHandle?.get<String>("delete_direccionCliente")?.also { prevHandle.remove<String>("delete_direccionCliente") }
        val correo = prevHandle?.get<String>("delete_correo")?.also { prevHandle.remove<String>("delete_correo") } ?: ""
        val telefono = prevHandle?.get<String>("delete_telefono")?.also { prevHandle.remove<String>("delete_telefono") } ?: ""
        val rowVersion = prevHandle?.get<String>("delete_rowVersion")?.also { prevHandle.remove<String>("delete_rowVersion") } ?: ""
        ClienteEliminarScreen(
            params = ClienteEliminarParams(
                id = id,
                nombreCliente = nombreCliente,
                direccionCliente = direccionCliente,
                correo = correo,
                telefono = telefono,
                rowVersion = rowVersion
            ),
            onNavigateBack = { navController.popBackStack() },
            onClienteEliminado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadClientes", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.productoListaGraph(navController: NavHostController) {
    composable(
        route = Routes.PRODUCTO,
        arguments = listOf(navArgument("page") { type = NavType.IntType })
    ) { backStackEntry ->
        val reloadSignal = backStackEntry.savedStateHandle.get<Boolean>("reloadProductos") ?: false
        if (reloadSignal) {
            backStackEntry.savedStateHandle["reloadProductos"] = false
        }

        val operationResult = backStackEntry.savedStateHandle.get<String>("operationResult") ?: ""
        if (operationResult.isNotEmpty()) {
            backStackEntry.savedStateHandle["operationResult"] = ""
        }

        ProductoScreen(
            reloadSignal = reloadSignal,
            operationResult = operationResult,
            onNavigateBack = { navController.popBackStack() },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToCreate = {
                navController.navigate(Routes.PRODUCTO_CREAR)
            },
            onNavigateToEdit = { id, nombreProducto, urlImagen, descripcion, existencia, precio, rowVersion ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("edit_nombreProducto", nombreProducto)
                    set("edit_urlImagen", urlImagen)
                    set("edit_descripcion", descripcion)
                    set("edit_existencia", existencia)
                    set("edit_precio", precio)
                    set("edit_rowVersion", rowVersion)
                }
                navController.navigate(ProductoRoutes.navToActualizarProducto(id))
            },
            onNavigateToDelete = { id, nombreProducto, urlImagen, descripcion, existencia, precio, rowVersion ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("delete_nombreProducto", nombreProducto)
                    set("delete_urlImagen", urlImagen)
                    set("delete_descripcion", descripcion)
                    set("delete_existencia", existencia)
                    set("delete_precio", precio)
                    set("delete_rowVersion", rowVersion)
                }
                navController.navigate(ProductoRoutes.navToEliminarProducto(id))
            }
        )
    }
}

private fun NavGraphBuilder.productoCrudGraph(navController: NavHostController) {
    composable(Routes.PRODUCTO_CREAR) {
        ProductoCrearScreen(
            onNavigateBack = { navController.popBackStack() },
            onProductoCreado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadProductos", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    composable(
        route = Routes.PRODUCTO_ACTUALIZAR,
        arguments = listOf(
            navArgument("id") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val prevHandle = navController.previousBackStackEntry?.savedStateHandle
        val nombreProducto = prevHandle?.get<String>("edit_nombreProducto")?.also { prevHandle.remove<String>("edit_nombreProducto") } ?: ""
        val urlImagen = prevHandle?.get<String>("edit_urlImagen")?.also { prevHandle.remove<String>("edit_urlImagen") }
        val descripcion = prevHandle?.get<String>("edit_descripcion")?.also { prevHandle.remove<String>("edit_descripcion") }
        val existencia = prevHandle?.get<Int>("edit_existencia")?.also { prevHandle.remove<Int>("edit_existencia") } ?: 0
        val precio = prevHandle?.get<Double>("edit_precio")?.also { prevHandle.remove<Double>("edit_precio") } ?: 0.0
        val rowVersion = prevHandle?.get<String>("edit_rowVersion")?.also { prevHandle.remove<String>("edit_rowVersion") } ?: ""
        ProductoActualizarScreen(
            params = ProductoActualizarParams(
                id = id,
                strNombreProducto = nombreProducto,
                strURLImagen = urlImagen,
                strDescripcion = descripcion,
                intNumeroExistencia = existencia,
                decPrecio = precio,
                rowVersion = rowVersion
            ),
            onNavigateBack = { navController.popBackStack() },
            onProductoActualizado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadProductos", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    composable(
        route = Routes.PRODUCTO_ELIMINAR,
        arguments = listOf(
            navArgument("id") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val prevHandle = navController.previousBackStackEntry?.savedStateHandle
        val nombreProducto = prevHandle?.get<String>("delete_nombreProducto")?.also { prevHandle.remove<String>("delete_nombreProducto") } ?: ""
        val urlImagen = prevHandle?.get<String>("delete_urlImagen")?.also { prevHandle.remove<String>("delete_urlImagen") }
        val descripcion = prevHandle?.get<String>("delete_descripcion")?.also { prevHandle.remove<String>("delete_descripcion") }
        val existencia = prevHandle?.get<Int>("delete_existencia")?.also { prevHandle.remove<Int>("delete_existencia") } ?: 0
        val precio = prevHandle?.get<Double>("delete_precio")?.also { prevHandle.remove<Double>("delete_precio") } ?: 0.0
        val rowVersion = prevHandle?.get<String>("delete_rowVersion")?.also { prevHandle.remove<String>("delete_rowVersion") } ?: ""
        ProductoEliminarScreen(
            params = ProductoEliminarParams(
                id = id,
                strNombreProducto = nombreProducto,
                strURLImagen = urlImagen,
                strDescripcion = descripcion,
                intNumeroExistencia = existencia,
                decPrecio = precio,
                rowVersion = rowVersion
            ),
            onNavigateBack = { navController.popBackStack() },
            onProductoEliminado = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadProductos", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.ventaListaGraph(navController: NavHostController) {
    composable(
        route = Routes.VENTA,
        arguments = listOf(navArgument("page") { type = NavType.IntType })
    ) { backStackEntry ->
        val reloadSignal = backStackEntry.savedStateHandle.get<Boolean>("reloadVentas") ?: false
        if (reloadSignal) {
            backStackEntry.savedStateHandle["reloadVentas"] = false
        }

        val operationResult = backStackEntry.savedStateHandle.get<String>("operationResult") ?: ""
        if (operationResult.isNotEmpty()) {
            backStackEntry.savedStateHandle["operationResult"] = ""
        }

        VentaScreen(
            reloadSignal = reloadSignal,
            operationResult = operationResult,
            onNavigateBack = { navController.popBackStack() },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToCreate = {
                navController.navigate(Routes.VENTA_CREAR)
            },
            onNavigateToDetalle = { ventaId, strClaveVenta, dteFechaHoraCompra, strNombreCliente, strEstado, idCliCliente, idSegUsuario, idVenCatEstado, rowVersion ->
                navController.navigate(
                    Routes.navToVentaDetalle(
                        ventaId, strClaveVenta, dteFechaHoraCompra, strNombreCliente,
                        strEstado, idCliCliente, idSegUsuario, idVenCatEstado, rowVersion
                    )
                )
            }
        )
    }
}

private fun NavGraphBuilder.ventaCrudGraph(navController: NavHostController) {
    composable(Routes.VENTA_CREAR) {
        VentaCrearScreen(
            onNavigateBack = { navController.popBackStack() },
            onVentaCreada = {
                navController.previousBackStackEntry?.savedStateHandle?.set("reloadVentas", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "success")
                navController.popBackStack()
            },
            onError = {
                navController.previousBackStackEntry?.savedStateHandle?.set("operationResult", "error")
                navController.popBackStack()
            },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.ventaDetalleGraph(navController: NavHostController) {
    composable(
        route = Routes.VENTA_DETALLE,
        arguments = listOf(
            navArgument("ventaId") { type = NavType.IntType },
            navArgument("strClaveVenta") { type = NavType.StringType },
            navArgument("dteFechaHoraCompra") { type = NavType.StringType },
            navArgument("strNombreCliente") { type = NavType.StringType },
            navArgument("strEstado") { type = NavType.StringType },
            navArgument("idCliCliente") { type = NavType.IntType },
            navArgument("idSegUsuario") { type = NavType.IntType },
            navArgument("idVenCatEstado") { type = NavType.IntType },
            navArgument("rowVersion") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        VentaDetalleScreen(
            onNavigateBack = { navController.popBackStack() },
            onSessionExpired = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            },
            ventaId = backStackEntry.arguments?.getInt("ventaId") ?: 0,
            strClaveVenta = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("strClaveVenta") ?: "", "UTF-8"
            ),
            dteFechaHoraCompra = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("dteFechaHoraCompra") ?: "", "UTF-8"
            ),
            strNombreCliente = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("strNombreCliente") ?: "", "UTF-8"
            ),
            strEstado = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("strEstado") ?: "", "UTF-8"
            ),
            idCliCliente = backStackEntry.arguments?.getInt("idCliCliente") ?: 0,
            idSegUsuario = backStackEntry.arguments?.getInt("idSegUsuario") ?: 0,
            idVenCatEstado = backStackEntry.arguments?.getInt("idVenCatEstado") ?: 0,
            rowVersion = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("rowVersion") ?: "", "UTF-8"
            )
        )
    }
}
