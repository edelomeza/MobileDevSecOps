package com.example.mobiledevsecops.security.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.domain.usecase.LoginUseCase
import com.example.mobiledevsecops.shared.fake.FakeAuthRepository
import com.example.mobiledevsecops.ui.login.LoginScreen
import com.example.mobiledevsecops.ui.login.LoginViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PasswordVisibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeAuthRepository()
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        tokenManager = TokenManager(context)
        tokenManager.clearAll()
        val loginUseCase = LoginUseCase(fakeRepo)
        viewModel = LoginViewModel(loginUseCase, tokenManager)
    }

    @Test
    fun campo_password_tiene_icono_de_visibilidad() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithContentDescription("Mostrar contraseña").assertIsDisplayed()
    }

    @Test
    fun alternar_visibilidad_cambia_el_icono() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        composeTestRule.onNodeWithContentDescription("Ocultar contraseña").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Ocultar contraseña").performClick()
        composeTestRule.onNodeWithContentDescription("Mostrar contraseña").assertIsDisplayed()
    }

    @Test
    fun campo_password_permite_ingresar_texto() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Contraseña").performTextInput("MiPassword123")
        composeTestRule.onNodeWithText("Ingresar").assertIsDisplayed()
    }
}
