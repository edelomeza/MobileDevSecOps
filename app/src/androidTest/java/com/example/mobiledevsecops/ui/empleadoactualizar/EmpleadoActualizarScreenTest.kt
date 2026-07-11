package com.example.mobiledevsecops.ui.empleadoactualizar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.domain.usecase.ActualizarEmpleadoUseCase
import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import com.example.mobiledevsecops.shared.fixture.EmpleadoFixtures
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EmpleadoActualizarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeEmpleadoRepository()
    private lateinit var useCase: ActualizarEmpleadoUseCase
    private lateinit var viewModel: EmpleadoActualizarViewModel

    @Before
    fun setUp() {
        fakeRepo.givenEmpleados(listOf(EmpleadoFixtures.empleado))
        useCase = ActualizarEmpleadoUseCase(fakeRepo)
        viewModel = EmpleadoActualizarViewModel(
            useCase, fakeRepo,
            EmpleadoActualizarParams(1, "AAAAAAAAB9E=")
        )
    }

    @Test
    fun empleado_actualizar_screen_muestra_campos() {
        composeTestRule.setContent {
            EmpleadoActualizarScreen(
                id = 1,
                rowVersion = "AAAAAAAAB9E=",
                onNavigateBack = { },
                onEmpleadoActualizado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Actualizar Empleado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Actualizar").assertIsDisplayed()
    }
}
