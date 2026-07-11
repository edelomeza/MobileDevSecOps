package com.example.mobiledevsecops.ui.empleado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mobiledevsecops.domain.model.Empleado
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadoScreen(
    reloadSignal: Boolean = false,
    operationResult: String = "",
    onNavigateBack: () -> Unit,
    onSessionExpired: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (id: Int, rowVersion: String) -> Unit,
    onNavigateToDelete: (id: Int, rowVersion: String) -> Unit,
    viewModel: EmpleadoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarIsSuccess by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EmpleadoEvent.NavigateBack -> onNavigateBack()
                is EmpleadoEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    LaunchedEffect(reloadSignal) {
        if (reloadSignal) {
            viewModel.loadTiposEmpleado()
            viewModel.loadPage(uiState.currentPage)
        }
    }

    LaunchedEffect(operationResult) {
        when (operationResult) {
            "success" -> {
                snackbarIsSuccess = true
                val job = launch {
                    snackbarHostState.showSnackbar(
                        message = "Operación exitosa!!",
                        duration = SnackbarDuration.Indefinite
                    )
                }
                delay(10000)
                job.cancel()
            }
            "error" -> {
                snackbarIsSuccess = false
                val job = launch {
                    snackbarHostState.showSnackbar(
                        message = "Error al procesar la operación!!",
                        duration = SnackbarDuration.Indefinite
                    )
                }
                delay(10000)
                job.cancel()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (snackbarIsSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                    contentColor = Color.White
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Empleados") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear empleado"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            SearchAndFilterBar(
                searchText = uiState.searchText,
                tiposEmpleado = uiState.tiposEmpleado,
                selectedTipoEmpleadoId = uiState.selectedTipoEmpleadoId,
                hasActiveFilters = uiState.isSearching || uiState.selectedTipoEmpleadoId != null,
                onSearchTextChanged = viewModel::onSearchTextChanged,
                onSearch = viewModel::onSearch,
                onTipoEmpleadoSelected = viewModel::onTipoEmpleadoFilterChanged,
                onClearFilters = viewModel::clearFilters
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> LoadingContent()
                uiState.error != null && uiState.empleados.isEmpty() -> ErrorContent(
                    message = uiState.error ?: "",
                    onRetry = { viewModel.loadPage(1) }
                )
                uiState.empleados.isEmpty() -> EmptyContent()
                else -> EmpleadoListContent(
                    uiState = uiState,
                    onNavigateToEdit = onNavigateToEdit,
                    onNavigateToDelete = onNavigateToDelete,
                    onPreviousPage = viewModel::goToPreviousPage,
                    onNextPage = viewModel::goToNextPage
                )
            }
        }
    }
}

@Composable
private fun SearchAndFilterBar(
    searchText: String,
    tiposEmpleado: List<com.example.mobiledevsecops.domain.model.EmpCatTipoEmpleado>,
    selectedTipoEmpleadoId: Int?,
    hasActiveFilters: Boolean,
    onSearchTextChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onTipoEmpleadoSelected: (Int?) -> Unit,
    onClearFilters: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedLabel = tiposEmpleado.find { it.id == selectedTipoEmpleadoId }?.strValor ?: "Todos"

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChanged,
            label = { Text("Buscar (Nombre/A Paterno/A Materno)") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { onSearchTextChanged(""); onSearch() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        )
        Button(onClick = onSearch) {
            Text("Buscar")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box {
            TextButton(onClick = { dropdownExpanded = true }) {
                Text("Tipo: $selectedLabel")
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todos") },
                    onClick = {
                        dropdownExpanded = false
                        onTipoEmpleadoSelected(null)
                    }
                )
                tiposEmpleado.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.strValor) },
                        onClick = {
                            dropdownExpanded = false
                            onTipoEmpleadoSelected(tipo.id)
                        }
                    )
                }
            }
        }

        if (hasActiveFilters) {
            TextButton(onClick = onClearFilters) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Limpiar filtros")
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando empleados...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No se encontraron empleados",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmpleadoListContent(
    uiState: EmpleadoUiState,
    onNavigateToEdit: (id: Int, rowVersion: String) -> Unit,
    onNavigateToDelete: (id: Int, rowVersion: String) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Text(
        text = "Total: ${uiState.totalCount} empleados",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            EmpleadoTableHeader()
            LazyColumn {
                itemsIndexed(uiState.empleados) { index, empleado ->
                    EmpleadoRow(
                        empleado = empleado,
                        isEven = index % 2 == 0,
                        onEditClick = {
                            onNavigateToEdit(empleado.id, empleado.rowVersion)
                        },
                        onDeleteClick = {
                            onNavigateToDelete(empleado.id, empleado.rowVersion)
                        }
                    )
                    if (index < uiState.empleados.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    PaginationControls(
        currentPage = uiState.currentPage,
        totalPages = uiState.totalPages,
        onPrevious = onPreviousPage,
        onNext = onNextPage
    )
}

@Composable
private fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPrevious,
            enabled = currentPage > 1
        ) {
            Text("Anterior")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Página $currentPage de $totalPages",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onNext,
            enabled = currentPage < totalPages
        ) {
            Text("Siguiente")
        }
    }
}

@Composable
private fun EmpleadoTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Nombre Completo",
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "CURP",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(80.dp))
    }
}

@Composable
private fun EmpleadoRow(
    empleado: Empleado,
    isEven: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isEven) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val nombreCompleto = buildString {
            append(empleado.strNombre)
            if (!empleado.strAPaterno.isNullOrBlank()) append(" ${empleado.strAPaterno}")
            if (!empleado.strAMaterno.isNullOrBlank()) append(" ${empleado.strAMaterno}")
        }
        Text(
            text = nombreCompleto,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = empleado.strCURP?.take(12) ?: "-",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar empleado",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar empleado",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
