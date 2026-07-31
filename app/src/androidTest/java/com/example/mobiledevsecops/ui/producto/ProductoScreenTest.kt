package com.example.mobiledevsecops.ui.producto

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.domain.usecase.BuscarProductosUseCase
import com.example.mobiledevsecops.shared.fake.FakeProductoRepository
import com.example.mobiledevsecops.shared.fixture.ProductoFixtures
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProductoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeProductoRepository()
    private lateinit var viewModel: ProductoViewModel

    @Before
    fun setUp() {
        fakeRepo.givenProductos(ProductoFixtures.productosMultiPage)
        val buscarUseCase = BuscarProductosUseCase(fakeRepo)
        viewModel = ProductoViewModel(fakeRepo, buscarUseCase)
    }

    @Test
    fun producto_screen_muestra_titulo_y_total() {
        composeTestRule.setContent {
            ProductoScreen(
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToEdit = { _, _, _, _, _, _, _ -> },
                onNavigateToDelete = { _, _, _, _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Productos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total: 15 productos").assertIsDisplayed()
    }

    @Test
    fun producto_screen_muestra_paginacion() {
        composeTestRule.setContent {
            ProductoScreen(
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToEdit = { _, _, _, _, _, _, _ -> },
                onNavigateToDelete = { _, _, _, _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Página 1 de 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Siguiente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Anterior").assertIsDisplayed()
    }
}
