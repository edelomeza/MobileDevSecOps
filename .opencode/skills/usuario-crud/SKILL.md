---
name: usuario-crud
description: Guia completa del CRUD de Usuario - Arquitectura Clean Architecture con Jetpack Compose, Ktor, Koin
---

## Overview

Documentacion completa del proceso CRUD de Usuario en MobileDevSecOps. Sigue patron Clean Architecture con 3 capas: Domain, Data, UI.

## Arquitectura

```
Domain (Logica de Negocio)
  ├── Model: Usuario, UsuarioPage
  ├── Repository: UsuarioRepository (interface)
  └── UseCases: Crear, Actualizar, Eliminar

Data (Fuentes de Datos)
  ├── Remote: UsuarioApi (Ktor Client)
  ├── DTOs: UsuarioDto, UserCreateRequest, UserUpdateRequest, UserDeleteRequest
  ├── Repository: UsuarioRepositoryImpl
  └── Exceptions: SessionExpiredException, ConflictException

UI (Presentacion)
  ├── Screens: UsuarioScreen, UsuarioCrearScreen, UsuarioActualizarScreen, UsuarioEliminarScreen
  ├── ViewModels: UsuarioViewModel, UsuarioCrearViewModel, UsuarioActualizarViewModel, UsuarioEliminarViewModel
  └── Navigation: NavGraph

DI (Dependency Injection)
  ├── AppModule: Registra Repository, UseCases, ViewModels
  └── NetworkModule: HttpClient con Ktor (Auth Bearer, Timeout, Retry)
```

## Modelo de Datos

### Usuario (Domain)
```kotlin
data class Usuario(
    val id: Int = 0,
    val strNombre: String,
    val strCorreoElectronico: String,
    val rowVersion: String = ""
)
```

### UsuarioPage (Domain)
```kotlin
data class UsuarioPage(
    val items: List<Usuario>,
    val totalCount: Int,
    val pageNumber: Int,
    val totalPages: Int
)
```

### DTOs (Data)
- `UsuarioDto` - Respuesta de API con @Serializable
- `UsuarioListResponse` - Respuesta paginada
- `UserCreateRequest` - Request para crear (strNombre, strPWD, strCorreoElectronico)
- `UserUpdateRequest` - Request para actualizar (id, strNombre, strPWD, strCorreoElectronico, rowVersion)
- `UserDeleteRequest` - Request para eliminar (id, rowVersion)

## Validaciones

### CrearUsuarioUseCase
| Campo | Regla |
|-------|-------|
| strNombre | No vacio, max 50 chars, regex `[\p{L}0-9_ ]` |
| strPWD | No vacio, 8-128 chars, 1 mayuscula, 1 digito, 1 especial, sin espacios |
| strCorreoElectronico | No vacio, max 50, formato email `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$` |

### ActualizarUsuarioUseCase
- Mismas validaciones que Crear
- Adicional: `id > 0` y `rowVersion` no vacio

### EliminarUsuarioUseCase
- `id > 0`
- `rowVersion` no vacio

## Flujo CRUD

### Navegacion
```
Login -> Index -> Usuario (paged list)
                   +-> CrearUsuario
                   +-> ActualizarUsuario(id, nombre, correo, rowVersion)
                   +-> EliminarUsuario(id, nombre, correo, rowVersion)
```

### Flujo Crear
1. UsuarioScreen: FAB (+) -> navega a UsuarioCrearScreen
2. UsuarioCrearScreen: Llena formulario -> onGuardarClicked()
3. UsuarioCrearViewModel: valida con CrearUsuarioUseCase.validar() -> llama use case
4. UseCase: valida de nuevo -> llama UsuarioRepository.crearUsuario()
5. Repository: crea UserCreateRequest -> llama UsuarioApi.crearUsuario()
6. API: HTTP POST /api/v1/Usuario con Bearer token
7. Respuesta: 201 Created -> UsuarioCreado event
8. NavGraph: popBackStack + reloadSignal=true + operationResult="success"

