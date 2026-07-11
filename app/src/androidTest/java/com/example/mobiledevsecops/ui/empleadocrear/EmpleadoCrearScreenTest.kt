package com.example.mobiledevsecops.ui.empleadocrear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.domain.usecase.CrearEmpleadoUseCase
import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import org.junit.Rule
import org.junit.Test

class EmpleadoCrearScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeEmpleadoRepository()
    private val useCase = CrearEmpleadoUseCase(fakeRepo)
    private val viewModel = EmpleadoCrearViewModel(useCase, fakeRepo)

    @Test
    fun empleado_crear_screen_muestra_campos() {
        composeTestRule.setContent {
            EmpleadoCrearScreen(
                onNavigateBack = { },
                onEmpleadoCreado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Crear Empleado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar").assertIsDisplayed()
    }

    @Test
    fun empleado_crear_screen_muestra_campos_opcionales() {
        composeTestRule.setContent {
            EmpleadoCrearScreen(
                onNavigateBack = { },
                onEmpleadoCreado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Apellido Paterno").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apellido Materno").assertIsDisplayed()
        composeTestRule.onNodeWithText("CURP").assertIsDisplayed()
    }
}
