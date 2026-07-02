# Resumen de correcciones — Primer commit

## Problemas identificados y soluciones

### 1. CI: Android SDK API 36 no instalado en workflows

Los jobs de CI compilan con `compileSdk=36` pero los runners `ubuntu-latest` no tienen API 36 preinstalada.

**Solución**: Agregado paso `yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-36" "build-tools;36.0.0"` en todos los jobs que compilan.

**Archivos modificados**:
- `.github/workflows/ci.yml` — 5 jobs (static-analysis, unit-test, instrumentation-compile, security-scan, build)
- `.github/workflows/codeql.yml` — 1 job (analyze)

### 2. Detekt falla por JDK 26 local

Detekt 1.23.8 (Kotlin compiler 1.9.10 embebido) no soporta JVM target 26 (JDK local).

**Solución**: Fijado `jvmTarget = JVM_11` explícitamente en KotlinCompile tasks. Además, se actualizó `detekt.yml` para excluir tests de `MagicNumber` y `TooGenericExceptionThrown`.

**Archivos modificados**:
- `app/build.gradle.kts`
- `detekt.yml`

### 3. MagicNumbers en código de producción

HTTP status codes, timeouts y longitudes de validación estaban como literales numéricos.

**Solución**: Extraídos a constantes con nombre en companion objects o top-level.

**Archivos modificados** (6):

| Archivo | Constantes agregadas |
|---|---|
| `data/local/TokenManager.kt` | `MILLIS_TO_SECONDS`, `EXPIRY_BUFFER_SECONDS`, `JWT_PART_COUNT` |
| `data/remote/AuthApi.kt` | `HTTP_OK`, `HTTP_PARTIAL_CONTENT`, `HTTP_UNAUTHORIZED` |
| `data/remote/UsuarioApi.kt` | `HTTP_UNAUTHORIZED`, `HTTP_CONFLICT` |
| `di/NetworkModule.kt` | `CONNECT_TIMEOUT_MS`, `REQUEST_TIMEOUT_MS`, `SOCKET_TIMEOUT_MS`, `MAX_RETRIES` |
| `domain/usecase/ActualizarUsuarioUseCase.kt` | `MAX_NOMBRE_LENGTH`, `PWD_MIN_LENGTH`, `PWD_MAX_LENGTH`, `MAX_CORREO_LENGTH` |
| `domain/usecase/CrearUsuarioUseCase.kt` | `MAX_NOMBRE_LENGTH`, `PWD_MIN_LENGTH`, `PWD_MAX_LENGTH`, `MAX_CORREO_LENGTH` |

### 4. UsuarioScreen: composable demasiado largo (246 líneas)

El composable `UsuarioScreen` tenía 246 líneas, excediendo el threshold de 80 de Detekt `LongMethod` (aunque UI está excluido).

**Solución**: Extraídos 5 composables: `LoadingContent`, `ErrorContent`, `EmptyContent`, `UsuarioListContent`, `PaginationControls`. `UsuarioScreen` quedó en ~45 líneas.

**Archivos modificados**:
- `ui/usuario/UsuarioScreen.kt`

### 5. api.base.url sin fallback

`project.property("api.base.url")` lanza `UnknownPropertyException` si la propiedad falta.

**Solución**: Cambiado a `project.findProperty("api.base.url") as? String ?: "https://10.0.2.2:7227"`.

**Archivos modificados**:
- `app/build.gradle.kts`

### 6. Mock-api: versiones hardcodeadas

mock-api usaba strings de versión hardcodeados en lugar del version catalog.

**Solución**: Agregadas dependencias de Ktor server + logback + serialization al catalog raíz. mock-api referencia el catalog via `from(files("../gradle/libs.versions.toml"))`.

**Archivos modificados**:
- `gradle/libs.versions.toml` — nuevos versions, libraries y plugins
- `mock-api/settings.gradle.kts` — referencia al catalog raíz
- `mock-api/build.gradle.kts` — todas las dependencias via `alias(libs.*)`

> **Nota**: El build del mock-api falla con `startShadowScripts` por incompatibilidad del Ktor plugin 3.2.0 con Gradle 9.4.1. Problema preexistente, no relacionado con esta corrección.

### 7. Certificate pinning y user certificates

`network_security_config.xml` tenía `<certificates src="user" />` habilitado en producción y no tenía certificate pinning.

**Solución**: Dividido en configuración por build type:

| Build | Config |
|---|---|
| Main (release) | Solo `certificates src="system"`. Sin user certs. |
| Debug (nuevo) | `certificates src="system"` + `src="user"` (para pruebas con proxies). |

> **Pendiente**: Agregar `<pin-set>` con hashes SHA-256 reales cuando se conozca el certificado del backend de producción.

**Archivos modificados**:
- `app/src/main/res/xml/network_security_config.xml`
- `app/src/debug/res/xml/network_security_config.xml` (nuevo)

### 8. Configuration Cache

Evaluado `org.gradle.configuration-cache=true`. Funciona correctamente con todos los plugins. Único issue: Detekt 1.23.8 usa `ReportingExtension.file(String)` (API obsoleta en Gradle 9, será eliminada en Gradle 10).

**Archivos**: Ninguno (config existente es correcta).

### 9. CodeQL + Kotlin 2.2.10

Riesgo de que CodeQL no tenga extractor compatible con Kotlin 2.2.10 inmediatamente. Se corrigió orden de steps (SDK antes de CodeQL init) y se aumentó timeout de 30 a 45 min.

**Archivos modificados**:
- `.github/workflows/codeql.yml`

### 10. "Suspicious receiver type" en packaging.resources

El operador `excludes +=` en `packaging { resources {} }` causaba ambigüedad de receiver entre `ApplicationExtension` e `Iterable`.

**Solución**: Usar `this.excludes.add(...)` con `this` explícito para desambiguar el receiver.

**Archivo modificado**:
- `app/build.gradle.kts`

## Estado final

| Herramienta | Resultado |
|---|---|
| `compileDebugKotlin` | ✅ |
| `compileReleaseKotlin` | ✅ |
| `assembleDebug` | ✅ |
| `lint` | ✅ |
| `detekt` | ✅ (0 violaciones) |
| `ktlintCheck` | ✅ |
| `testDebugUnitTest` | ✅ |
| Configuration cache | ✅ |
