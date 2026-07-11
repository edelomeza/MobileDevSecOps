package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ActualizarEmpleadoUseCaseTest {

    private val fakeRepo = FakeEmpleadoRepository()
    private val useCase = ActualizarEmpleadoUseCase(fakeRepo)

    @Test
    fun `actualizar empleado con datos validos retorna Success`() = runTest {
        fakeRepo.givenEmpleados(listOf(com.example.mobiledevsecops.shared.fixture.EmpleadoFixtures.empleado))
        val result = useCase(1, "Juan", "Perez", null, null, null, "AAAAAAAAB9E=")
        assertTrue(result is ActualizarEmpleadoResult.Success)
    }

    @Test
    fun `actualizar empleado con id invalido retorna ValidationError`() = runTest {
        val result = useCase(0, "Juan", null, null, null, null, "AAAAAAAAB9E=")
        val error = result as ActualizarEmpleadoResult.ValidationError
        assertTrue(error.errores.containsKey("id"))
    }

    @Test
    fun `actualizar empleado con rowVersion vacio retorna ValidationError`() = runTest {
        val result = useCase(1, "Juan", null, null, null, null, "")
        val error = result as ActualizarEmpleadoResult.ValidationError
        assertTrue(error.errores.containsKey("rowVersion"))
    }

    @Test
    fun `actualizar empleado con conflicto retorna Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        val result = useCase(1, "Juan", null, null, null, null, "AAAAAAAAB9E=")
        assertTrue(result is ActualizarEmpleadoResult.Error)
    }

    @Test
    fun `actualizar empleado con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(1, "Juan", null, null, null, null, "AAAAAAAAB9E=")
        assertTrue(result is ActualizarEmpleadoResult.SessionExpired)
    }
}
