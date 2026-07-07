package com.example.mobiledevsecops.di

import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.data.remote.AuthApi
import com.example.mobiledevsecops.data.remote.UsuarioApi
import com.example.mobiledevsecops.data.repository.AuthRepositoryImpl
import com.example.mobiledevsecops.data.repository.UsuarioRepositoryImpl
import com.example.mobiledevsecops.domain.repository.AuthRepository
import com.example.mobiledevsecops.domain.repository.UsuarioRepository
import com.example.mobiledevsecops.domain.usecase.ActualizarUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.BuscarUsuariosUseCase
import com.example.mobiledevsecops.domain.usecase.CrearUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.EliminarUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.LoginUseCase
import com.example.mobiledevsecops.domain.usecase.LogoutUseCase
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
    factory { BuscarUsuariosUseCase(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { IndexViewModel(get()) }
    viewModel { UsuarioViewModel(get(), get()) }
    viewModel { UsuarioCrearViewModel(get()) }
    viewModel { (id: Int, nombre: String, correo: String, rowVersion: String) ->
        UsuarioActualizarViewModel(get(), id, nombre, correo, rowVersion)
    }
    viewModel { (id: Int, nombre: String, correo: String, rowVersion: String) ->
        UsuarioEliminarViewModel(get(), id, nombre, correo, rowVersion)
    }
}
