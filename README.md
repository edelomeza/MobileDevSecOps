# MobileDevSecOps

[![CI](https://img.shields.io/badge/CI-passing-brightgreen)]()
[![CodeQL](https://img.shields.io/badge/CodeQL-passing-brightgreen)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple)]()
[![Compose BOM](https://img.shields.io/badge/Compose_BOM-2026.02.01-blue)]()
[![minSdk](https://img.shields.io/badge/minSdk-24-orange)]()
[![targetSdk](https://img.shields.io/badge/targetSdk-36-green)]()
[![Licencia](https://img.shields.io/badge/licencia-MIT-blue)]()

Aplicación Android con prácticas DevSecOps. Gestiona autenticación de usuarios y operaciones CRUD a través de una API REST, construida con Jetpack Compose y Clean Architecture.

---

## Tech Stack

| Componente | Versión |
|---|---|
| Kotlin | 2.2.10 |
| AGP (Android Gradle Plugin) | 9.2.1 |
| Gradle | 9.4.1 |
| JDK | 21 |
| compileSdk / targetSdk | 36 |
| minSdk | 24 |
| Jetpack Compose BOM | 2026.02.01 |
| Ktor Client | 2.3.12 |
| Koin | 4.2.2 |
| MockK | 1.13.12 |
| Detekt | 1.23.8 |
| Ktlint | 12.2.0 |
| Kover | 0.9.1 |
| OWASP Dependency Check | 10.0.4 |

---

## Funcionalidades

### Autenticación
- **Inicio de sesión** con límite de intentos: máximo 5 consecutivos, backoff exponencial (30s → 60s → 120s → ... hasta 16min)
- **Cierre de sesión** con revocación server-side + limpieza local del token
- **Token JWT** con validación estructural al guardar: algoritmo en header, exp/nbf en payload, formato de firma Base64-URL
- **Sesión expirada** detectada automáticamente y redirección a Login desde cualquier pantalla

### Gestión de Usuarios (CRUD)
- **Lista paginada** (8 usuarios por página) con tabla: Nombre | Correo | Editar | Eliminar
- **Crear usuario** con política de contraseñas (mín. 8 caracteres, mayúscula, dígito, especial)
- **Actualizar usuario** con `rowVersion` para control de concurrencia optimista
- **Eliminar usuario** con pantalla de confirmación destructiva
- **Feedback visual** mediante Snackbar (verde para éxito, rojo para error)

---

## Navegación

```
Login → Index → Usuario/{page}
                 +→ Usuario/crear
                 +→ Usuario/actualizar/{id}/{nombre}/{correo}/{rowVersion}
                 +→ Usuario/eliminar/{id}/{nombre}/{correo}/{rowVersion}
```

HTTP 401 dispara `SessionExpiredException` → redirección a Login desde cualquier pantalla.

---

## Arquitectura

Clean Architecture con 3 capas + Inyección de dependencias con Koin.

```
┌──────────────────────────────────────────┐
│      Capa de Presentación (Compose)      │
│                                          │
│  LoginScreen / IndexScreen / Usuario...  │
│  LoginViewModel / IndexViewModel / ...   │
│  → UiState (StateFlow) + Eventos        │
│  → Sin dependencias de Android framework │
├──────────────────────────────────────────┤
│        Capa de Dominio (Kotlin puro)     │
│                                          │
│  UseCases → Login, Logout, Crear, ...    │
│  Repository interfaces (Auth, Usuario)   │
│  Modelos → Usuario, AuthResult, Page     │
│  Sin dependencias Android                │
├──────────────────────────────────────────┤
│          Capa de Datos (Ktor + SP)       │
│                                          │
│  RepositoryImpl → AuthApi / UsuarioApi   │
│  TokenManager (EncryptedSharedPreferences)│
│  DTOs para serialización JSON            │
└──────────────────────────────────────────┘

DI con Koin:
  ├── AppModule → TokenManager, repos, use cases, viewmodels
  └── NetworkModule → HttpClient (auth bearer, timeouts, retry, logging)
```

---

## Estructura del proyecto

```
MobileDevSecOps/
├── app/
│   ├── src/main/java/com/example/mobiledevsecops/
│   │   ├── di/                      # Módulos Koin (AppModule, NetworkModule)
│   │   ├── domain/                  # Capa de dominio
│   │   │   ├── model/               #   Usuario, AuthResult, UsuarioPage
│   │   │   ├── repository/          #   Interfaces AuthRepository, UsuarioRepository
│   │   │   └── usecase/             #   Login, Logout, Crear, Actualizar, Eliminar
│   │   ├── data/                    # Capa de datos
│   │   │   ├── remote/              #   AuthApi, UsuarioApi, DTOs, SessionExpiredException
│   │   │   ├── local/               #   TokenManager (EncryptedSharedPreferences)
│   │   │   └── repository/          #   AuthRepositoryImpl, UsuarioRepositoryImpl
│   │   ├── ui/                      # Capa de presentación
│   │   │   ├── login/               #   LoginScreen + LoginViewModel
│   │   │   ├── index/               #   IndexScreen + IndexViewModel
│   │   │   ├── usuario/             #   UsuarioScreen + UsuarioViewModel
│   │   │   ├── usuariocrear/        #   UsuarioCrearScreen + UsuarioCrearViewModel
│   │   │   ├── usuarioactualizar/   #   UsuarioActualizarScreen + ViewModel
│   │   │   ├── usuarioeliminar/     #   UsuarioEliminarScreen + ViewModel
│   │   │   ├── navigation/          #   AppNavGraph, Routes
│   │   │   └── theme/               #   Tema Material3
│   │   └── util/                    # Logger
│   ├── src/test/                    # Tests unitarios (27 clases)
│   └── src/androidTest/             # Tests instrumentados (16 clases)
├── mock-api/                        # Servidor mock Ktor (Docker)
│   └── src/main/kotlin/.../
│       ├── Application.kt           #   Entry point
│       ├── data/                    #   UsuarioDatabase (in-memory)
│       ├── model/                   #   Modelos del mock
│       └── plugins/                 #   Routing + Serialization
├── ci/                              # Dockerfile para CI
│   └── Dockerfile                   #   JDK 21 + Android SDK 36
├── .devcontainer/                   # VS Code DevContainer
│   ├── devcontainer.json
│   └── Dockerfile
├── .github/workflows/               # Pipelines CI/CD
│   ├── ci.yml                       #   Build, test, lint, seguridad
│   └── codeql.yml                   #   CodeQL SAST
├── gradle/
│   └── libs.versions.toml           # Catálogo de versiones
├── gradle.properties                # Propiedades globales + api.base.url
├── detekt.yml                       # Configuración Detekt
├── dependency-check-suppressions.xml# Supresiones OWASP
├── keys.properties.example          # Template para firma de releases
├── docker-compose.yml               # Mock API service
└── AGENTS.md                        # Documentación para asistentes IA
```

---

## Inicio rápido

```bash
# 1. Clonar el repositorio
git clone <repo-url>
cd MobileDevSecOps

# 2. Iniciar el mock API (Docker)
docker compose up -d
# Credenciales: admin / Admin123!
# Endpoint: http://localhost:8080

# 3. (Opcional) Configurar URL del backend
#   Editar gradle.properties:
#   api.base.url=http://10.0.2.2:8080

# 4. Compilar e instalar
./gradlew assembleDebug

# 5. Ejecutar tests unitarios
./gradlew testDebugUnitTest

# 6. Ejecutar todos los chequeos de calidad
./gradlew lint detekt ktlintCheck --no-daemon
```

---

## Comandos de build

```bash
# Build completo (debug)
./gradlew assembleDebug

# Todos los chequeos de calidad
./gradlew lint detekt ktlintCheck --no-daemon

# Tests unitarios + reporte de cobertura
./gradlew testDebugUnitTest koverHtmlReport koverVerify --no-daemon

# Compilar tests instrumentados (sin ejecutarlos)
./gradlew compileDebugAndroidTestSources --no-daemon

# Escaneo de vulnerabilidades en dependencias
./gradlew dependencyCheckAnalyze --no-daemon

# Build release local con keystore
cp keys.properties.example keys.properties
# Editar keys.properties con las credenciales de tu keystore
./gradlew assembleRelease
```

---

## Tests

### Tests unitarios (27 clases)
JUnit4 + MockK + kotlinx-coroutines-test + Ktor MockEngine

| Categoría | Clase | Descripción |
|---|---|---|
| **ViewModels** | `LoginViewModelTest` | Estado inicial, campos, login exitoso/fallido, red, auto-clear error |
| | `IndexViewModelTest` | Flujo de logout |
| | `UsuarioViewModelTest` | Paginación, sesión expirada |
| | `UsuarioCrearViewModelTest` | Creación con validación |
| | `UsuarioActualizarViewModelTest` | Actualización con rowVersion |
| | `UsuarioEliminarViewModelTest` | Confirmación de eliminación |
| **Use Cases** | `LoginUseCaseTest` | Login exitoso/fallido/red/sesión |
| | `LogoutUseCaseTest` | Logout exitoso/fallido |
| | `CrearUsuarioUseCaseTest` | Validación de reglas de negocio |
| | `ActualizarUsuarioUseCaseTest` | Validación de reglas de negocio |
| | `EliminarUsuarioUseCaseTest` | Validación de reglas de negocio |
| **Data** | `AuthRepositoryImplTest` | Integración API + TokenManager |
| | `UsuarioRepositoryImplTest` | Integración API |
| | `DtoSerializationTest` | Serialización JSON |
| **DI** | `KoinModulesTest` | Carga de módulos |
| **Navigation** | `NavGraphTest` | Rutas de navegación |
| **Seguridad** | `PasswordPolicyTest` | 15 tests: longitud, mayúsculas, dígitos, especiales, contraseñas débiles |
| | `InputSanitizationTest` | 15 tests: SQL injection, XSS, NoSQL, path traversal |
| | `SessionManagementTest` | 10 tests: ciclo de vida del token, 401, expiración |
| | `SensitiveDataLoggingTest` | 5 tests: contraseñas no expuestas, username redactado |

### Tests instrumentados (16 clases)
Compose UI Test + Espresso

| Categoría | Clase | Descripción |
|---|---|---|
| **UI** | `LoginScreenTest` | Renderizado, campos vacíos, Snackbar |
| | `IndexScreenTest` | Pantalla principal |
| | `UsuarioScreenTest` | Título, paginación |
| | `UsuarioCrearScreenTest` | Formulario de creación |
| | `UsuarioActualizarScreenTest` | Formulario de actualización |
| | `UsuarioEliminarScreenTest` | Confirmación de eliminación |
| **Seguridad** | `CertificatePinningTest` | 7 tests: estructura XML, 2 pins SHA-256, expiración, cleartext |
| | `EncryptedStorageTest` | 7 tests: guardar/recuperar/limpiar token, JWT expirado |
| | `PasswordVisibilityTest` | 3 tests: toggle de visibilidad de contraseña |

### Cobertura
Kover con exclusión de: `BuildConfig`, `MainActivity`, `MobileDevSecOpsApp`, `di.*`, `ui.theme.*`, `util.*`.

---

## Pipeline CI/CD

### Workflows en `.github/workflows/`

**ci.yml** — Ejecución en push a `main`/`develop` y PR a `main`:

| # | Job | Descripción |
|---|---|---|
| 1 | `validate-wrapper` | Validación de checksum del Gradle Wrapper |
| 2 | `static-analysis` | `lint` + `detekt` + `ktlintCheck` |
| 3 | `unit-test` | `testDebugUnitTest` + `koverHtmlReport` + `koverVerify` |
| 4 | `instrumentation-compile` | Compilación de tests instrumentados |
| 5 | `security-scan` | `dependencyCheckAnalyze` (falla en CVSS ≥ 7.0) |
| 6 | `build` | `assembleRelease` + `bundleRelease` con secrets de firma (solo `main`, depende de 3-5) |

Los jobs 3-5 se ejecutan en paralelo. El job 6 solo en `main` y requiere éxito de 3-5.

---

## Local Build Setup

```bash
# 1. Generar keystore debug (si no tienes uno)
keytool -genkey -v -keystore debug.keystore -alias androiddebugkey \
  -storepass android -keypass android -keyalg RSA -keysize 2048 \
  -validity 10000 -dname "CN=Debug, OU=Dev, O=MobileDevSecOps, L=City, S=State, C=US"

# 2. Copiar el template de propiedades de firma
cp keys.properties.example keys.properties

# 3. (Opcional) Editar keys.properties con tu keystore real
#    keystore.path=ruta/a/tu/keystore.jks
#    keystore.store.password=...
#    keystore.key.alias=...
#    keystore.key.password=...

# 4. Build de release firmado
./gradlew assembleRelease
```

> `keys.properties` está en `.gitignore` — nunca se sube al repositorio. En CI se usan los secrets de GitHub (`SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`).

**codeql.yml** — Análisis estático de seguridad:
- Lenguaje: `java-kotlin` (autobuild)
- Disparadores: push/PR a `main`/`develop` + semanal (lunes 06:00 UTC)
- Permisos: `security-events: write`

---

## Seguridad

| Práctica | Descripción |
|---|---|
| **CodeQL SAST** | Análisis estático de seguridad del código Kotlin vía GitHub Actions (semanal + push/PR) |
| **OWASP Dependency Check** | Escaneo de vulnerabilidades en dependencias; falla el build en CVSS ≥ 7.0 |
| **Detekt + Ktlint** | Análisis estático de código y estilo forzados en CI |
| **ProGuard/R8** | Ofuscación y minificación en build release |
| **Certificate Pinning** | 2 pins SHA-256 en `network_security_config.xml` para rotación segura, expiración 2027-12-31 |
| **EncryptedSharedPreferences** | Almacenamiento cifrado del JWT (AES256-GCM para valores, AES256-SIV para claves) |
| **Validación JWT** | Verificación estructural al guardar: algoritmo en header, exp/nbf en payload, formato de firma Base64-URL |
| **Rate limiting** | Máximo 5 intentos de login consecutivos, backoff exponencial (30s → 60s → 120s → ... → 16min) |
| **Token refresh** | Limpieza automática del token al recibir HTTP 401 del servidor |
| **Logging sanitizado** | Bearer token redactado en logs; solo primeros 2 caracteres del username en debug |
| **Sin backup** | `allowBackup="false"`, reglas de backup y extracción excluyen preferencias cifradas |
| **Tests de seguridad** | 59+ tests específicos: política de contraseñas, sanitización de input, sesión, logging, pinning, almacenamiento |

---

## API Endpoints

Base URL: `https://localhost:7227`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/Login/login` | Inicio de sesión (body: `User`, `Password`) |
| POST | `/api/v1/Logout/logout` | Cierre de sesión |
| GET | `/api/v1/Usuario?PageNumber=&PageSize=` | Listar usuarios (paginado) |
| POST | `/api/v1/Usuario` | Crear usuario |
| PUT | `/api/v1/Usuario/{id}` | Actualizar usuario |
| DELETE | `/api/v1/Usuario/{id}` | Eliminar usuario |

### Mock API (Docker)

```bash
docker compose up -d
# Credenciales: admin / Admin123!
# Endpoint: http://localhost:8080
# Las credenciales se configuran vía variables de entorno: MOCK_USER, MOCK_PASSWORD
```

---

## DevContainer

El proyecto incluye configuración para VS Code DevContainer:

- **Base**: Ubuntu 24.04
- **JDK**: 21 (Temurin)
- **Android SDK**: 36 + Build Tools 36.0.0
- **Gradle**: 9.4.1
- **Extensiones**: Kotlin, Gradle, EditorConfig for VS Code

Para usarlo, abre la carpeta en VS Code y ejecuta: `Reopen in Container`.

---

## Licencia

```text
MIT License

Copyright (c) 2026

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
