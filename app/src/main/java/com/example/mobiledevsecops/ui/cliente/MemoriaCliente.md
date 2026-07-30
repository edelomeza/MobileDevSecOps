## Resumen de lo realizado para **Cliente**

### Rama: `CrudCliente` (desde `main`)

### API
- Base URL cambiada a `https://webapidevopsproject-h5fn4.ondigitalocean.app`
- Endpoints: `/api/v1/Cliente` — GET (paginado), POST, PUT/{id}, DELETE/{id}

### Capa Domain (4 archivos nuevos)
| Archivo | Propósito |
|---|---|
| `Cliente.kt` | Modelo: `id`, `nombreCliente`, `direccionCliente`, `correoElectronico`, `numeroTelefono`, `rowVersion` |
| `ClientePage.kt` | Paginación: `items`, `pageNumber`, `totalPages`, `totalCount` |
| `ClienteRepository.kt` | Interfaz con 4 métodos: `buscar`, `crear`, `actualizar`, `eliminar` |
| `Buscar/Crear/Actualizar/EliminarClienteUseCase.kt` | 4 use cases |

### Capa Data (6 archivos nuevos)
| Archivo | Propósito |
|---|---|
| `ClienteDto.kt` + `ClienteListResponse` | DTOs con `@SerialName` para mapeo JSON |
| `ClienteCreateRequest.kt` | Request DTO para POST |
| `ClienteUpdateRequest.kt` | Request DTO para PUT |
| `ClienteDeleteRequest.kt` | Request DTO para DELETE |
| `ClienteApi.kt` | Ktor client — GET/POST/PUT/DELETE con bearer token |
| `ClienteRepositoryImpl.kt` | Implementación con mappers `toDomain()` |

### Capa UI (8 archivos nuevos)
| Archivo | Propósito |
|---|---|
| `ClienteScreen.kt` + `ClienteViewModel.kt` | Lista paginada con búsqueda por nombre, tabla (Nombre/E-Mail/Teléfono), FAB, flechas de navegación |
| `ClienteCrearScreen.kt` + `ClienteCrearViewModel.kt` | Formulario de creación con 5 campos |
| `ClienteActualizarScreen.kt` + `ClienteActualizarViewModel.kt` | Formulario de edición con `ClienteActualizarParams` |
| `ClienteEliminarScreen.kt` + `ClienteEliminarViewModel.kt` | Confirmación de eliminación con `ClienteEliminarParams` |

### Archivos modificados (4)
| Archivo | Cambio |
|---|---|
| `AppModule.kt` | Registros DI: ClienteApi, repositorio, 4 use cases, 4 ViewModels |
| `NavGraph.kt` | 4 rutas nuevas: `cliente/{page}`, `cliente/crear`, `cliente/actualizar/{id}`, `cliente/eliminar/{id}` |
| `IndexScreen.kt` | Item "Cliente" en el menú hamburguesa |
| `IndexViewModel.kt` | Propiedad `onNavigateToCliente` |

### Tests unitarios (9 archivos, ~83 tests)
| Archivo | Tests |
|---|---|
| `FakeClienteRepository.kt` | Fake con `MutableList` para tests |
| `ClienteFixtures.kt` | Objetos de prueba |
| `ClienteRepositoryImplTest.kt` | 11 tests (MockEngine) |
| `BuscarClientesUseCaseTest.kt` | 5 tests |
| `CrearClienteUseCaseTest.kt` | 7 tests |
| `ActualizarClienteUseCaseTest.kt` | 7 tests |
| `EliminarClienteUseCaseTest.kt` | 7 tests |
| `ClienteViewModelTest.kt` | 14 tests |
| `ClienteCrearViewModelTest.kt` | 11 tests |
| `ClienteActualizarViewModelTest.kt` | 12 tests |
| `ClienteEliminarViewModelTest.kt` | 9 tests |

### Tests de instrumentación
- `IndexScreenTest.kt` corregido: parámetro `onNavigateToCliente` + aserción `"Cliente"`

### Build results
| Comando | Resultado |
|---|---|
| `compileDebugKotlin` | ✅ |
| `testDebugUnitTest` | ✅ 331 tests |
| `lint detekt ktlintCheck` | ✅ |
| `compileDebugAndroidTestSources` | ✅ |

### Pendiente (preexistente, no relacionado)
- CertificatePinningTest (3 fallos) — falta `<pin-set>` en XML
- EmpleadoScreenTest (1 fallo) — falta `waitForIdle()`
