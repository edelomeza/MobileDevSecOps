# Memoria — Pruebas del Proyecto MobileDevSecOps

## 1. Resumen General

| Tipo | Tests | Archivos |
|------|:-----:|:--------:|
| Unitarias (test/) | 176 | 21 + 6 helpers |
| Instrumentadas (androidTest/) | 34 | 11 + 6 helpers |
| **Total** | **210** | **32 + 12 helpers** |

---

## 2. Pruebas Unitarias (`app/src/test/`)

### 2.1 DTO Serialization — `DtoSerializationTest` (19 tests)

**Propósito:** Verificar que los 8 DTOs del proyecto se serializan/deserializan correctamente con `kotlinx.serialization`, usando la misma configuración `Json { ignoreUnknownKeys = true; isLenient = true }` que `NetworkModule.kt`.

**DTOs cubiertos:**

| DTO | Tests | Escenarios |
|-----|:-----:|------------|
| `LoginRequest` | 2 | Serialización normal, campos vacíos |
| `LoginResponse` | 4 | Con token, token nulo, ambos nulos, campos desconocidos |
| `LoginResponse` | 1 | JSON inválido lanza `SerializationException` |
| `LogoutResponse` | 2 | Con mensaje, mensaje nulo |
| `UsuarioDto` | 4 | Con `RowVersion`, sin `RowVersion`, null→"" mapping, `@SerialName` |
| `UsuarioListResponse` | 2 | Con datos, lista vacía |
| `UserCreateRequest` | 2 | Normal, caracteres especiales |
| `UserUpdateRequest` | 1 | Serialización con todos los campos |
| `UserDeleteRequest` | 1 | Serialización con id y rowVersion |

**Problemas encontrados:** Ninguno.

---

### 2.2 Data Repository — `AuthRepositoryImplTest` + `UsuarioRepositoryImplTest` (19 tests)

**Propósito:** Verificar las implementaciones de repositorio con Ktor MockEngine: mapeo DTO→dominio, headers HTTP, manejo de errores (401 → `SessionExpiredException`, 409 → `ConflictException`, errores de red).

| Archivo | Tests | Propósito |
|---------|:-----:|-----------|
| `AuthRepositoryImplTest` | 10 | Login exitoso, error credenciales, network error, session expired, logout, sesión activa, múltiples intentos fallidos |
| `UsuarioRepositoryImplTest` | 9 | Listar usuarios, paginación, crear/actualizar/eliminar exitoso, conflictos (rowVersion), session expired, network error |

**Problema encontrado:** `HttpClient` sin `ContentNegotiation` falla con `IllegalStateException` al serializar bodies con `setBody()`.

**Solución:** Instalar `ContentNegotiation { json() }` en todos los `HttpClient` de test:

```kotlin
val client = HttpClient(MockEngine) {
    install(ContentNegotiation) { json() }
    engine { … }
}
```

---

### 2.3 Navegación — `NavGraphTest` (10 tests)

**Propósito:** Verificar constantes de ruta, builders (`navToUsuario`, `navToActualizar`, `navToEliminar`) y codificación URL de parámetros.

**Grupos de tests:**

| Grupo | Tests | Qué verifica |
|-------|:-----:|--------------|
| Constantes de ruta | 5 | `LOGIN`, `INDEX`, `USUARIO`, `USUARIO_CREAR`, placeholders |
| Builder `navToUsuario` | 2 | Page por defecto (1) y específico (3) |
| Builder `navToActualizar` | 3 | Valores simples, espacios en nombre, `+` en correo |
| Builder `navToEliminar` | 2 | Valores simples, espacios en nombre |

**Problema encontrado:** `android.net.Uri.encode()` no funciona en entorno JVM sin Android framework. Lanza `NullPointerException` en tests unitarios.

**Solución:** Usar `mockkStatic(Uri::class)` para mockear `Uri.encode()` como identity function:

```kotlin
@Before
fun setUp() {
    mockkStatic(Uri::class)
    every { Uri.encode(any()) } answers { firstArg() as String }
}

@After
fun tearDown() {
    unmockkStatic(Uri::class)
}
```

---

### 2.4 DI/Koin — `KoinModulesTest` (3 tests)

