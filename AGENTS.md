# MobileDevSecOps - AGENTS

## Project Overview

Android application with DevSecOps practices. Manages user authentication and CRUD operations via a REST API, built with Jetpack Compose and Clean Architecture.

## Tech Stack

| Component | Version |
|---|---|
| Kotlin | 2.2.10 |
| AGP | 9.2.1 |
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

## Architecture

Clean Architecture with 3 layers:

- **domain/** - Business logic: models, repository interfaces, use cases. No Android dependencies.
- **data/** - Data sources: Ktor API clients (remote/), EncryptedSharedPreferences (local/), repository implementations.
- **ui/** - Presentation: Jetpack Compose screens + ViewModels per feature.

DI with Koin (AppModule.kt, NetworkModule.kt).

## Navigation

```
Login -> Index -> Usuario (paged list)
                   +-> CrearUsuario
                   +-> ActualizarUsuario(id, nombre, correo, rowVersion)
                   +-> EliminarUsuario(id, nombre, correo, rowVersion)
```

HTTP 401 triggers SessionExpiredException -> redirect to Login.

## Project Structure

```
MobileDevSecOps/
├── app/
│   ├── src/main/java/com/example/mobiledevsecops/
│   │   ├── di/          # Koin DI modules
│   │   ├── domain/      # Models, repos, use cases
│   │   ├── data/        # API, DTOs, TokenManager, repos
│   │   ├── ui/          # Compose screens + ViewModels
│   │   └── util/        # Logger
│   ├── src/test/        # Unit tests (JUnit4 + MockK)
│   └── src/androidTest/ # Instrumentation tests (Compose UI)
├── gradle/libs.versions.toml  # Version catalog
├── detekt.yml           # Static analysis config
├── dependency-check-suppressions.xml
├── docker-compose.yml   # Mock API service
├── mock-api/            # Ktor mock server for local dev
├── ci/Dockerfile        # CI Docker image (JDK + Android SDK)
└── .devcontainer/       # VS Code DevContainer
```

## Build Commands

```bash
# Full build
./gradlew assembleDebug

# Run all quality checks
./gradlew lint detekt ktlintCheck --no-daemon

# Unit tests + coverage
./gradlew testDebugUnitTest koverHtmlReport koverVerify --no-daemon

# Instrumentation tests (run on connected device)
./gradlew connectedDebugAndroidTest --no-daemon --no-configuration-cache

# Instrumentation tests (compile only)
./gradlew compileDebugAndroidTestSources --no-daemon

# Security scan
./gradlew dependencyCheckAnalyze --no-daemon

# Release build (requires signing keys)
./gradlew assembleRelease

# Release build con tu keystore real
cp keys.properties.example keys.properties
# Editar keys.properties con las credenciales de tu keystore
./gradlew assembleRelease
```

## Testing

- **Unit tests** (app/src/test/): JUnit4 + MockK + kotlinx-coroutines-test + Ktor MockEngine
- **Instrumentation tests** (app/src/androidTest/): Compose UI Test + Espresso
- **Coverage**: Kover (excludes BuildConfig, MainActivity, App, di.*, theme.*, util.*)
- Test helpers in shared/ (fakes, fixtures, rules) are duplicated in both test/ and androidTest/

## Code Conventions

- **No comments** in production code unless necessary
- Follow existing patterns (ViewModel per screen, sealed class for UI state, Result for use cases)
- Use version catalog (`libs.*`) for all dependencies
- `max_line_length = 160` (from .editorconfig)
- Detekt: WildcardImport active (excluded in tests), ForbiddenComment active (TODO/FIXME/STOPSHIP)
- Ktlint: enforced, fails build on violations

## API Endpoints (Backend)

Base URL: `https://localhost:7227`

| Method | Path | Description |
|---|---|---|
| POST | /api/v1/Login/login | Login (body: User, Password) |
| POST | /api/v1/Logout/logout | Logout |
| GET | /api/v1/Usuario?PageNumber=&PageSize= | List usuarios |
| POST | /api/v1/Usuario | Create usuario |
| PUT | /api/v1/Usuario/{id} | Update usuario |
| DELETE | /api/v1/Usuario/{id} | Delete usuario |

### Mock API (Docker)

```bash
docker compose up -d
# Credentials: admin / Admin123!
# Endpoint: http://localhost:8080
```

## CI Pipeline (.github/workflows/)

Jobs (parallel):
1. **validate-wrapper** - Gradle wrapper checksum validation
2. **codeql-sast** - CodeQL SAST for Kotlin (sec-events write, scheduled weekly + push/PR)
3. **static-analysis** - `lint detekt ktlintCheck`
4. **unit-test** - `testDebugUnitTest koverHtmlReport koverVerify`
5. **instrumentation-compile** - `compileDebugAndroidTestSources` (compile only)
6. **security-scan** - `dependencyCheckAnalyze`
7. **build** (main only) - `assembleRelease + bundleRelease` with signing secrets, depends on 3-6

## Local Build Setup

```bash
# Generar keystore debug por defecto
keytool -genkey -v -keystore debug.keystore -alias androiddebugkey \
  -storepass android -keypass android -keyalg RSA -keysize 2048 \
  -validity 10000 -dname "CN=Debug, OU=Dev, O=MobileDevSecOps, L=City, S=State, C=US"

# Copiar y configurar keys.properties
cp keys.properties.example keys.properties
# Editar keys.properties con las credenciales de tu keystore real

# Build de release firmado local
./gradlew assembleRelease
```

`keys.properties` está en `.gitignore` — nunca se sube al repositorio.

## Security

- CodeQL SAST (static application security testing) via GitHub Actions
- OWASP Dependency Check (fails on CVSS >= 7.0)
- EncryptedSharedPreferences for JWT token storage
- JWT structural validation on save (header alg, payload exp/nbf, signature format, Base64-URL)
- Rate limiting on login: max 5 attempts, exponential backoff (30s up to 16min)
- Token auto-cleared on server 401 via Ktor bearer refreshTokens
- ProGuard/R8 minification in release
- network_security_config.xml with certificate pinning (SHA-256)
- Detekt/ktlint enforced in CI
- No backup allowed (android:allowBackup="false")
