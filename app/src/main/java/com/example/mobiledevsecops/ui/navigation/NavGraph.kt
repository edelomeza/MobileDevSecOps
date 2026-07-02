package com.example.mobiledevsecops.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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

    fun navToUsuario(page: Int = 1) = "usuario/$page"
    fun navToActualizar(id: Int) = "usuario/actualizar/$id"
    fun navToEliminar(id: Int) = "usuario/eliminar/$id"
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
    }
}
