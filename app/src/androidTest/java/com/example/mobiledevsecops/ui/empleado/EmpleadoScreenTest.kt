package com.example.mobiledevsecops.ui.empleado

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import com.example.mobiledevsecops.shared.fixture.EmpleadoFixtures
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EmpleadoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeEmpleadoRepository()
    private lateinit var viewModel: EmpleadoViewModel

    @Before
    fun setUp() {
        fakeRepo.givenEmpleados(EmpleadoFixtures.empleadosMultiPage)
        viewModel = EmpleadoViewModel(fakeRepo)
    }

    @Test
    fun empleado_screen_muestra_titulo_y_total() {
        composeTestRule.setContent {
            EmpleadoScreen(
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToEdit = { _, _ -> },
                onNavigateToDelete = { _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Empleados").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total: 15 empleados").assertIsDisplayed()
    }

    @Test
    fun empleado_screen_muestra_paginacion() {
        composeTestRule.setContent {
            EmpleadoScreen(
                onNavigateBack = { },
                onSessionExpired = { },
                onNavigateToCreate = { },
                onNavigateToEdit = { _, _ -> },
                onNavigateToDelete = { _, _ -> },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Página 1 de 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Siguiente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Anterior").assertIsDisplayed()
    }
}
