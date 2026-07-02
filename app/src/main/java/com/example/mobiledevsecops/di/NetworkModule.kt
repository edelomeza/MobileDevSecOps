package com.example.mobiledevsecops.di

import com.example.mobiledevsecops.BuildConfig
import com.example.mobiledevsecops.data.local.TokenManager
import com.example.mobiledevsecops.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        val tokenManager = get<TokenManager>()

        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                })
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 15_000
            }

            install(io.ktor.client.plugins.HttpRequestRetry) {
                maxRetries = 3
                retryOnServerErrors()
                retryOnException()
            }

            install(Auth) {
                bearer {
                    sendWithoutRequest { false }
                    loadTokens {
                        val token = tokenManager.getToken()
                        if (token != null) {
                            BearerTokens(token, "")
                        } else {
                            null
                        }
                    }
                    refreshTokens {
                        Logger.w("Token rechazado por el servidor (401), limpiando sesión")
                        tokenManager.clearAll()
                        null
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    level = LogLevel.HEADERS
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            val sanitized = message.replace(
                                Regex("Authorization: Bearer [^\\s]+"),
                                "Authorization: Bearer [REDACTED]"
                            )
                            Logger.d("KTOR: $sanitized")
                        }
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                install(ResponseObserver) {
                    onResponse { response ->
                        Logger.d("Response ${response.status.value}: ${response.status.description}")
                    }
                }
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
