package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class EliminarEmpleadoUseCaseTest {

    private val fakeRepo = FakeEmpleadoRepository()
    private val useCase = EliminarEmpleadoUseCase(fakeRepo)

    @Test
    fun `eliminar empleado con datos validos retorna Success`() = runTest {
        fakeRepo.givenEmpleados(listOf(com.example.mobiledevsecops.shared.fixture.EmpleadoFixtures.empleado))
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarEmpleadoResult.Success)
    }

    @Test
    fun `eliminar empleado con id invalido retorna ValidationError`() = runTest {
        val result = useCase(0, "AAAAAAAAB9E=")
        val error = result as EliminarEmpleadoResult.ValidationError
        assertTrue(error.errores.containsKey("id"))
    }

    @Test
    fun `eliminar empleado con rowVersion vacio retorna ValidationError`() = runTest {
        val result = useCase(1, "")
        val error = result as EliminarEmpleadoResult.ValidationError
        assertTrue(error.errores.containsKey("rowVersion"))
    }

    @Test
    fun `eliminar empleado con conflicto retorna Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarEmpleadoResult.Error)
    }

    @Test
    fun `eliminar empleado con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarEmpleadoResult.SessionExpired)
    }
}
