package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrearUsuarioUseCaseTest {

    private val fakeRepo = FakeUsuarioRepository()
    private val useCase = CrearUsuarioUseCase(fakeRepo)

    @Test
    fun `crear usuario con datos validos retorna Success`() = runTest {
        val result = useCase("Juan Pérez", "Abcd1234!@", "juan@example.com")
        assertTrue(result is CrearUsuarioResult.Success)
        assertEquals(1, fakeRepo.usuarios.size)
    }

    @Test
    fun `crear usuario con nombre vacio retorna ValidationError`() = runTest {
        val result = useCase("", "password123", "juan@example.com")
        val error = result as CrearUsuarioResult.ValidationError
        assertTrue(error.errores.containsKey("strNombre"))
        assertFalse(fakeRepo.usuarios.any { it.strCorreoElectronico == "juan@example.com" })
    }

    @Test
    fun `crear usuario con nombre muy largo retorna ValidationError`() = runTest {
        val nombreLargo = "A".repeat(51)
        val result = useCase(nombreLargo, "password123", "juan@example.com")
        val error = result as CrearUsuarioResult.ValidationError
        assertEquals("Máximo 50 caracteres", error.errores["strNombre"])
    }

    @Test
    fun `crear usuario con nombre con caracteres especiales retorna ValidationError`() = runTest {
        val result = useCase("Juan!!!", "password123", "juan@example.com")
        val error = result as CrearUsuarioResult.ValidationError
        assertEquals("Solo letras (incluye acentos/ñ), números y espacios", error.errores["strNombre"])
    }

    @Test
    fun `crear usuario con password corto retorna ValidationError`() = runTest {
        val result = useCase("Juan Pérez", "1234567", "juan@example.com")
        val error = result as CrearUsuarioResult.ValidationError
        assertEquals("Debe tener: mínimo 8 caracteres, 1 mayúscula, 1 caracter especial", error.errores["strPWD"])
    }

    @Test
    fun `crear usuario con correo invalido retorna ValidationError`() = runTest {
        val result = useCase("Juan Pérez", "password123", "correo-invalido")
        val error = result as CrearUsuarioResult.ValidationError
        assertEquals("Formato de correo inválido", error.errores["strCorreoElectronico"])
    }

    @Test
    fun `crear usuario cuando falla el repositorio retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase("Juan Pérez", "Abcd1234!@", "juan@example.com")
        assertTrue(result is CrearUsuarioResult.Error)
    }

    @Test
    fun `crear usuario con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase("Juan Pérez", "Abcd1234!@", "juan@example.com")
        assertTrue(result is CrearUsuarioResult.SessionExpired)
    }

    @Test
    fun `validacion retorna multiples errores simultaneos`() {
        val errores = useCase.validar("", "", "")
        assertEquals(3, errores.size)
        assertTrue(errores.containsKey("strNombre"))
        assertTrue(errores.containsKey("strPWD"))
        assertTrue(errores.containsKey("strCorreoElectronico"))
    }
}
