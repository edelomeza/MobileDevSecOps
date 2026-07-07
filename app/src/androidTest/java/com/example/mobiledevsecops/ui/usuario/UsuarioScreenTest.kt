package com.example.mobiledevsecops.ui.usuario

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.mobiledevsecops.domain.usecase.BuscarUsuariosUseCase
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsuarioScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeUsuarioRepository()
    private lateinit var buscarUsuariosUseCase: BuscarUsuariosUseCase
    private lateinit var viewModel: UsuarioViewModel

    @Before
    fun setUp() {
        fakeRepo.givenUsuarios(UsuarioFixtures.usuariosMultiPage)
        buscarUsuariosUseCase = BuscarUsuariosUseCase(fakeRepo)
        viewModel = UsuarioViewModel(fakeRepo, buscarUsuariosUseCase)
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
                onNavigateToCreate = { },
                onSessionExpired = { },
                onNavigateToEdit = { _, _, _, _ -> },
                onNavigateToDelete = { _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Página 1 de 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Siguiente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Anterior").assertIsDisplayed()
    }

    @Test
    fun search_bar_es_visible() {
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

        composeTestRule.onNodeWithText("Buscar por nombre").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscar").assertIsDisplayed()
    }

    @Test
    fun buscar_por_nombre_muestra_resultados() {
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

        composeTestRule.onNodeWithText("Buscar por nombre").performTextInput("Usuario 5")
        composeTestRule.onNodeWithText("Buscar").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Total: 1 usuarios"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Total: 1 usuarios").assertIsDisplayed()
    }

    @Test
    fun limpiar_busqueda_restaura_lista() {
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

        composeTestRule.onNodeWithText("Buscar por nombre").performTextInput("Usuario 5")
        composeTestRule.onNodeWithText("Buscar").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Mostrando resultados de búsqueda"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Mostrar todos").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Total: 15 usuarios"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Total: 15 usuarios").assertIsDisplayed()
    }
}
