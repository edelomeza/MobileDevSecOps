package com.example.mobiledevsecops.ui.login

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.domain.usecase.LoginUseCase
import com.example.mobiledevsecops.shared.fake.FakeAuthRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

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
    fun login_screen_muestra_titulo_y_campos() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
        composeTestRule.onNodeWithText("Usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ingresar").assertIsDisplayed()
    }

    @Test
    fun login_con_campos_vacios_muestra_errores() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Ingresar").performClick()

        composeTestRule.onNodeWithText("El usuario no puede estar vacío").assertIsDisplayed()
        composeTestRule.onNodeWithText("La contraseña no puede estar vacía").assertIsDisplayed()
    }

    @Test
    fun login_con_error_muestra_snackbar() {
        fakeRepo.shouldFail = true

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Usuario").performTextInput("admin")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("wrong")
        composeTestRule.onNodeWithText("Ingresar").performClick()

        composeTestRule.onNodeWithText("Credenciales inválidas", substring = true).assertIsDisplayed()
    }
}
