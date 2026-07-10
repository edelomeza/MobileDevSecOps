package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuscarUsuariosUseCaseTest {

    private val fakeRepo = FakeUsuarioRepository()
    private val useCase = BuscarUsuariosUseCase(fakeRepo)

    @Before
    fun setUp() {
        fakeRepo.givenUsuarios(UsuarioFixtures.usuariosMultiPage)
    }

    @Test
    fun `buscar con texto valido retorna Success`() = runTest {
        val result = useCase("Usuario 5", 1)
        assertTrue(result is BuscarUsuariosResult.Success)
        val success = result as BuscarUsuariosResult.Success
        assertEquals(1, success.page.totalCount)
        assertEquals("Usuario 5", success.page.items.first().strNombre)
    }

    @Test
    fun `buscar con texto corto retorna ValidationError`() = runTest {
        val result = useCase("a", 1)
        assertTrue(result is BuscarUsuariosResult.ValidationError)
    }

    @Test
    fun `buscar con texto vacio retorna ValidationError`() = runTest {
        val result = useCase("", 1)
        assertTrue(result is BuscarUsuariosResult.ValidationError)
    }

    @Test
    fun `buscar con texto largo retorna ValidationError`() = runTest {
        val textoLargo = "A".repeat(101)
        val result = useCase(textoLargo, 1)
        assertTrue(result is BuscarUsuariosResult.ValidationError)
    }

    @Test
    fun `buscar con coincidencia parcial retorna resultados`() = runTest {
        val result = useCase("ario 5", 1)
        assertTrue(result is BuscarUsuariosResult.Success)
        val success = result as BuscarUsuariosResult.Success
        assertEquals(1, success.page.totalCount)
    }

    @Test
    fun `buscar con correo retorna resultados`() = runTest {
        val result = useCase("usuario5@", 1)
        assertTrue(result is BuscarUsuariosResult.Success)
        val success = result as BuscarUsuariosResult.Success
        assertEquals(1, success.page.totalCount)
    }

    @Test
    fun `buscar sin coincidencias retorna lista vacia`() = runTest {
        val result = useCase("xyz123", 1)
        assertTrue(result is BuscarUsuariosResult.Success)
        val success = result as BuscarUsuariosResult.Success
        assertEquals(0, success.page.totalCount)
        assertTrue(success.page.items.isEmpty())
    }

    @Test
    fun `buscar sin coincidencias case insensitive`() = runTest {
        val result = useCase("USUARIO 5", 1)
        assertTrue(result is BuscarUsuariosResult.Success)
        val success = result as BuscarUsuariosResult.Success
        assertEquals(1, success.page.totalCount)
    }

    @Test
    fun `buscar cuando falla el repositorio retorna Error`() = runTest {
        fakeRepo.shouldThrowException = true
        val result = useCase("Usuario 1", 1)
        assertTrue(result is BuscarUsuariosResult.Error)
    }

    @Test
    fun `buscar con sesion expirada retorna SessionExpired`() = runTest {
        fakeRepo.shouldThrowSessionExpired = true
        val result = useCase("Usuario 1", 1)
        assertTrue(result is BuscarUsuariosResult.SessionExpired)
    }
}
