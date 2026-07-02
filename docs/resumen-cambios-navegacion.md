# Resumen de cambios - Flujo de navegación y actualización de estado

## Problema original

Al crear o actualizar un usuario y regresar a la lista principal, los datos no se refrescaban automáticamente. Adicionalmente, cuando sí había recarga (eliminación), siempre volvía a la página 1 en vez de preservar la página actual.

## Solución aplicada

Se estandarizó el mecanismo de recarga para los tres flujos (crear, actualizar, eliminar) mediante `savedStateHandle` y se corrigió la recarga para preservar la página actual.

## Archivos modificados (4)

| Archivo | Cambio |
|---|---|
| `ui/usuariocrear/UsuarioCrearScreen.kt` | Nuevo callback `onUsuarioCreado` separado de `onNavigateBack` |
| `ui/usuarioactualizar/UsuarioActualizarScreen.kt` | Nuevo callback `onUsuarioActualizado` separado de `onNavigateBack` |
| `ui/navigation/NavGraph.kt` | Los nuevos callbacks setean `reloadUsuarios=true` en `savedStateHandle` antes de `popBackStack()` |
| `ui/usuario/UsuarioScreen.kt` | Recarga con `loadPage(uiState.currentPage)` en vez de `loadPage(1)` |

## Comportamiento final

- **Crear usuario** → recarga la lista en la misma página donde estaba
- **Actualizar usuario** → recarga la lista en la misma página donde estaba
- **Eliminar usuario** → recarga la lista en la misma página donde estaba
- **Cancelar** desde cualquier pantalla → vuelve sin recarga innecesaria

## Resultado

Los tres flujos de navegación (crear, actualizar, eliminar) ahora son homogéneos y el usuario permanece en la misma página del listado paginado tras cualquier operación exitosa.

---

# Resumen de cambios - Feedback visual con Snackbar

## Problema original

Al agregar, actualizar o eliminar un registro satisfactoriamente, no se mostraba ningún mensaje de confirmación al usuario. Los errores de API se mostraban localmente en cada pantalla mediante Snackbar, sin regresar a la lista principal.

## Solución aplicada

Se implementó un mecanismo de feedback unificado mediante Snackbar coloreado en la pantalla principal (`UsuarioScreen`), utilizando `savedStateHandle` para comunicar el resultado de la operación desde las pantallas hijas.

## Archivos modificados (8)

| Archivo | Cambio |
|---|---|
| `ui/usuario/UsuarioScreen.kt` | Nuevo parámetro `operationResult`, `SnackbarHost` con colores personalizados (verde `#4CAF50` / rojo `#F44336`), `LaunchedEffect` con `SnackbarDuration.Indefinite` + `delay(10000ms)` + `job.cancel()` |
| `ui/navigation/NavGraph.kt` | Callbacks de éxito y error guardan `operationResult` ("success"/"error") en `savedStateHandle`; nuevo callback `onError` en rutas hijas |
| `ui/usuariocrear/UsuarioCrearViewModel.kt` | Nuevo evento `Error`, emitido en vez de Snackbar local |
| `ui/usuariocrear/UsuarioCrearScreen.kt` | Parámetro `onError`, maneja `Error` event, elimina Snackbar local |
| `ui/usuarioactualizar/UsuarioActualizarViewModel.kt` | Nuevo evento `Error`, emitido en vez de Snackbar local |
| `ui/usuarioactualizar/UsuarioActualizarScreen.kt` | Parámetro `onError`, maneja `Error` event, elimina Snackbar local |
| `ui/usuarioeliminar/UsuarioEliminarViewModel.kt` | Nuevo evento `Error`, emitido en vez de Snackbar local |
| `ui/usuarioeliminar/UsuarioEliminarScreen.kt` | Parámetro `onError`, elimina Snackbar local |

## Comportamiento final

| Acción | Comportamiento |
|---|---|
| Crear/Actualizar/Eliminar **exitoso** | Snackbar **verde** "Operación exitosa!!" (10s) |
| Error de **API** | Navega a pantalla principal → Snackbar **rojo** "Error al procesar la operación!!" (10s) |
| Error de **validación** | Se queda en el formulario mostrando errores de campo (sin cambios) |
| **Cancelar** / Volver | Sin mensaje |

## Detalles técnicos

- Se usó `Snackbar` de Material3 (más idiomático para Compose) en vez de Android `Toast`
- La duración exacta de 10 segundos se logró con `SnackbarDuration.Indefinite` + `delay(10000)` + `job.cancel()` en lugar de las duraciones predefinidas `Short` (~4s) o `Long` (~10s)
- El color del Snackbar se controla mediante el estado `snackbarIsSuccess` en el `SnackbarHost`

# Resumen de cambios - Ocultar ID en pantallas de actualizar y eliminar

## Problema original

Las pantallas de actualizar (`UsuarioActualizarScreen`) y eliminar (`UsuarioEliminarScreen`) mostraban un campo `OutlinedTextField` deshabilitado con el ID del usuario, información interna que no debería ser visible.

## Solución aplicada

Se eliminó el campo visual del ID de ambas pantallas, manteniendo su uso interno en los ViewModels para las llamadas API.

## Archivos modificados (2)

| Archivo | Cambio |
|---|---|
| `ui/usuarioactualizar/UsuarioActualizarScreen.kt` | Eliminado `OutlinedTextField` del ID (incluyendo su `supportingText` de `rowVersionError`) y su `Spacer` |
| `ui/usuarioeliminar/UsuarioEliminarScreen.kt` | Eliminado `OutlinedTextField` del ID y su `Spacer` |

## Comportamiento final

- **Actualizar usuario**: Ya no muestra el ID; solo campos editables (nombre, contraseña, correo)
- **Eliminar usuario**: Ya no muestra el ID; solo muestra nombre y correo (solo lectura) con confirmación
- La lógica de validación y API sigue usando el ID internamente sin cambios