**Propósito:** Verificar que los módulos de Koin (`appModule` + `networkModule`) resuelven todas las dependencias correctamente.

**Tests:**

| Test | Verifica |
|------|----------|
| `todos los modulos resuelven todas las dependencias` | `AuthRepository`, `UsuarioRepository`, 5 use cases, `AuthApi`, `UsuarioApi`, `TokenManager`, `HttpClient` |
| `todos los viewModels se resuelven` | `LoginViewModel`, `IndexViewModel`, `UsuarioViewModel`, `UsuarioCrearViewModel` |
| `HttpClient se resuelve correctamente` | `HttpClient` no es nulo |

**Problema encontrado:** `appModule` depende de `HttpClient` (definido en `networkModule`) y `networkModule` depende de `TokenManager` (definido en `appModule`). Cargar un solo módulo produce `NoDefinitionFoundException`.

**Solución:** Cargar ambos módulos juntos en todos los tests:

```kotlin
startKoin {
    androidContext(mockContext)
    modules(appModule, networkModule)
}
```

**Problema adicional:** Los ViewModels necesitan `Dispatchers.Main` configurado, de lo contrario lanzan `IllegalStateException` en `HandlerDispatcher.kt`.

**Solución:** Reutilizar `MainCoroutineRule` existente en `shared/rule/`:

```kotlin
@get:Rule
val mainCoroutineRule = MainCoroutineRule()
```

---

### 2.5 Casos de Uso (5 archivos, 25 tests)

| Archivo | Tests | Propósito |
|---------|:-----:|-----------|
| `LoginUseCaseTest` | 4 | Success, error credenciales, network error, session expired |
| `LogoutUseCaseTest` | 1 | Success + limpia token |
| `CrearUsuarioUseCaseTest` | 9 | Success, validation (4 campos), repo error, session expired, errores múltiples |
| `ActualizarUsuarioUseCaseTest` | 5 | Success, id inválido, rowVersion vacío, conflicto, session expired |
| `EliminarUsuarioUseCaseTest` | 6 | Success, id inválido, rowVersion vacío, conflicto, session expired, ambos inválidos |

**Patrón común:** Todos usan `fakeRepo` (FakeAuthRepository / FakeUsuarioRepository) con flags (`shouldFail`, `shouldBeNetworkError`, etc.) y `runTest` de `kotlinx.coroutines.test`.

---

### 2.6 ViewModels (6 archivos, 53 tests)

| Archivo | Tests | Propósito |
|---------|:-----:|-----------|
| `LoginViewModelTest` | 10 | Estado inicial, cambios de campo, login exitoso, error, validación, network, auto-clear |
| `IndexViewModelTest` | 3 | Estado inicial, logout, sesión activa |
| `UsuarioViewModelTest` | 9 | Paginación (siguiente/anterior/bordes), error, session expired, back |
| `UsuarioCrearViewModelTest` | 12 | Estado inicial, cambios de campo, guardar (éxito/error/validation/session), cancelar, dismiss |
| `UsuarioActualizarViewModelTest` | 11 | Estado inicial, cambios de campo, actualizar (éxito/error/conflicto/session), cancelar, dismiss |
| `UsuarioEliminarViewModelTest` | 8 | Estado inicial, eliminar (éxito/error/conflicto/session), cancelar, dismiss, validación |

**Patrón común:** Usan `FakeAuthRepository` / `FakeUsuarioRepository` y `MainCoroutineRule` para el dispatcher.

---

### 2.7 Seguridad (4 archivos, 46 tests)

| Archivo | Tests | Propósito |
|---------|:-----:|-----------|
| `PasswordPolicyTest` | 14 | Validación de password: blank, longitud, boundary 8 chars, gaps documentados (mayúsculas, dígitos, especiales, comunes, secuenciales, repetidos, espacios, solo números, Unicode) |
| `InputSanitizationTest` | 16 | Validación de nombre (caracteres permitidos, inyección SQL/XSS/NoSQL/path traversal, longitud), email (formato, inyección, boundary 50 chars) |
| `SessionManagementTest` | 10 | Token save/clear/logout, session expired, gaps (sin refresh token, sin validación JWT, detección 401 frágil) |
| `SensitiveDataLoggingTest` | 6 | Password no expuesto en estado, username redactado en AuthRepo, gap (ViewModel no redacta username), logging debug-only |

