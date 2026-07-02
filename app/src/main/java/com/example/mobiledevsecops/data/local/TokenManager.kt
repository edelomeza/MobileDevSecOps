package com.example.mobiledevsecops.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.util.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class TokenManager(context: Context) {
    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String): Boolean {
        if (token.isEmpty()) {
            prefs.edit().putString(KEY_JWT, "").apply()
            if (BuildConfig.DEBUG) Logger.d("Token vacío guardado exitosamente")
            return true
        }
        if (!isJwtValid(token)) {
            if (BuildConfig.DEBUG) Logger.w("Intento de guardar token JWT inválido, rechazado")
            return false
        }
        prefs.edit().putString(KEY_JWT, token).apply()
        if (BuildConfig.DEBUG) Logger.d("Token guardado exitosamente")
        return true
    }

    fun getToken(): String? {
        return prefs.getString(KEY_JWT, null)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun saveLoginAttempts(count: Int, lockoutUntil: Long) {
        prefs.edit()
            .putInt(KEY_LOGIN_ATTEMPTS, count)
            .putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
            .apply()
    }

    fun getLoginAttempts(): Pair<Int, Long> {
        return Pair(
            prefs.getInt(KEY_LOGIN_ATTEMPTS, 0),
            prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        )
    }

    fun clearLoginAttempts() {
        prefs.edit()
            .remove(KEY_LOGIN_ATTEMPTS)
            .remove(KEY_LOCKOUT_UNTIL)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        val token = getToken() ?: return false
        val exp = decodeJwtExpiry(token)
        val currentTime = System.currentTimeMillis() / 1000
        if (BuildConfig.DEBUG) Logger.d("isLoggedIn: exp=$exp, currentTime=$currentTime")
        if (exp == null) {
            if (BuildConfig.DEBUG) Logger.w("Token sin exp, rechazando")
            clearAll()
            return false
        }
        val bufferSec = 300L
        if (exp < currentTime - bufferSec) {
            if (BuildConfig.DEBUG) Logger.w("Token expirado (exp=$exp, currentTime=$currentTime, buffer=${bufferSec}s), limpiando sesión")
            clearAll()
            return false
        }
        return true
    }

    private fun isJwtValid(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                if (BuildConfig.DEBUG) Logger.d("isJwtValid: token no tiene 3 partes")
                return false
            }

            val headerRaw = Base64.decode(parts[0], Base64.URL_SAFE).decodeToString()
            val headerJson = Json.parseToJsonElement(headerRaw).jsonObject
            val alg = headerJson["alg"]?.jsonPrimitive?.content ?: return false
            if (alg.isBlank()) return false

            val payloadRaw = Base64.decode(parts[1], Base64.URL_SAFE).decodeToString()
            val payloadJson = Json.parseToJsonElement(payloadRaw).jsonObject
            val exp = payloadJson["exp"]?.jsonPrimitive?.content?.toLongOrNull() ?: return false

            Base64.decode(parts[2], Base64.URL_SAFE)

            true
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Logger.d("isJwtValid: excepción=${e.message}")
            false
        }
    }

    private fun decodeJwtExpiry(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = Base64.decode(parts[1], Base64.URL_SAFE)
            val json = Json.parseToJsonElement(payload.decodeToString()).jsonObject
            json["exp"]?.jsonPrimitive?.content?.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "secure_auth_prefs"
        private const val KEY_JWT = "jwt_token"
        private const val KEY_LOGIN_ATTEMPTS = "login_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"
    }
}
