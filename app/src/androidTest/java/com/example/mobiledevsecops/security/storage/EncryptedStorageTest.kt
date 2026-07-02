package com.example.mobiledevsecops.security.storage

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.shared.security.SecurityFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedStorageTest {

    private lateinit var tokenManager: TokenManager

    @Before
    fun setUp() {
        tokenManager = TokenManager(ApplicationProvider.getApplicationContext())
        tokenManager.clearAll()
    }

    @Test
    fun token_guardado_puede_ser_recuperado() {
        tokenManager.saveToken(SecurityFixtures.validJwtToken)
        val retrieved = tokenManager.getToken()
        assertEquals(SecurityFixtures.validJwtToken, retrieved)
    }

    @Test
    fun token_no_guardado_retorna_null() {
        tokenManager.clearAll()
        val retrieved = tokenManager.getToken()
        assertNull(retrieved)
    }

    @Test
    fun clearAll_elimina_el_token_almacenado() {
        tokenManager.saveToken(SecurityFixtures.validJwtToken)
        assertNotNull(tokenManager.getToken())

        tokenManager.clearAll()
        assertNull(tokenManager.getToken())
    }

    @Test
    fun isLoggedIn_retorna_true_cuando_hay_token() {
        tokenManager.saveToken(SecurityFixtures.validJwtToken)
        assertTrue(tokenManager.isLoggedIn())
    }

    @Test
    fun isLoggedIn_retorna_false_cuando_no_hay_token() {
        tokenManager.clearAll()
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun token_vacio_se_guarda_y_recupera_correctamente() {
        tokenManager.saveToken("")
        val retrieved = tokenManager.getToken()
        assertEquals("", retrieved)
    }

    @Test
    fun token_jwt_expirado_se_almacena_pero_no_se_considera_valido() {
        tokenManager.saveToken(SecurityFixtures.expiredJwtToken)
        val retrieved = tokenManager.getToken()
        assertEquals(SecurityFixtures.expiredJwtToken, retrieved)
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun persistencia_entre_instancias_de_tokenManager() {
        tokenManager.saveToken(SecurityFixtures.validJwtToken)

        val newTokenManager = TokenManager(ApplicationProvider.getApplicationContext())
        val retrieved = newTokenManager.getToken()
        assertEquals(
            "Los tokens deben persistir entre instancias",
            SecurityFixtures.validJwtToken,
            retrieved
        )
    }
}
