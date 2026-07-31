package com.example.mobiledevsecops.di

import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.data.remote.AuthApi
import com.example.mobiledevsecops.data.remote.ClienteApi
import com.example.mobiledevsecops.data.remote.EmpleadoApi
import com.example.mobiledevsecops.data.remote.ProductoApi
import com.example.mobiledevsecops.data.remote.UsuarioApi
import com.example.mobiledevsecops.data.repository.AuthRepositoryImpl
import com.example.mobiledevsecops.data.repository.ClienteRepositoryImpl
import com.example.mobiledevsecops.data.repository.EmpleadoRepositoryImpl
import com.example.mobiledevsecops.data.repository.ProductoRepositoryImpl
import com.example.mobiledevsecops.data.repository.UsuarioRepositoryImpl
import com.example.mobiledevsecops.domain.repository.AuthRepository
import com.example.mobiledevsecops.domain.repository.ClienteRepository
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository
import com.example.mobiledevsecops.domain.repository.ProductoRepository
import com.example.mobiledevsecops.domain.repository.UsuarioRepository
import com.example.mobiledevsecops.domain.usecase.ActualizarClienteUseCase
import com.example.mobiledevsecops.domain.usecase.ActualizarEmpleadoUseCase
import com.example.mobiledevsecops.domain.usecase.ActualizarProductoUseCase
import com.example.mobiledevsecops.domain.usecase.ActualizarUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.BuscarClientesUseCase
import com.example.mobiledevsecops.domain.usecase.BuscarProductosUseCase
import com.example.mobiledevsecops.domain.usecase.BuscarUsuariosUseCase
import com.example.mobiledevsecops.domain.usecase.CrearClienteUseCase
import com.example.mobiledevsecops.domain.usecase.CrearEmpleadoUseCase
import com.example.mobiledevsecops.domain.usecase.CrearProductoUseCase
import com.example.mobiledevsecops.domain.usecase.CrearUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.EliminarClienteUseCase
import com.example.mobiledevsecops.domain.usecase.EliminarEmpleadoUseCase
import com.example.mobiledevsecops.domain.usecase.EliminarProductoUseCase
import com.example.mobiledevsecops.domain.usecase.EliminarUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.LoginUseCase
import com.example.mobiledevsecops.domain.usecase.LogoutUseCase
import com.example.mobiledevsecops.ui.cliente.ClienteViewModel
import com.example.mobiledevsecops.ui.clienteactualizar.ClienteActualizarParams
import com.example.mobiledevsecops.ui.clienteactualizar.ClienteActualizarViewModel
import com.example.mobiledevsecops.ui.clientecrear.ClienteCrearViewModel
import com.example.mobiledevsecops.ui.clienteeliminar.ClienteEliminarParams
import com.example.mobiledevsecops.ui.clienteeliminar.ClienteEliminarViewModel
import com.example.mobiledevsecops.ui.empleado.EmpleadoViewModel
import com.example.mobiledevsecops.ui.empleadoactualizar.EmpleadoActualizarParams
import com.example.mobiledevsecops.ui.empleadoactualizar.EmpleadoActualizarViewModel
import com.example.mobiledevsecops.ui.empleadocrear.EmpleadoCrearViewModel
import com.example.mobiledevsecops.ui.empleadoeliminar.EmpleadoEliminarParams
import com.example.mobiledevsecops.ui.empleadoeliminar.EmpleadoEliminarViewModel
import com.example.mobiledevsecops.ui.index.IndexViewModel
import com.example.mobiledevsecops.ui.login.LoginViewModel
import com.example.mobiledevsecops.ui.producto.ProductoViewModel
import com.example.mobiledevsecops.ui.productoactualizar.ProductoActualizarParams
import com.example.mobiledevsecops.ui.productoactualizar.ProductoActualizarViewModel
import com.example.mobiledevsecops.ui.productocrear.ProductoCrearViewModel
import com.example.mobiledevsecops.ui.productoeliminar.ProductoEliminarParams
import com.example.mobiledevsecops.ui.productoeliminar.ProductoEliminarViewModel
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

    single<ClienteRepository> { ClienteRepositoryImpl(get()) }
    factory { ClienteApi(get()) }
    factory { BuscarClientesUseCase(get()) }
    factory { CrearClienteUseCase(get()) }
    factory { ActualizarClienteUseCase(get()) }
    factory { EliminarClienteUseCase(get()) }
    viewModel { ClienteViewModel(get(), get()) }
    viewModel { ClienteCrearViewModel(get()) }
    viewModel { params ->
        ClienteActualizarViewModel(get(), params[0] as ClienteActualizarParams)
    }
    viewModel { params ->
        ClienteEliminarViewModel(get(), params[0] as ClienteEliminarParams)
    }

    single<ProductoRepository> { ProductoRepositoryImpl(get()) }
    factory { ProductoApi(get()) }
    factory { BuscarProductosUseCase(get()) }
    factory { CrearProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
    viewModel { ProductoViewModel(get(), get()) }
    viewModel { ProductoCrearViewModel(get()) }
    viewModel { params ->
        ProductoActualizarViewModel(get(), params[0] as ProductoActualizarParams)
    }
    viewModel { params ->
        ProductoEliminarViewModel(get(), params[0] as ProductoEliminarParams)
    }
}
