package com.example.mobiledevsecops.di

import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.data.remote.AuthApi
import com.example.mobiledevsecops.data.remote.EmpleadoApi
import com.example.mobiledevsecops.data.remote.UsuarioApi
import com.example.mobiledevsecops.data.repository.AuthRepositoryImpl
import com.example.mobiledevsecops.data.repository.EmpleadoRepositoryImpl
import com.example.mobiledevsecops.data.repository.UsuarioRepositoryImpl
import com.example.mobiledevsecops.domain.repository.AuthRepository
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository
import com.example.mobiledevsecops.domain.repository.UsuarioRepository
import com.example.mobiledevsecops.domain.usecase.ActualizarEmpleadoUseCase
import com.example.mobiledevsecops.domain.usecase.ActualizarUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.CrearEmpleadoUseCase
import com.example.mobiledevsecops.domain.usecase.CrearUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.EliminarEmpleadoUseCase
import com.example.mobiledevsecops.domain.usecase.EliminarUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.LoginUseCase
import com.example.mobiledevsecops.domain.usecase.LogoutUseCase
import com.example.mobiledevsecops.ui.empleado.EmpleadoViewModel
import com.example.mobiledevsecops.ui.empleadoactualizar.EmpleadoActualizarParams
import com.example.mobiledevsecops.ui.empleadoactualizar.EmpleadoActualizarViewModel
import com.example.mobiledevsecops.ui.empleadocrear.EmpleadoCrearViewModel
import com.example.mobiledevsecops.ui.empleadoeliminar.EmpleadoEliminarParams
import com.example.mobiledevsecops.ui.empleadoeliminar.EmpleadoEliminarViewModel
import com.example.mobiledevsecops.ui.index.IndexViewModel
import com.example.mobiledevsecops.ui.login.LoginViewModel
import com.example.mobiledevsecops.ui.usuario.UsuarioViewModel
import com.example.mobiledevsecops.ui.usuarioactualizar.UsuarioActualizarViewModel
import com.example.mobiledevsecops.ui.usuariocrear.UsuarioCrearViewModel
import com.example.mobiledevsecops.ui.usuarioeliminar.UsuarioEliminarViewModel
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { TokenManager(androidContext()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<UsuarioRepository> { UsuarioRepositoryImpl(get()) }
    factory { AuthApi(get()) }
    factory { UsuarioApi(get()) }
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { CrearUsuarioUseCase(get()) }
    factory { ActualizarUsuarioUseCase(get()) }
    factory { EliminarUsuarioUseCase(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { IndexViewModel(get()) }
    viewModel { UsuarioViewModel(get()) }
    viewModel { UsuarioCrearViewModel(get()) }
    viewModel { (id: Int, nombre: String, correo: String, rowVersion: String) ->
        UsuarioActualizarViewModel(get(), id, nombre, correo, rowVersion)
    }
    viewModel { (id: Int, nombre: String, correo: String, rowVersion: String) ->
        UsuarioEliminarViewModel(get(), id, nombre, correo, rowVersion)
    }

    single<EmpleadoRepository> { EmpleadoRepositoryImpl(get()) }
    factory { EmpleadoApi(get()) }
    factory { CrearEmpleadoUseCase(get()) }
    factory { ActualizarEmpleadoUseCase(get()) }
    factory { EliminarEmpleadoUseCase(get()) }
    viewModel { EmpleadoViewModel(get()) }
    viewModel { EmpleadoCrearViewModel(get(), get()) }
    viewModel { params ->
        EmpleadoActualizarViewModel(get(), get(), params[0] as EmpleadoActualizarParams)
    }
    viewModel { params ->
        EmpleadoEliminarViewModel(get(), get(), params[0] as EmpleadoEliminarParams)
    }
}