### Flujo Actualizar
1. UsuarioScreen: IconButton Edit -> navega a UsuarioActualizarScreen/{id}
2. UsuarioActualizarScreen: Muestra datos actuales -> modifica -> onActualizarClicked()
3. Similar a Crear pero con HTTP PUT /api/v1/Usuario/{id}

### Flujo Eliminar
1. UsuarioScreen: IconButton Delete -> navega a UsuarioEliminarScreen/{id}
2. UsuarioEliminarScreen: Muestra datos (solo lectura) -> confirmacion -> onEliminarClicked()
3. Similar a Crear pero con HTTP DELETE /api/v1/Usuario/{id}

## Seguridad

### JWT Authentication
- Bearer plugin en Ktor: adjunta token automaticamente a cada request
- Token almacenado en EncryptedSharedPreferences via TokenManager
- sendWithoutRequest: false (no envia token si no existe)

### Session Expired
- HTTP 401 -> lanza SessionExpiredException
- Bearer.refreshTokens limpia token via TokenManager.clearAll()
- Use case retorna SessionExpired
- ViewModel emite evento SessionExpired
- NavGraph navega a LOGIN con popUpTo(0) { inclusive = true }

### Concurrency Control
- rowVersion se obtiene al listar/obtener usuario
- Se envia en PUT y DELETE
- HTTP 409 Conflict -> ConflictException -> "El registro ha sido modificado por otro usuario"

## Endpoints API

| Metodo | Endpoint | Body | Response |
|--------|----------|------|----------|
| GET | /api/v1/Usuario?PageNumber=&PageSize= | - | UsuarioListResponse |
| POST | /api/v1/Usuario | UserCreateRequest | UsuarioDto (201) |
| PUT | /api/v1/Usuario/{id} | UserUpdateRequest | UsuarioDto |
| DELETE | /api/v1/Usuario/{id} | UserDeleteRequest | LogoutResponse |

Base URL: `https://localhost:7227` (produccion) o `http://localhost:8080` (mock)

## DI (Koin)

### AppModule
| Tipo | Clave | Registro |
|------|-------|----------|
| Repository | UsuarioRepository | single |
| API | UsuarioApi | factory |
| UseCase | CrearUsuarioUseCase | factory |
| UseCase | ActualizarUsuarioUseCase | factory |
| UseCase | EliminarUsuarioUseCase | factory |
| ViewModel | UsuarioViewModel | viewModel |
| ViewModel | UsuarioCrearViewModel | viewModel |
| ViewModel | UsuarioActualizarViewModel | viewModel (parametersOf) |
| ViewModel | UsuarioEliminarViewModel | viewModel (parametersOf) |

### NetworkModule
- HttpClient con: ContentNegotiation, HttpTimeout (15s/30s/15s), HttpRequestRetry (3), Auth Bearer, Logging

## UI Pattern

### ViewModel Pattern
```kotlin
// UiState - Estado inmutable del UI
data class XxxUiState(
    val field: String = "",
    val fieldError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Event - Eventos de un solo uso
sealed class XxxEvent {
    data object NavigateBack
    data object Success
    data object SessionExpired
}

// StateFlow para estado, SharedFlow para eventos
val uiState: StateFlow<XxxUiState>
val events: SharedFlow<XxxEvent>
```

### Screen Pattern
```kotlin
@Composable
fun XxxScreen(
    onNavigateBack: () -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: XxxViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // LaunchedEffect para eventos de un solo uso
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is XxxEvent.NavigateBack -> onNavigateBack()
                is XxxEvent.SessionExpired -> onSessionExpired()
            }
        }
    }
}
```

## Tests

### Unit Tests (app/src/test/)
- UseCases: CrearUsuarioUseCaseTest (9), ActualizarUsuarioUseCaseTest (5), EliminarUsuarioUseCaseTest (6)
- Repository: UsuarioRepositoryImplTest (9) - usa Ktor MockEngine
- ViewModels: UsuarioViewModelTest (9), UsuarioCrearViewModelTest (10), UsuarioActualizarViewModelTest (10), UsuarioEliminarViewModelTest (8)