**Nota:** Varios tests están marcados como `"GAP"` — documentan debilidades de seguridad conocidas pero no corregidas (password policy débil, sin refresh token, TokenManager sin validación de expiración).

---

## 3. Pruebas Instrumentadas (`app/src/androidTest/`)

### 3.1 UI Funcional — 15 tests

| Archivo | Tests | Propósito |
|---------|:-----:|-----------|
| `ui/login/LoginScreenTest.kt` | 3 | Renderizado, validación campos vacíos, snackbar en error |
| `ui/index/IndexScreenTest.kt` | 2 | Bienvenida, drawer de navegación |
| `ui/usuario/UsuarioScreenTest.kt` | 2 | Título, total y controles de paginación |
| `ui/usuariocrear/UsuarioCrearScreenTest.kt` | 3 | Campos, validaciones required, email inválido |
| `ui/usuarioactualizar/UsuarioActualizarScreenTest.kt` | 3 | Campos pre-rellenados, nombre vacío, email inválido |
| `ui/usuarioeliminar/UsuarioEliminarScreenTest.kt` | 2 | Confirmación, botón eliminar |

### 3.2 Seguridad — 19 tests

| Archivo | Tests | Propósito |
|---------|:-----:|-----------|
| `security/network/CertificatePinningTest.kt` | 8 | `network_security_config.xml`: estructura, pin SHA-256, expiración, cleartext, gap de backup pin |
| `security/storage/EncryptedStorageTest.kt` | 8 | `TokenManager` con almacenamiento cifrado real: save/get/null/clear/isLoggedIn/token vacío/JWT expirado/persistencia |
| `security/ui/PasswordVisibilityTest.kt` | 3 | Toggle de visibilidad de contraseña: icono visible, cambio al alternar, campo acepta input |

### 3.3 Helpers Compartidos (androidTest)

| Archivo | Propósito |
|---------|-----------|
| `shared/security/SecurityFixtures.kt` | Tokens JWT válido/expirado para `EncryptedStorageTest` |
| `shared/security/FakeTokenManager.kt` | Fake in-memory de `TokenManager` |
| `shared/fake/FakeAuthRepository.kt` | Fake de `AuthRepository` para LoginScreenTest |
| `shared/fake/FakeUsuarioRepository.kt` | Fake de `UsuarioRepository` para CRUD screen tests |
| `shared/fixture/UsuarioFixtures.kt` | Fixtures de `Usuario`, `UsuarioDto`, `UsuarioPage` |
| `shared/rule/MainCoroutineRule.kt` | Regla para tests con corrutinas |

---

## 4. Problemas Encontrados y Soluciones

### 4.1 DEX: espacios en nombres de método (androidTest)

```
Error: Space characters in SimpleName 'token guardado puede ser recuperado'
are not allowed prior to DEX version 040
```

**Causa:** Los métodos en instrumentados se compilan a DEX, que no permite espacios ni backticks.

**Solución:** Usar `snake_case` en todos los nombres de método:

```kotlin
// ❌ Incorrecto (unit test style)
@Test
fun `token guardado puede ser recuperado`()

// ✅ Correcto (androidTest style)
@Test
fun token_guardado_puede_ser_recuperado()
```

**Archivos afectados:** `EncryptedStorageTest.kt`, `CertificatePinningTest.kt`, `PasswordVisibilityTest.kt`

---

### 4.2 XML binario AAPT2 en recursos res/xml/ (androidTest)

```
org.xml.sax.SAXParseException: Unexpected token
(position:TEXT ...)
```

**Causa:** AAPT2 compila `res/xml/` a formato binario. `context.resources.openRawResource(R.xml.archivo)` retorna el XML binario, no texto plano. `DocumentBuilder.parse()` espera XML textual.

**Solución:** Usar `context.resources.getXml(R.xml.archivo)` que retorna `XmlResourceParser`:

