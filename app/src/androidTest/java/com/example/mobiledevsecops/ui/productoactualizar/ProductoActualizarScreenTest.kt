package com.example.mobiledevsecops.ui.productoactualizar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.domain.usecase.ActualizarProductoUseCase
import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import org.junit.Rule
import org.junit.Test

class ProductoActualizarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeProductoRepository()
    private val useCase = ActualizarProductoUseCase(fakeRepo)

    private val params = ProductoActualizarParams(
        id = 1,
        strNombreProducto = "Laptop HP",
        strURLImagen = "https://example.com/laptop.jpg",
        strDescripcion = "Laptop 15.6 pulgadas",
        intNumeroExistencia = 10,
        decPrecio = 12500.00,
        rowVersion = "AAAAAAAAB9E="
    )

    private val viewModel = ProductoActualizarViewModel(useCase, params)

    @Test
    fun producto_actualizar_screen_muestra_titulo() {
        composeTestRule.setContent {
            ProductoActualizarScreen(
                params = params,
                onNavigateBack = { },
                onProductoActualizado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Actualizar Producto").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre del Producto").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Actualizar").assertIsDisplayed()
    }
}
