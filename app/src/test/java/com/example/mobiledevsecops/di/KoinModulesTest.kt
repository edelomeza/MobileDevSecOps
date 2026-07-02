package com.example.mobiledevsecops.di

import android.content.Context
import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.data.remote.AuthApi
import com.example.mobiledevsecops.data.remote.UsuarioApi
import com.example.mobiledevsecops.domain.repository.AuthRepository
import com.example.mobiledevsecops.domain.repository.UsuarioRepository
import com.example.mobiledevsecops.domain.usecase.ActualizarUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.CrearUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.EliminarUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.LoginUseCase
import com.example.mobiledevsecops.domain.usecase.LogoutUseCase
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import com.example.mobiledevsecops.ui.index.IndexViewModel
import com.example.mobiledevsecops.ui.login.LoginViewModel
import com.example.mobiledevsecops.ui.usuario.UsuarioViewModel
import com.example.mobiledevsecops.ui.usuariocrear.UsuarioCrearViewModel
import io.ktor.client.HttpClient
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class KoinModulesTest : KoinTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        mockContext = mockk<Context>(relaxed = true)
        every { mockContext.getString(any()) } returns ""
        every { mockContext.getSharedPreferences(any(), any()) } returns mockk(relaxed = true)
        stopKoin()
    }

    @Test
    fun `todos los modulos resuelven todas las dependencias`() {
        val mockTokenManager = mockk<TokenManager>(relaxed = true)
        startKoin {
            allowOverride(true)
            androidContext(mockContext)
            modules(appModule, networkModule, module {
                single<TokenManager> { mockTokenManager }
            })
        }

        assertNotNull(get<AuthRepository>())
        assertNotNull(get<UsuarioRepository>())
        assertNotNull(get<LoginUseCase>())
        assertNotNull(get<LogoutUseCase>())
        assertNotNull(get<CrearUsuarioUseCase>())
        assertNotNull(get<ActualizarUsuarioUseCase>())
        assertNotNull(get<EliminarUsuarioUseCase>())
        assertNotNull(get<AuthApi>())
        assertNotNull(get<UsuarioApi>())
        assertNotNull(get<TokenManager>())
        assertNotNull(get<HttpClient>())

        stopKoin()
    }

    @Test
    fun `todos los viewModels se resuelven`() {
        val mockTokenManager = mockk<TokenManager>(relaxed = true)
        every { mockTokenManager.getLoginAttempts() } returns Pair(0, 0L)
        startKoin {
            allowOverride(true)
            androidContext(mockContext)
            modules(appModule, networkModule, module {
                single<TokenManager> { mockTokenManager }
            })
        }

        assertNotNull(get<LoginViewModel>())
        assertNotNull(get<IndexViewModel>())
        assertNotNull(get<UsuarioViewModel>())
        assertNotNull(get<UsuarioCrearViewModel>())

        stopKoin()
    }

    @Test
    fun `HttpClient se resuelve correctamente`() {
        val mockTokenManager = mockk<TokenManager>(relaxed = true)
        startKoin {
            allowOverride(true)
            androidContext(mockContext)
            modules(appModule, networkModule, module {
                single<TokenManager> { mockTokenManager }
            })
        }

        val client: HttpClient = get()
        assertNotNull(client)

        stopKoin()
    }
}
