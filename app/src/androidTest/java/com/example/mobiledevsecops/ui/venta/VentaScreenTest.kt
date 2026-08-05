package com.example.mobiledevsecops.ui.venta

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.mobiledevsecops.domain.usecase.BuscarVentasUseCase
import com.example.mobiledevsecops.shared.fake.FakeVentaRepository
import com.example.mobiledevsecops.shared.fixture.VentaFixtures
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VentaScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeVentaRepository()
    private lateinit var buscarVentasUseCase: BuscarVentasUseCase
    private lateinit var viewModel: VentaViewModel

    @Before
    fun setUp() {
        fakeRepo.givenVentas(VentaFixtures.ventasMultiPage)
        buscarVentasUseCase = BuscarVentasUseCase(fakeRepo)
        viewModel = VentaViewModel(fakeRepo, buscarVentasUseCase)
    }

    @Test
    fun venta_screen_muestra_titulo_y_total() {
        composeTestRule.setContent {
            VentaScreen(
                reloadSignal = false,
                operationResult = "",
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToDetalle = { _, _, _, _, _, _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Ventas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total: 15 ventas").assertIsDisplayed()
    }

    @Test
    fun venta_screen_muestra_paginacion() {
        composeTestRule.setContent {
            VentaScreen(
                reloadSignal = false,
                operationResult = "",
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToDetalle = { _, _, _, _, _, _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Página 1 de 2"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Página 1 de 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Siguiente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Anterior").assertIsDisplayed()
    }

    @Test
    fun venta_screen_muestra_filtros() {
        composeTestRule.setContent {
            VentaScreen(
                reloadSignal = false,
                operationResult = "",
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToDetalle = { _, _, _, _, _, _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Buscar por clave o cliente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Desde").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hasta").assertIsDisplayed()
    }

    @Test
    fun venta_screen_muestra_encabezados_de_tabla() {
        composeTestRule.setContent {
            VentaScreen(
                reloadSignal = false,
                operationResult = "",
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToDetalle = { _, _, _, _, _, _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Fecha y Hora").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clave Venta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre Cliente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estado").assertIsDisplayed()
    }

    @Test
    fun buscar_por_cliente_muestra_resultados() {
        composeTestRule.setContent {
            VentaScreen(
                reloadSignal = false,
                operationResult = "",
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToDetalle = { _, _, _, _, _, _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Buscar por clave o cliente").performTextInput("Cliente 5")
        composeTestRule.onNodeWithText("Buscar").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Total: 1 ventas"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Total: 1 ventas").assertIsDisplayed()
    }

    @Test
    fun limpiar_busqueda_restaura_lista() {
        composeTestRule.setContent {
            VentaScreen(
                reloadSignal = false,
                operationResult = "",
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToDetalle = { _, _, _, _, _, _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Buscar por clave o cliente").performTextInput("Cliente 5")
        composeTestRule.onNodeWithText("Buscar").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Mostrando resultados de búsqueda"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Mostrar todos").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Total: 15 ventas"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Total: 15 ventas").assertIsDisplayed()
    }

    @Test
    fun venta_screen_muestra_contenido_de_filas() {
        composeTestRule.setContent {
            VentaScreen(
                reloadSignal = false,
                operationResult = "",
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToDetalle = { _, _, _, _, _, _, _, _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Cliente 1"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Cliente 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("V000000001").assertIsDisplayed()
    }
}
