package com.example.mobiledevsecops.ui.productoeliminar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.domain.usecase.EliminarProductoUseCase
import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import org.junit.Rule
import org.junit.Test

class ProductoEliminarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeProductoRepository()
    private val useCase = EliminarProductoUseCase(fakeRepo)

    private val params = ProductoEliminarParams(
        id = 1,
        strNombreProducto = "Laptop HP",
        strURLImagen = "https://example.com/laptop.jpg",
        strDescripcion = "Laptop 15.6 pulgadas",
        intNumeroExistencia = 10,
        decPrecio = 12500.00,
        rowVersion = "AAAAAAAAB9E="
    )

    private val viewModel = ProductoEliminarViewModel(useCase, params)

    @Test
    fun producto_eliminar_screen_muestra_titulo() {
        composeTestRule.setContent {
            ProductoEliminarScreen(
                params = params,
                onNavigateBack = { },
                onProductoEliminado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Eliminar Producto").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿Está seguro de eliminar este producto?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eliminar").assertIsDisplayed()
    }
}
