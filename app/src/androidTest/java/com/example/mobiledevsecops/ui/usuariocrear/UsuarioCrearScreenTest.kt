package com.example.mobiledevsecops.ui.usuariocrear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.mobiledevsecops.domain.usecase.CrearUsuarioUseCase
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsuarioCrearScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeUsuarioRepository()
    private lateinit var viewModel: UsuarioCrearViewModel

    @Before
    fun setUp() {
        val useCase = CrearUsuarioUseCase(fakeRepo)
        viewModel = UsuarioCrearViewModel(useCase)
    }

    @Test
    fun crear_usuario_screen_muestra_campos() {
        composeTestRule.setContent {
            UsuarioCrearScreen(
                onNavigateBack = { },
                onUsuarioCreado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Crear Usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Correo Electrónico").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar").assertIsDisplayed()
    }

    @Test
    fun crear_usuario_con_campos_vacios_muestra_errores() {
        composeTestRule.setContent {
            UsuarioCrearScreen(
                onNavigateBack = { },
                onUsuarioCreado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Guardar").performClick()

        composeTestRule.onNodeWithText("El nombre es obligatorio").assertIsDisplayed()
        composeTestRule.onNodeWithText("La contraseña es obligatoria").assertIsDisplayed()
        composeTestRule.onNodeWithText("El correo es obligatorio").assertIsDisplayed()
    }

    @Test
    fun crear_usuario_con_correo_invalido_muestra_error() {
        composeTestRule.setContent {
            UsuarioCrearScreen(
                onNavigateBack = { },
                onUsuarioCreado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Nombre").performTextInput("Juan Pérez")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
        composeTestRule.onNodeWithText("Correo Electrónico").performTextInput("correo-invalido")
        composeTestRule.onNodeWithText("Guardar").performClick()

        composeTestRule.onNodeWithText("Formato de correo inválido").assertIsDisplayed()
    }
}
