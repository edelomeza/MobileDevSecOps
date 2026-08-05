package com.example.mobiledevsecops.ui.ventacrear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.mobiledevsecops.domain.usecase.BuscarClientesUseCase
import com.example.mobiledevsecops.domain.usecase.BuscarUsuariosUseCase
import com.example.mobiledevsecops.domain.usecase.CrearVentaUseCase
import com.example.mobiledevsecops.shared.fake.FakeClienteRepository
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fake.FakeVentaRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VentaCrearScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeVentaRepo = FakeVentaRepository()
    private val fakeClienteRepo = FakeClienteRepository()
    private val fakeUsuarioRepo = FakeUsuarioRepository()
    private lateinit var viewModel: VentaCrearViewModel

    @Before
    fun setUp() {
        fakeClienteRepo.givenClientes(
            listOf(
                com.example.mobiledevsecops.domain.model.Cliente(
                    id = 1,
                    strNombreCliente = "Juan Perez",
                    strDireccionCliente = "Calle 123",
                    strCorreoElectronico = "juan@example.com",
                    strNumeroTelefono = "5512345678",
                    rowVersion = "AAAAAAAAB9E="
                )
            )
        )
        fakeUsuarioRepo.givenUsuarios(UsuarioFixtures.usuariosMultiPage)

        val crearVentaUseCase = CrearVentaUseCase(fakeVentaRepo)
        val buscarClientesUseCase = BuscarClientesUseCase(fakeClienteRepo)
        val buscarUsuariosUseCase = BuscarUsuariosUseCase(fakeUsuarioRepo)
        viewModel = VentaCrearViewModel(
            crearVentaUseCase,
            buscarClientesUseCase,
            buscarUsuariosUseCase
        )
    }

    @Test
    fun crear_venta_screen_muestra_campos() {
        composeTestRule.setContent {
            VentaCrearScreen(
                onNavigateBack = { },
                onVentaCreada = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Crear Venta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cliente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar").assertIsDisplayed()
    }

    @Test
    fun crear_venta_sin_seleccion_muestra_errores() {
        composeTestRule.setContent {
            VentaCrearScreen(
                onNavigateBack = { },
                onVentaCreada = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Guardar").performClick()

        composeTestRule.onNodeWithText("Debe seleccionar un cliente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Debe seleccionar un usuario").assertIsDisplayed()
    }

    @Test
    fun autocompletado_cliente_muestra_sugerencias() {
        composeTestRule.setContent {
            VentaCrearScreen(
                onNavigateBack = { },
                onVentaCreada = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Buscar cliente", useUnmergedTree = true).performTextInput("Juan")

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Juan Perez"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Juan Perez").assertIsDisplayed()
    }

    @Test
    fun crear_venta_datos_validos_emite_venta_creada() {
        var ventaCreada = false
        composeTestRule.setContent {
            VentaCrearScreen(
                onNavigateBack = { },
                onVentaCreada = { ventaCreada = true },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Buscar cliente", useUnmergedTree = true).performTextInput("Juan")

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Juan Perez"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Juan Perez").performClick()

        composeTestRule.onNodeWithText("Buscar usuario", useUnmergedTree = true).performTextInput("Usuario 1")

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Usuario 1"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Usuario 1").performClick()

        composeTestRule.onNodeWithText("Guardar").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            ventaCreada
        }
        assertTrue(ventaCreada)
    }
}
