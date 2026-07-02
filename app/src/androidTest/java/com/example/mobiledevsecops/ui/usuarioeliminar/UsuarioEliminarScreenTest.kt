package com.example.mobiledevsecops.ui.usuarioeliminar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.domain.usecase.EliminarUsuarioUseCase
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsuarioEliminarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeUsuarioRepository()
    private lateinit var viewModel: UsuarioEliminarViewModel

    @Before
    fun setUp() {
        fakeRepo.givenUsuarios(listOf(UsuarioFixtures.usuario))
        val useCase = EliminarUsuarioUseCase(fakeRepo)
        viewModel = UsuarioEliminarViewModel(
            eliminarUsuarioUseCase = useCase,
            id = 1,
            nombre = "Juan Pérez",
            correo = "juan@example.com",
            rowVersion = "AAAAAAAAB9E="
        )
    }

    @Test
    fun eliminar_usuario_screen_muestra_campos() {
        composeTestRule.setContent {
            UsuarioEliminarScreen(
                id = 1,
                nombre = "Juan Pérez",
                correo = "juan@example.com",
                rowVersion = "AAAAAAAAB9E=",
                onNavigateBack = { },
                onUsuarioEliminado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Eliminar Usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre").assertIsDisplayed()
        composeTestRule.onNodeWithText("Correo Electrónico").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eliminar").assertIsDisplayed()
    }

    @Test
    fun eliminar_usuario_screen_muestra_mensaje_confirmacion() {
        composeTestRule.setContent {
            UsuarioEliminarScreen(
                id = 1,
                nombre = "Juan Pérez",
                correo = "juan@example.com",
                rowVersion = "AAAAAAAAB9E=",
                onNavigateBack = { },
                onUsuarioEliminado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(
            "¿Está seguro de eliminar este usuario? Esta acción no se puede deshacer."
        ).assertIsDisplayed()
    }
}