```kotlin
val parser = context.resources.getXml(R.xml.network_security_config)
var eventType = parser.eventType
while (eventType != XmlPullParser.END_DOCUMENT) {
    if (eventType == XmlPullParser.START_TAG && parser.name == "network-security-config") {
        // Encontrado
    }
    eventType = parser.next()
}
parser.close()
```

**Archivo afectado:** `CertificatePinningTest.kt` (reescrito con pull parser)

---

### 4.3 Campos de texto pre-rellenados en Compose Test (androidTest)

```
java.lang.AssertionError: ... contains 'El nombre es obligatorio' ... is not displayed!
```

**Causa:** `performTextInput("")` concatena una cadena vacía (no-op). El campo ya contenía "Juan Pérez", nunca se vació.

**Solución:** Usar `performTextClearance()` para limpiar antes de escribir:

```kotlin
composeTestRule.onNodeWithText("Nombre").performTextClearance()
composeTestRule.onNodeWithText("Nombre").performTextInput("Nuevo Nombre")
```

**Nota:** No se puede encadenar (`.performTextClearance().performTextInput(...)`) porque `performTextInput` retorna `Unit`.

**Archivo afectado:** `UsuarioActualizarScreenTest.kt`

---

### 4.4 META-INF/LICENSE.md duplicados (androidTest)

```
Duplicate META-INF/LICENSE.md
```

**Causa:** Múltiples dependencias incluyen el mismo archivo LICENSE.

**Solución:** Agregar bloque `packaging` en `app/build.gradle.kts`:

```kotlin
android {
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE.md"
            )
        }
    }
}
```

---

### 4.5 Uri.encode() sin Android framework (unit test — NavGraph)

**Causa:** `android.net.Uri.encode()` no está disponible en entorno JVM puro. Lanza `NullPointerException`.

**Solución:** Mockear con `mockkStatic`:

```kotlin
mockkStatic(Uri::class)
every { Uri.encode(any()) } answers { firstArg() as String }
```

**Archivo afectado:** `NavGraphTest.kt`

---

### 4.6 Koin cross-dependency entre módulos (unit test — DI)

**Causa:** `appModule` depende de `HttpClient` (en `networkModule`) y `networkModule` depende de `TokenManager` (en `appModule`). Cargar uno solo produce `NoDefinitionFoundException`.

**Solución:** Cargar ambos módulos juntos:

```kotlin
modules(appModule, networkModule)
```

**Archivo afectado:** `KoinModulesTest.kt`

---

### 4.7 `@ExperimentalCoroutinesApi` warning en MainCoroutineRule (test + androidTest)

```
This declaration needs opt-in. Its usage should be marked with
'@kotlinx.coroutines.ExperimentalCoroutinesApi'
```

**Causa:** `UnconfinedTestDispatcher()` en el constructor está marcado como experimental. El `@OptIn` estaba solo en métodos, no cubría el parámetro del constructor.

**Solución:** Mover `@OptIn` al nivel de clase:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainCoroutineRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() { ... }
```

**Archivos afectados:** `shared/rule/MainCoroutineRule.kt` (en test/ y androidTest/)

---

### 4.8 SharedFlow: recolectar antes de emitir (unit test — ViewModels)

**Causa:** `MutableSharedFlow()` con `replay=0` (configuración por defecto) pierde eventos si se emite antes de que el colector esté suscrito. Los tests lanzaban la acción primero y luego verificaban el evento.

**Solución:** Recolectar el `SharedFlow` **antes** de ejecutar la acción que dispara el evento:

```kotlin
// ✅ Correcto: recolectar antes de trigger
val events = mutableListOf<UsuarioCrearEvent>()
backgroundScope.launch(UnconfinedTestDispatcher()) {
    viewModel.events.collect { events.add(it) }
}

