package com.example.mobiledevsecops.ui.venta

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import com.example.mobiledevsecops.domain.model.Venta
import com.example.mobiledevsecops.util.Fechas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaScreen(
    reloadSignal: Boolean = false,
    operationResult: String = "",
    onNavigateBack: () -> Unit,
    onSessionExpired: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetalle: (Int, String, String, String, String, Int, Int, Int, String) -> Unit,
    viewModel: VentaViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarIsSuccess by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VentaEvent.NavigateBack -> onNavigateBack()
                is VentaEvent.SessionExpired -> onSessionExpired()
                is VentaEvent.ShowSnackbar -> {
                    snackbarIsSuccess = false
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    LaunchedEffect(reloadSignal) {
        if (reloadSignal) {
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
                title = { Text("Ventas") },
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
                    contentDescription = "Agregar venta"
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
            FiltrosVenta(
                searchText = uiState.searchText,
                fechaInicio = uiState.fechaInicio,
                fechaFin = uiState.fechaFin,
                isSearching = uiState.isSearching,
                isSearchActive = uiState.isSearchActive,
                onSearchTextChanged = viewModel::onSearchTextChanged,
                onFechaInicioChanged = viewModel::onFechaInicioChanged,
                onFechaFinChanged = viewModel::onFechaFinChanged,
                onBuscarClicked = viewModel::onBuscarClicked,
                onClearSearch = viewModel::onClearSearch
            )

            when {
                uiState.isLoading -> LoadingContent()
                uiState.error != null && uiState.ventas.isEmpty() -> ErrorContent(
                    message = uiState.error ?: "",
                    onRetry = { viewModel.loadPage(1) }
                )
                uiState.ventas.isEmpty() -> EmptyContent()
                else -> VentaListContent(
                    uiState = uiState,
                    onPreviousPage = viewModel::goToPreviousPage,
                    onNextPage = viewModel::goToNextPage,
                    onNavigateToDetalle = onNavigateToDetalle
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosVenta(
    searchText: String,
    fechaInicio: String?,
    fechaFin: String?,
    isSearching: Boolean,
    isSearchActive: Boolean,
    onSearchTextChanged: (String) -> Unit,
    onFechaInicioChanged: (Long?) -> Unit,
    onFechaFinChanged: (Long?) -> Unit,
    onBuscarClicked: () -> Unit,
    onClearSearch: () -> Unit
) {
    var fechaInicioMillis by remember { mutableStateOf<Long?>(null) }
    var fechaFinMillis by remember { mutableStateOf<Long?>(null) }
    var showFechaInicioPicker by remember { mutableStateOf(false) }
    var showFechaFinPicker by remember { mutableStateOf(false) }

    LaunchedEffect(fechaInicio, fechaFin) {
        if (fechaInicio == null) fechaInicioMillis = null
        if (fechaFin == null) fechaFinMillis = null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar por clave o cliente") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { onSearchTextChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar texto"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuscarClicked,
                enabled = !isSearching,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp).width(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Buscar")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { showFechaInicioPicker = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.height(18.dp).width(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = fechaInicioMillis?.let { "Desde: ${Fechas.formatearFechaDesdeMillis(it)}" } ?: "Desde"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = { showFechaFinPicker = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.height(18.dp).width(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = fechaFinMillis?.let { "Hasta: ${Fechas.formatearFechaDesdeMillis(it)}" } ?: "Hasta"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (fechaInicio != null || fechaFin != null) {
                TextButton(onClick = {
                    fechaInicioMillis = null
                    fechaFinMillis = null
                    onFechaInicioChanged(null)
                    onFechaFinChanged(null)
                }) {
                    Text("Limpiar fechas")
                }
            }
        }

        if (showFechaInicioPicker) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = fechaInicioMillis)
            DatePickerDialog(
                onDismissRequest = { showFechaInicioPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            fechaInicioMillis = millis
                            onFechaInicioChanged(millis)
                        }
                        showFechaInicioPicker = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFechaInicioPicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }

        if (showFechaFinPicker) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = fechaFinMillis)
            DatePickerDialog(
                onDismissRequest = { showFechaFinPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            fechaFinMillis = millis
                            onFechaFinChanged(millis)
                        }
                        showFechaFinPicker = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFechaFinPicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }

        if (isSearchActive) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mostrando resultados de búsqueda",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedButton(onClick = onClearSearch) {
                    Text("Mostrar todos")
                }
            }
        }

        if (isSearching) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.height(16.dp).width(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buscando...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                text = "Cargando ventas...",
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
            text = "No se encontraron ventas",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VentaListContent(
    uiState: VentaUiState,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onNavigateToDetalle: (Int, String, String, String, String, Int, Int, Int, String) -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Total: ${uiState.totalCount} ventas",
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
            TableHeader()
            LazyColumn {
                itemsIndexed(uiState.ventas) { index, venta ->
                    VentaRow(
                        venta = venta,
                        isEven = index % 2 == 0,
                        onNavigateToDetalle = onNavigateToDetalle
                    )
                    if (index < uiState.ventas.lastIndex) {
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
private fun TableHeader() {
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
            text = "Fecha y Hora",
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "Clave Venta",
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "Nombre Cliente",
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "Estado",
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun VentaRow(
    venta: Venta,
    isEven: Boolean,
    onNavigateToDetalle: (Int, String, String, String, String, Int, Int, Int, String) -> Unit
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
        Text(
            text = Fechas.formatearFechaHora(venta.dteFechaHoraCompra),
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = venta.strClaveVenta,
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = venta.strNombreCliente,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = venta.strEstado,
            modifier = Modifier.weight(0.9f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            onClick = {
                onNavigateToDetalle(
                    venta.id,
                    venta.strClaveVenta,
                    venta.dteFechaHoraCompra ?: "",
                    venta.strNombreCliente,
                    venta.strEstado,
                    venta.idCliCliente,
                    venta.idSegUsuario,
                    venta.idVenCatEstado,
                    venta.rowVersion
                )
            }
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}