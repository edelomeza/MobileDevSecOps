package com.example.mobiledevsecops.ui.usuario

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.domain.repository.UsuarioRepository
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsuarioScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeUsuarioRepository()
    private lateinit var viewModel: UsuarioViewModel

    @Before
    fun setUp() {
        fakeRepo.givenUsuarios(UsuarioFixtures.usuariosMultiPage)
        viewModel = UsuarioViewModel(fakeRepo)
    }

    @Test
    fun usuario_screen_muestra_titulo_y_botones() {
        composeTestRule.setContent {
            UsuarioScreen(
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToEdit = { _, _, _, _ -> },
                onNavigateToDelete = { _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Usuarios").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total: 15 usuarios").assertIsDisplayed()
    }

    @Test
    fun usuario_screen_muestra_paginacion() {
        composeTestRule.setContent {
            UsuarioScreen(
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToEdit = { _, _, _, _ -> },
                onNavigateToDelete = { _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Página 1 de 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Siguiente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Anterior").assertIsDisplayed()
    }
}
