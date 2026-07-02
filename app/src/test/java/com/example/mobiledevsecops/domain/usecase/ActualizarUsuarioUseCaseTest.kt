package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ActualizarUsuarioUseCaseTest {

    private val fakeRepo = FakeUsuarioRepository()
    private val useCase = ActualizarUsuarioUseCase(fakeRepo)

    @Before
    fun setUp() {
        fakeRepo.givenUsuarios(listOf(UsuarioFixtures.usuario))
    }

    @Test
    fun `actualizar usuario con datos validos retorna Success`() = runTest {
        val result = useCase(1, "Nuevo Nombre", "Newpass123!", "nuevo@example.com", "AAAAAAAAB9E=")
        assertTrue(result is ActualizarUsuarioResult.Success)
        assertEquals("Nuevo Nombre", fakeRepo.usuarios.first().strNombre)
    }

    @Test
    fun `actualizar usuario con id invalido retorna ValidationError`() = runTest {
        val result = useCase(0, "Nombre", "password123", "correo@example.com", "AAAAAAAAB9E=")
        val error = result as ActualizarUsuarioResult.ValidationError
        assertTrue(error.errores.containsKey("id"))
    }

    @Test
    fun `actualizar usuario con rowVersion vacio retorna ValidationError`() = runTest {
        val result = useCase(1, "Nombre", "password123", "correo@example.com", "")
        val error = result as ActualizarUsuarioResult.ValidationError
        assertTrue(error.errores.containsKey("rowVersion"))
    }

    @Test
    fun `actualizar usuario con conflicto retorna Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        val result = useCase(1, "Nombre", "Abcd1234!@", "correo@example.com", "AAAAAAAAB9E=")
        assertTrue(result is ActualizarUsuarioResult.Error)
        assertEquals("El registro ha sido modificado por otro usuario", (result as ActualizarUsuarioResult.Error).mensaje)
    }

    @Test
    fun `actualizar usuario con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(1, "Nombre", "Abcd1234!@", "correo@example.com", "AAAAAAAAB9E=")
        assertTrue(result is ActualizarUsuarioResult.SessionExpired)
    }
}
