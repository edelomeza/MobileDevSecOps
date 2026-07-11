package com.example.mobiledevsecops.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mobiledevsecops.ui.empleado.EmpleadoScreen
import com.example.mobiledevsecops.ui.empleadoactualizar.EmpleadoActualizarScreen
import com.example.mobiledevsecops.ui.empleadocrear.EmpleadoCrearScreen
import com.example.mobiledevsecops.ui.empleadoeliminar.EmpleadoEliminarScreen
import com.example.mobiledevsecops.ui.index.IndexScreen
import com.example.mobiledevsecops.ui.login.LoginScreen
import com.example.mobiledevsecops.ui.usuario.UsuarioScreen
import com.example.mobiledevsecops.ui.usuarioactualizar.UsuarioActualizarScreen
import com.example.mobiledevsecops.ui.usuariocrear.UsuarioCrearScreen
import com.example.mobiledevsecops.ui.usuarioeliminar.UsuarioEliminarScreen

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

    fun navToUsuario(page: Int = 1) = "usuario/$page"
    fun navToActualizar(id: Int) = "usuario/actualizar/$id"
    fun navToEliminar(id: Int) = "usuario/eliminar/$id"
    fun navToEmpleado(page: Int = 1) = "empleado/$page"
    fun navToActualizarEmpleado(id: Int, rowVersion: String) = "empleado/actualizar/$id/${java.net.URLEncoder.encode(rowVersion, "UTF-8")}"
    fun navToEliminarEmpleado(id: Int, rowVersion: String) = "empleado/eliminar/$id/${java.net.URLEncoder.encode(rowVersion, "UTF-8")}"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.INDEX) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
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
                }
            )
        }
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
}