### Instrumentation Tests (app/src/androidTest/)
- UsuarioScreenTest (2), UsuarioCrearScreenTest (3), UsuarioActualizarScreenTest (3), UsuarioEliminarScreenTest (2)

### Test Helpers
- FakeUsuarioRepository - Implementacion fake con flags shouldThrowException, shouldThrowSessionExpired, shouldThrowConflict
- UsuarioFixtures - Datos de prueba (usuario, usuarioDto, usuarioPage)
- MainCoroutineRule - JUnit Rule para Dispatchers.Main con TestDispatcher

## Mock API

### Stack
- Ktor Server + Netdy
- Puerto: 8080
- Base de datos en memoria con 20 usuarios pre-cargados

### Credenciales
- Usuario: admin (configurable via MOCK_USER env var)
- Password: Admin123! (configurable via MOCK_PASSWORD env var)

### Endpoints Mock
```bash
# Login
POST /api/v1/Login/login
Body: { "User": "admin", "Password": "Admin123!" }
Response: { "token": "eyJ..." }

# CRUD
GET    /api/v1/Usuario?PageNumber=1&PageSize=8
POST   /api/v1/Usuario
PUT    /api/v1/Usuario/{id}
DELETE /api/v1/Usuario/{id}
```

### Docker
```bash
docker compose up -d
# Endpoint: http://localhost:8080
```

## Comandos

```bash
# Build completo
./gradlew assembleDebug

# Quality checks
./gradlew lint detekt ktlintCheck --no-daemon

# Unit tests + coverage
./gradlew testDebugUnitTest koverHtmlReport koverVerify --no-daemon

# Instrumentation tests (compilar)
./gradlew compileDebugAndroidTestSources --no-daemon

# Instrumentation tests (ejecutar)
./gradlew connectedDebugAndroidTest --no-daemon --no-configuration-cache

# Security scan
./gradlew dependencyCheckAnalyze --no-daemon

# Release build
./gradlew assembleRelease
```

## Archivos Clave

### Domain
- `app/src/main/java/.../domain/model/Usuario.kt`
- `app/src/main/java/.../domain/model/UsuarioPage.kt`
- `app/src/main/java/.../domain/repository/UsuarioRepository.kt`
- `app/src/main/java/.../domain/usecase/CrearUsuarioUseCase.kt`
- `app/src/main/java/.../domain/usecase/ActualizarUsuarioUseCase.kt`
- `app/src/main/java/.../domain/usecase/EliminarUsuarioUseCase.kt`

### Data
- `app/src/main/java/.../data/remote/UsuarioApi.kt`
- `app/src/main/java/.../data/remote/dto/*.kt` (5 DTOs)
- `app/src/main/java/.../data/repository/UsuarioRepositoryImpl.kt`

### UI
- `app/src/main/java/.../ui/usuario/UsuarioViewModel.kt`
- `app/src/main/java/.../ui/usuario/UsuarioScreen.kt`
- `app/src/main/java/.../ui/usuariocrear/UsuarioCrearViewModel.kt`
- `app/src/main/java/.../ui/usuariocrear/UsuarioCrearScreen.kt`
- `app/src/main/java/.../ui/usuarioactualizar/UsuarioActualizarViewModel.kt`
- `app/src/main/java/.../ui/usuarioactualizar/UsuarioActualizarScreen.kt`
- `app/src/main/java/.../ui/usuarioeliminar/UsuarioEliminarViewModel.kt`
- `app/src/main/java/.../ui/usuarioeliminar/UsuarioEliminarScreen.kt`

### Navigation
- `app/src/main/java/.../ui/navigation/NavGraph.kt`

### DI
- `app/src/main/java/.../di/AppModule.kt`
- `app/src/main/java/.../di/NetworkModule.kt`

## Cuando Usar Este Skill

- Cuando necesites crear un CRUD similar (ej: Empleado, Producto)
- Cuando necesites entender la arquitectura del proyecto
- Cuando necesites agregar un nuevo endpoint o pantalla
- Cuando necesites escribir tests para un componente CRUD
- Cuando necesites debugear problemas de sesion o concurrencia
