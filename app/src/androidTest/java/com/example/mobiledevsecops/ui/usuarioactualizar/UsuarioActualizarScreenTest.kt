package com.example.mobiledevsecops.ui.usuarioactualizar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.mobiledevsecops.domain.usecase.ActualizarUsuarioUseCase
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsuarioActualizarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeUsuarioRepository()
    private lateinit var viewModel: UsuarioActualizarViewModel

    @Before
    fun setUp() {
        fakeRepo.givenUsuarios(listOf(UsuarioFixtures.usuario))
        val useCase = ActualizarUsuarioUseCase(fakeRepo)
        viewModel = UsuarioActualizarViewModel(
            actualizarUsuarioUseCase = useCase,
            id = 1,
            nombre = "Juan Pérez",
            correo = "juan@example.com",
            rowVersion = "AAAAAAAAB9E="
        )
    }

    @Test
    fun actualizar_usuario_screen_muestra_campos() {
        composeTestRule.setContent {
            UsuarioActualizarScreen(
                id = 1,
                nombre = "Juan Pérez",
                correo = "juan@example.com",
                rowVersion = "AAAAAAAAB9E=",
                onNavigateBack = { },
                onUsuarioActualizado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Actualizar Usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Correo Electrónico").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Actualizar").assertIsDisplayed()
    }

    @Test
    fun actualizar_usuario_con_nombre_vacio_muestra_error() {
        composeTestRule.setContent {
            UsuarioActualizarScreen(
                id = 1,
                nombre = "Juan Pérez",
                correo = "juan@example.com",
                rowVersion = "AAAAAAAAB9E=",
                onNavigateBack = { },
                onUsuarioActualizado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Nombre").performTextClearance()
        composeTestRule.onNodeWithText("Actualizar").performClick()

        composeTestRule.onNodeWithText("El nombre es obligatorio").assertIsDisplayed()
    }

    @Test
    fun actualizar_usuario_con_correo_invalido_muestra_error() {
        composeTestRule.setContent {
            UsuarioActualizarScreen(
                id = 1,
                nombre = "Juan Pérez",
                correo = "juan@example.com",
                rowVersion = "AAAAAAAAB9E=",
                onNavigateBack = { },
                onUsuarioActualizado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Nombre").performTextClearance()
        composeTestRule.onNodeWithText("Nombre").performTextInput("Nuevo Nombre")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
        composeTestRule.onNodeWithText("Correo Electrónico").performTextClearance()
        composeTestRule.onNodeWithText("Correo Electrónico").performTextInput("correo-invalido")
        composeTestRule.onNodeWithText("Actualizar").performClick()

        composeTestRule.onNodeWithText("Formato de correo inválido").assertIsDisplayed()
    }
}
