package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EliminarUsuarioUseCaseTest {

    private val fakeRepo = FakeUsuarioRepository()
    private val useCase = EliminarUsuarioUseCase(fakeRepo)

    @Before
    fun setUp() {
        fakeRepo.givenUsuarios(listOf(UsuarioFixtures.usuario))
    }

    @Test
    fun `eliminar usuario con datos validos retorna Success`() = runTest {
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarUsuarioResult.Success)
        assertTrue(fakeRepo.usuarios.isEmpty())
    }

    @Test
    fun `eliminar usuario con id invalido retorna ValidationError`() = runTest {
        val result = useCase(0, "AAAAAAAAB9E=")
        val error = result as EliminarUsuarioResult.ValidationError
        assertTrue(error.errores.containsKey("id"))
    }

    @Test
    fun `eliminar usuario con rowVersion vacio retorna ValidationError`() = runTest {
        val result = useCase(1, "")
        val error = result as EliminarUsuarioResult.ValidationError
        assertTrue(error.errores.containsKey("rowVersion"))
    }

    @Test
    fun `eliminar usuario con conflicto retorna Error`() = runTest {
        fakeRepo.shouldThrowConflict = true
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarUsuarioResult.Error)
    }

    @Test
    fun `eliminar usuario con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase(1, "AAAAAAAAB9E=")
        assertTrue(result is EliminarUsuarioResult.SessionExpired)
    }

    @Test
    fun `validacion de eliminacion con ambos campos invalidos`() {
        val errores = useCase.validar(0, "")
        assertEquals(2, errores.size)
        assertTrue(errores.containsKey("id"))
        assertTrue(errores.containsKey("rowVersion"))
    }
}