// Luego disparar la acción
viewModel.guardar()
```

**Archivos afectados:** `UsuarioCrearViewModelTest.kt`, `UsuarioActualizarViewModelTest.kt`, `UsuarioEliminarViewModelTest.kt`

---

### 4.9 Ktor MockEngine sin ContentNegotiation (unit test — Data Repository)

**Causa:** `HttpClient(MockEngine)` sin `ContentNegotiation` lanza `IllegalStateException` al usar `setBody(UserCreateRequest)` porque no sabe serializar el objeto.

**Solución:** Instalar `ContentNegotiation { json() }` en todos los `HttpClient` de test:

```kotlin
val client = HttpClient(MockEngine) {
    install(ContentNegotiation) { json() }
    engine { … }
}
```

**Archivos afectados:** `AuthRepositoryImplTest.kt`, `UsuarioRepositoryImplTest.kt`

---

### 4.10 `waitForIdle()` faltante en Compose tests (androidTest)

**Causa:** En dispositivos reales, `createComposeRule().setContent{}` puede no haber terminado de componer antes de que el test intente interactuar con la UI, produciendo `No compose hierarchies found`.

**Solución:** Agregar `waitForIdle()` inmediatamente después de `setContent`:

```kotlin
composeTestRule.setContent { MiScreen() }
composeTestRule.waitForIdle()
```

**Archivo afectado:** `UsuarioEliminarScreenTest.kt`

---

### 4.11 Koin `allowOverride` para mocks en tests (unit test — DI)

**Causa:** Al recargar módulos de Koin con `startKoin { modules(…) }`, si una definición ya existe, Koin lanza `DefinitionOverrideException` a menos que se permita explícitamente la sobreescritura.

**Solución:** Usar `allowOverride(true)` y proveer un mock completo que incluya todos los métodos requeridos:

```kotlin
startKoin {
    allowOverride(true)
    modules(appModule, networkModule)
}
```

**Archivo afectado:** `KoinModulesTest.kt`

---

### 4.12 Kover incompatible con Kotlin 2.2.10

**Causa:** Kover 0.9.1 no genera cobertura con Kotlin 2.2.10 — el reporte HTML aparece vacío ("No coverage information was found").

**Solución:** Actualizar Kover a 0.9.8 en `gradle/libs.versions.toml`:

```toml
kover = "0.9.8"
```

**Archivo afectado:** `gradle/libs.versions.toml`

---

## 5. Buenas Prácticas

### 5.1 Nomenclatura

| Ámbito | Formato | Ejemplo |
|--------|---------|---------|
| **Unit test** (`test/`) | backticks + español | `` `login exitoso retorna Success` `` |
| **Instrumentado** (`androidTest/`) | snake_case | `login_exitoso_retorna_success` |

Razón: DEX no permite espacios ni backticks en nombres de método.

### 5.2 Recursos XML compilados (androidTest)

Usar `context.resources.getXml(R.xml.archivo)` con `XmlPullParser` en vez de `openRawResource` + `DocumentBuilder`. Los recursos `res/xml/` son compilados a binario por AAPT2.

### 5.3 Interacción con TextField pre-rellenados (androidTest)

Siempre usar `performTextClearance()` antes de `performTextInput()` cuando se quiere reemplazar el contenido. No se puede encadenar.

### 5.4 APIs de Android ausentes en JVM (unit test)

Usar `mockkStatic(Clase::class)` para mockear métodos de Android que no tienen implementación real en el SDK stub (ej: `Uri.encode()`).

### 5.5 Dependencias cruzadas en Koin (unit test)

Si los módulos de Koin tienen dependencias entre sí, cargarlos juntos en los tests. No asumir que un módulo es autosuficiente.

### 5.6 Dispatcher Main en ViewModels (unit test)

Siempre incluir `MainCoroutineRule` (de `shared/rule/`) cuando se testean ViewModels que usan corrutinas con `Dispatchers.Main`.

### 5.7 Helpers duplicados en test/ y androidTest/

Copiar helpers en ambos source sets (`test/` y `androidTest/`) — son source sets separados y no comparten código automáticamente. Mantenerlos sincronizados.

### 5.8 Compilación previa (androidTest)

Verificar compilación primero antes de ejecutar en dispositivo:

```bash
./gradlew :app:compileDebugAndroidTestSources
```

### 5.9 Ejecución de tests específicos (androidTest)

No usar `--tests` con `connectedDebugAndroidTest`. Usar:

```bash
./gradlew :app:connectedDebugAndroidTest
-Pandroid.testInstrumentationRunnerArguments.class=com.example.mobiledevsecops.security.network.CertificatePinningTest
```

### 5.10 Packaging excludes

Agregar `META-INF/LICENSE.md`, `META-INF/LICENSE-notice.md` y `META-INF/NOTICE.md` a `packaging.resources.excludes` en `build.gradle.kts` para evitar duplicados.

---

## 6. Estructura de Archivos

```
app/src/
├── test/java/com/example/mobiledevsecops/
│   ├── ExampleUnitTest.kt                        ← 1 test
│   ├── data/
│   │   ├── remote/dto/
│   │   │   └── DtoSerializationTest.kt          ← 19 tests
│   │   └── repository/
│   │       ├── AuthRepositoryImplTest.kt         ← 10 tests
│   │       └── UsuarioRepositoryImplTest.kt      ← 9 tests
│   ├── di/
│   │   └── KoinModulesTest.kt                   ← 3 tests
│   ├── domain/usecase/
│   │   ├── LoginUseCaseTest.kt                   ← 4 tests
│   │   ├── LogoutUseCaseTest.kt                  ← 1 test
│   │   ├── CrearUsuarioUseCaseTest.kt            ← 9 tests
│   │   ├── ActualizarUsuarioUseCaseTest.kt       ← 5 tests
│   │   └── EliminarUsuarioUseCaseTest.kt         ← 6 tests
│   ├── security/
│   │   ├── validation/
│   │   │   ├── PasswordPolicyTest.kt             ← 14 tests
│   │   │   └── InputSanitizationTest.kt          ← 16 tests
│   │   ├── session/SessionManagementTest.kt      ← 10 tests
│   │   └── logging/SensitiveDataLoggingTest.kt   ← 6 tests
│   ├── shared/
│   │   ├── fake/
│   │   │   ├── FakeAuthRepository.kt
│   │   │   └── FakeUsuarioRepository.kt
│   │   ├── fixture/UsuarioFixtures.kt
│   │   ├── rule/MainCoroutineRule.kt
│   │   └── security/
│   │       ├── FakeTokenManager.kt
│   │       └── SecurityFixtures.kt
│   ├── ui/
│   │   ├── login/LoginViewModelTest.kt           ← 10 tests
│   │   ├── index/IndexViewModelTest.kt           ← 3 tests
│   │   ├── usuario/UsuarioViewModelTest.kt       ← 9 tests
│   │   ├── usuariocrear/UsuarioCrearViewModelTest.kt         ← 12 tests
│   │   ├── usuarioactualizar/UsuarioActualizarViewModelTest.kt ← 11 tests
│   │   └── navigation/NavGraphTest.kt           ← 10 tests
│   │   └── usuarioeliminar/UsuarioEliminarViewModelTest.kt   ← 8 tests
│
└── androidTest/java/com/example/mobiledevsecops/
    ├── ExampleInstrumentedTest.kt
    ├── security/
    │   ├── network/CertificatePinningTest.kt
    │   ├── storage/EncryptedStorageTest.kt
    │   └── ui/PasswordVisibilityTest.kt
    ├── shared/
    │   ├── fake/
    │   │   ├── FakeAuthRepository.kt
    │   │   └── FakeUsuarioRepository.kt
    │   ├── fixture/UsuarioFixtures.kt
    │   ├── rule/MainCoroutineRule.kt
    │   └── security/
    │       ├── FakeTokenManager.kt
    │       └── SecurityFixtures.kt
    └── ui/
        ├── login/LoginScreenTest.kt
        ├── index/IndexScreenTest.kt
        ├── usuario/UsuarioScreenTest.kt
        ├── usuariocrear/UsuarioCrearScreenTest.kt
        ├── usuarioactualizar/UsuarioActualizarScreenTest.kt
        └── usuarioeliminar/UsuarioEliminarScreenTest.kt
```

---

## 7. Resultados en Dispositivo

```
Starting 34 tests on moto g72 - 13
Tests 20/34 completed (0 skipped, 0 failed)
Tests 26/34 completed (0 skipped, 0 failed)
Tests 30/34 completed (0 skipped, 0 failed)
Finished 34 tests on moto g72 - 13
BUILD SUCCESSFUL in 4m 34s

**34 tests instrumentados ejecutados — 0 fallos.**
```

Unit tests ejecutados: **176 — 0 fallos**.

**Total general: 210 tests — 0 fallos.**
