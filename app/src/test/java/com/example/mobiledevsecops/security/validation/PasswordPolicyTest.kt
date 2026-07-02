package com.example.mobiledevsecops.security.validation

import com.example.mobiledevsecops.domain.usecase.ActualizarUsuarioUseCase
import com.example.mobiledevsecops.domain.usecase.CrearUsuarioUseCase
import com.example.mobiledevsecops.shared.security.SecurityFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordPolicyTest {

    private val crearUseCase = CrearUsuarioUseCase(
        com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository()
    )
    private val actualizarUseCase = ActualizarUsuarioUseCase(
        com.example.mobiledevsecops.shared.fake.FakeUsuarioRepository()
    )

    @Test
    fun `password en blanco produce error de validacion`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(errores.containsKey("strPWD"))
        assertEquals("La contraseña es obligatoria", errores["strPWD"])
    }

    @Test
    fun `password menor a 8 caracteres produce error`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "Ab1#",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(errores.containsKey("strPWD"))
        assertEquals("Debe tener: mínimo 8 caracteres", errores["strPWD"])
    }

    @Test
    fun `password de exactamente 8 caracteres es valida`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "Abcd1234!",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertFalse(errores.containsKey("strPWD"))
    }

    @Test
    fun `password de mas de 8 caracteres es valida`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "Abcd1234!@#",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertFalse(errores.containsKey("strPWD"))
    }

    @Test
    fun `password sin mayusculas es rechazada por politica actual`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "abcdefgh",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(
            "La política actual exige mayúsculas en la contraseña",
            errores.containsKey("strPWD")
        )
    }

    @Test
    fun `password sin digitos es rechazada por politica actual`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "ABCDEFGH",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(
            "La política actual exige dígitos en la contraseña",
            errores.containsKey("strPWD")
        )
    }

    @Test
    fun `password sin caracteres especiales es rechazada por politica actual`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "Abcdefgh",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(
            "La política actual exige caracteres especiales",
            errores.containsKey("strPWD")
        )
    }

    @Test
    fun `passwords comunes y debiles son rechazadas por politica actual`() {
        for (weakPassword in SecurityFixtures.commonPasswords) {
            val errores = crearUseCase.validar(
                strNombre = SecurityFixtures.validName,
                strPWD = weakPassword,
                strCorreoElectronico = SecurityFixtures.validEmail
            )
            assertTrue(
                "La política actual rechaza contraseña débil: '$weakPassword'",
                errores.containsKey("strPWD")
            )
        }
    }

    @Test
    fun `passwords secuenciales son rechazadas por politica actual`() {
        for (seqPassword in SecurityFixtures.sequentialPasswords) {
            val errores = crearUseCase.validar(
                strNombre = SecurityFixtures.validName,
                strPWD = seqPassword,
                strCorreoElectronico = SecurityFixtures.validEmail
            )
            assertTrue(
                "La política actual rechaza patrones secuenciales: '$seqPassword'",
                errores.containsKey("strPWD")
            )
        }
    }

    @Test
    fun `passwords con caracteres repetidos son rechazadas por politica actual`() {
        for (repPassword in SecurityFixtures.repeatedPasswords) {
            val errores = crearUseCase.validar(
                strNombre = SecurityFixtures.validName,
                strPWD = repPassword,
                strCorreoElectronico = SecurityFixtures.validEmail
            )
            assertTrue(
                "La política actual rechaza caracteres repetidos: '$repPassword'",
                errores.containsKey("strPWD")
            )
        }
    }

    @Test
    fun `password con espacios es rechazada por politica actual`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "abc defg",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(
            "La política actual rechaza espacios en la contraseña",
            errores.containsKey("strPWD")
        )
    }

    @Test
    fun `password solo numeros es rechazada por politica actual`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "12345678",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(
            "La política actual rechaza contraseñas solo numéricas",
            errores.containsKey("strPWD")
        )
    }

    @Test
    fun `password Unicode es rechazada por politica actual`() {
        val errores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "áéíóúñçß",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        assertTrue(
            "La política actual exige mayúsculas, dígitos y caracteres especiales",
            errores.containsKey("strPWD")
        )
    }

    @Test
    fun `validacion de password es identica en actualizar y crear`() {
        val crearErrores = crearUseCase.validar(
            strNombre = SecurityFixtures.validName,
            strPWD = "",
            strCorreoElectronico = SecurityFixtures.validEmail
        )
        val actualizarErrores = actualizarUseCase.validar(
            id = 1,
            strNombre = SecurityFixtures.validName,
            strPWD = "",
            strCorreoElectronico = SecurityFixtures.validEmail,
            rowVersion = "AAAAAAAAB9E="
        )
        assertEquals(crearErrores["strPWD"], actualizarErrores["strPWD"])
    }
}
