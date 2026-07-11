package com.example.mobiledevsecops.ui.empleadoeliminar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobiledevsecops.domain.usecase.EliminarEmpleadoUseCase
import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import com.example.mobiledevsecops.shared.fixture.EmpleadoFixtures
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EmpleadoEliminarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepo = FakeEmpleadoRepository()
    private lateinit var useCase: EliminarEmpleadoUseCase
    private lateinit var viewModel: EmpleadoEliminarViewModel

    @Before
    fun setUp() {
        fakeRepo.givenEmpleados(listOf(EmpleadoFixtures.empleado))
        useCase = EliminarEmpleadoUseCase(fakeRepo)
        viewModel = EmpleadoEliminarViewModel(
            useCase, fakeRepo,
            EmpleadoEliminarParams(1, "AAAAAAAAB9E=")
        )
    }

    @Test
    fun empleado_eliminar_screen_muestra_campos() {
        composeTestRule.setContent {
            EmpleadoEliminarScreen(
                id = 1,
                rowVersion = "AAAAAAAAB9E=",
                onNavigateBack = { },
                onEmpleadoEliminado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Eliminar Empleado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apellido Paterno").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eliminar").assertIsDisplayed()
    }

    @Test
    fun empleado_eliminar_screen_muestra_mensaje_confirmacion() {
        composeTestRule.setContent {
            EmpleadoEliminarScreen(
                id = 1,
                rowVersion = "AAAAAAAAB9E=",
                onNavigateBack = { },
                onEmpleadoEliminado = { },
                onError = { },
                onSessionExpired = { },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText(
            "¿Está seguro de eliminar este empleado? Esta acción no se puede deshacer."
        ).assertIsDisplayed()
    }
}
