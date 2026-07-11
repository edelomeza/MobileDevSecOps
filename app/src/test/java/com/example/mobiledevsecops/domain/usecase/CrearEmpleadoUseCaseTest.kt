package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeEmpleadoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrearEmpleadoUseCaseTest {

    private val fakeRepo = FakeEmpleadoRepository()
    private val useCase = CrearEmpleadoUseCase(fakeRepo)

    @Test
    fun `crear empleado con datos validos retorna Success`() = runTest {
        val result = useCase("Juan", "Perez", "Lopez", "HEXA010101HDFLNN01", 1)
        assertTrue(result is CrearEmpleadoResult.Success)
        assertEquals(1, fakeRepo.empleados.size)
    }

    @Test
    fun `crear empleado con nombre vacio retorna ValidationError`() = runTest {
        val result = useCase("", "Perez", null, null, null)
        val error = result as CrearEmpleadoResult.ValidationError
        assertTrue(error.errores.containsKey("strNombre"))
    }

    @Test
    fun `crear empleado con nombre muy largo retorna ValidationError`() = runTest {
        val nombreLargo = "A".repeat(51)
        val result = useCase(nombreLargo, null, null, null, null)
        val error = result as CrearEmpleadoResult.ValidationError
        assertEquals("Máximo 50 caracteres", error.errores["strNombre"])
    }

    @Test
    fun `crear empleado con nombre caracteres invalidos retorna ValidationError`() = runTest {
        val result = useCase("Juan!!!", null, null, null, null)
        val error = result as CrearEmpleadoResult.ValidationError
        assertEquals("Solo letras, números, guion bajo y espacios", error.errores["strNombre"])
    }

    @Test
    fun `crear empleado con aPaterno muy largo retorna ValidationError`() = runTest {
        val aPaternoLargo = "A".repeat(51)
        val result = useCase("Juan", aPaternoLargo, null, null, null)
        val error = result as CrearEmpleadoResult.ValidationError
        assertEquals("Máximo 50 caracteres", error.errores["strAPaterno"])
    }

    @Test
    fun `crear empleado con aPaterno caracteres invalidos retorna ValidationError`() = runTest {
        val result = useCase("Juan", "Perez123", null, null, null)
        val error = result as CrearEmpleadoResult.ValidationError
        assertEquals("Solo letras (incluye acentos/ñ) y espacios", error.errores["strAPaterno"])
    }

    @Test
    fun `crear empleado con CURP invalido retorna ValidationError`() = runTest {
        val result = useCase("Juan", null, null, "CURPINVALIDA", null)
        val error = result as CrearEmpleadoResult.ValidationError
        assertEquals("Formato de CURP inválido", error.errores["strCURP"])
    }

    @Test
    fun `crear empleado cuando falla repositorio retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase("Juan", null, null, null, null)
        assertTrue(result is CrearEmpleadoResult.Error)
    }

    @Test
    fun `crear empleado con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase("Juan", null, null, null, null)
        assertTrue(result is CrearEmpleadoResult.SessionExpired)
    }

    @Test
    fun `validacion retorna multiples errores simultaneos`() {
        val errores = useCase.validar("", "", "", "INVALIDA", null)
        assertTrue(errores.size >= 1)
        assertTrue(errores.containsKey("strNombre"))
    }
}
