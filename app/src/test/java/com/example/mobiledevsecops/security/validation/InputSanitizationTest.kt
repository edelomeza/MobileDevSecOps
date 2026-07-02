package com.example.mobiledevsecops.security.validation

import com.example.mobiledevsecops.domain.usecase.CrearUsuarioUseCase
import com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository
import com.example.mobiledevsecops.shared.security.SecurityFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputSanitizationTest {

    private val useCase = CrearUsuarioUseCase(FakeUsuarioRepository())

    @Test
    fun `nombre con espacios y guion bajo no produce error`() {
        val errores = useCase.validar(
            strNombre = "Maria Jose Lopez",
            strPWD = SecurityFixtures.validPassword,
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertFalse(errores.containsKey("strNombre"))
    }

    @Test
    fun `nombre con acentos es aceptado por politica actual`() {
        val errores = useCase.validar(
            strNombre = "María José López",
            strPWD = SecurityFixtures.validPassword,
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertFalse("La política actual acepta acentos (regex con \\p{L})", errores.containsKey("strNombre"))
    }

    @Test
    fun `nombre con numeros es permitido`() {
        val errores = useCase.validar(
            strNombre = "Usuario123",
            strPWD = SecurityFixtures.validPassword,
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertFalse(errores.containsKey("strNombre"))
    }

    @Test
    fun `nombre con guion bajo es permitido`() {
        val errores = useCase.validar(
            strNombre = "juan_perez",
            strPWD = SecurityFixtures.validPassword,
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertFalse(errores.containsKey("strNombre"))
    }

    @Test
    fun `nombre con inyeccion SQL produce error de caracteres no permitidos`() {
        for (payload in SecurityFixtures.sqlInjectionPayloads) {
            val errores = useCase.validar(
                strNombre = payload,
                strPWD = SecurityFixtures.validPassword,
                strCorreoElectronico = SecurityFixtures.validEmail
            )
            assertTrue(
                "SQL injection '$payload' debería ser rechazado",
                errores.containsKey("strNombre")
            )
        }
    }

    @Test
    fun `nombre con XSS produce error de caracteres no permitidos`() {
        for (payload in SecurityFixtures.xssPayloads) {
            val errores = useCase.validar(
                strNombre = payload,
                strPWD = SecurityFixtures.validPassword,
                strCorreoElectronico = SecurityFixtures.validEmail
            )
            assertTrue(
                "XSS payload '$payload' debería ser rechazado",
                errores.containsKey("strNombre")
            )
        }
    }

    @Test
    fun `nombre con payloads NoSQL produce error`() {
        for (payload in SecurityFixtures.noSqlInjectionPayloads) {
            val errores = useCase.validar(
                strNombre = payload,
                strPWD = SecurityFixtures.validPassword,
                strCorreoElectronico = SecurityFixtures.validEmail
            )
            assertTrue(
                "NoSQL injection '$payload' debería ser rechazado",
                errores.containsKey("strNombre")
            )
        }
    }

    @Test
    fun `nombre con path traversal produce error`() {
        for (payload in SecurityFixtures.pathTraversalPayloads) {
            val errores = useCase.validar(
                strNombre = payload,
                strPWD = SecurityFixtures.validPassword,
                strCorreoElectronico = SecurityFixtures.validEmail
            )
            assertTrue(
                "Path traversal '$payload' debería ser rechazado",
                errores.containsKey("strNombre")
            )
        }
    }

    @Test
    fun `nombre vacio produce error`() {
        val errores = useCase.validar(
            strNombre = "",
            strPWD = SecurityFixtures.validPassword,
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(errores.containsKey("strNombre"))
        assertTrue(errores["strNombre"]?.contains("obligatorio") == true)
    }

    @Test
    fun `nombre con solo espacios produce error`() {
        val errores = useCase.validar(
            strNombre = "   ",
            strPWD = SecurityFixtures.validPassword,
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(errores.containsKey("strNombre"))
    }

    @Test
    fun `nombre mayor a 50 caracteres produce error`() {
        val errores = useCase.validar(
            strNombre = "A".repeat(51),
            strPWD = SecurityFixtures.validPassword,
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(errores.containsKey("strNombre"))
        assertEquals("Máximo 50 caracteres", errores["strNombre"])
    }

    @Test
    fun `nombre exactamente 50 caracteres es valido`() {
        val errores = useCase.validar(
            strNombre = "A".repeat(50),
            strPWD = SecurityFixtures.validPassword,
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertFalse(errores.containsKey("strNombre"))
    }

    @Test
    fun `email con inyeccion produce error`() {
        for (payload in SecurityFixtures.sqlInjectionPayloads) {
            val errores = useCase.validar(
                strNombre = SecurityFixtures.validName,
                strPWD = SecurityFixtures.validPassword,
                strCorreoElectronico = "$payload@example.com"
            )
            assertTrue(
                "Email con SQL injection '$payload' debería ser rechazado",
                errores.containsKey("strCorreoElectronico")
            )
        }
    }

    @Test
    fun `email malformado produce error`() {
        val invalidEmails = listOf(
            "usuario",
            "@example.com",
            "usuario@",
            "usuario@.com",
            "usuario@example",
            "usuario @example.com",
            "usuario@example.c"
        )
        for (email in invalidEmails) {
            val errores = useCase.validar(
                strNombre = SecurityFixtures.validName,
                strPWD = SecurityFixtures.validPassword,
                strCorreoElectronico = email
            )
            assertTrue("Email inválido '$email' debería ser rechazado", errores.containsKey("strCorreoElectronico"))
        }
    }

    @Test
    fun `email con caracteres especiales comunes en dominios es valido`() {
        val validEmails = listOf(
            "user+tag@example.com",
            "user.name@example.com",
            "user_name@example.com",
            "user-name@example.com",
            "user123@example.co.uk",
            "a@b.cd"
        )
        for (email in validEmails) {
            val errores = useCase.validar(
                strNombre = SecurityFixtures.validName,
                strPWD = SecurityFixtures.validPassword,
                strCorreoElectronico = email
            )
            assertFalse("Email válido '$email' no debería ser rechazado", errores.containsKey("strCorreoElectronico"))
        }
    }

    @Test
    fun `email mayor a 50 caracteres produce error`() {
        val errores = useCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = SecurityFixtures.validPassword,
            strCorreoElectronico = "a".repeat(51) + "@example.com"
        )
        assertTrue(errores.containsKey("strCorreoElectronico"))
    }
}
