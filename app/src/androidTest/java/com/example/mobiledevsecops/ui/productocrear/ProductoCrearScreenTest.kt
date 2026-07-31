package com.example.mobiledevsecops.ui.productocrear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.domain.usecase.CrearProductoUseCase
import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import org.junit.Rule
import org.junit.Test

class ProductoCrearScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeProductoRepository()
    private val useCase = CrearProductoUseCase(fakeRepo)
    private val viewModel = ProductoCrearViewModel(useCase)

    @Test
    fun producto_crear_screen_muestra_campos() {
        composeTestRule.setContent {
            ProductoCrearScreen(
                onNavigateBack = { },
                onProductoCreado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Crear Producto").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre del Producto").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar").assertIsDisplayed()
    }

    @Test
    fun producto_crear_screen_muestra_campos_opcionales() {
        composeTestRule.setContent {
            ProductoCrearScreen(
                onNavigateBack = { },
                onProductoCreado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("URL de Imagen (opcional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Descripción (opcional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Existencia").assertIsDisplayed()
        composeTestRule.onNodeWithText("Precio").assertIsDisplayed()
    }
}
