package com.example.mobiledevsecops.ui.index

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.mobiledevsecops.domain.usecase.LogoutUseCase
import com.example.mobiledevsecops.shared.fake.FakeAuthRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class IndexScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeAuthRepository()
    private lateinit var viewModel: IndexViewModel

    @Before
    fun setUp() {
        val logoutUseCase = LogoutUseCase(fakeRepo)
        viewModel = IndexViewModel(logoutUseCase)
    }

    @Test
    fun index_screen_muestra_bienvenida() {
        composeTestRule.setContent {
            IndexScreen(
                onSessionExpired = { },
                onNavigateToUsuario = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Inicio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bienvenido").assertIsDisplayed()
        composeTestRule.onNodeWithText("Has iniciado sesión correctamente").assertIsDisplayed()
    }

    @Test
    fun index_screen_menu_muestra_opciones() {
        composeTestRule.setContent {
            IndexScreen(
                onSessionExpired = { },
                onNavigateToUsuario = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()

        composeTestRule.onNodeWithText("MobileDevSecOps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Menú de navegación").assertIsDisplayed()
        composeTestRule.onNodeWithText("Usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cerrar").assertIsDisplayed()
    }
}
