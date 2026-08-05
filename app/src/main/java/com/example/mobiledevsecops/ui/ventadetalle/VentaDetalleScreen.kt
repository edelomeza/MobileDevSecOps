package com.example.mobiledevsecops.ui.ventadetalle

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaDetalleScreen(
    onNavigateBack: () -> Unit,
    onSessionExpired: () -> Unit,
    ventaId: Int,
    strClaveVenta: String,
    dteFechaHoraCompra: String,
    strNombreCliente: String,
    strEstado: String,
    idCliCliente: Int,
    idSegUsuario: Int,
    idVenCatEstado: Int,
    rowVersion: String,
    viewModel: VentaDetalleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ventaId) {
        viewModel.inicializar(
            ventaId = ventaId,
            strClaveVenta = strClaveVenta,
            dteFechaHoraCompra = dteFechaHoraCompra,
            strNombreCliente = strNombreCliente,
            strEstado = strEstado,
            idCliCliente = idCliCliente,
            idSegUsuario = idSegUsuario,
            idVenCatEstado = idVenCatEstado,
            rowVersion = rowVersion
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VentaDetalleEvent.NavigateBack -> onNavigateBack()
                is VentaDetalleEvent.SessionExpired -> onSessionExpired()
                is VentaDetalleEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelarEliminar,
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de eliminar este producto de la venta?") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onConfirmarEliminar,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelarEliminar) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Venta") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onCerrarClicked) {
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                DatosVentaCard(
                    dteFechaHoraCompra = dteFechaHoraCompra,
                    strClaveVenta = strClaveVenta,
                    strNombreCliente = strNombreCliente
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                EstadoVentaSection(
                    estadosVenta = uiState.estadosVenta,
                    estadoSeleccionadoId = uiState.estadoSeleccionadoId,
                    isGuardando = uiState.isGuardandoEstado,
                    onEstadoSeleccionado = viewModel::onEstadoSeleccionado
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Agregar Producto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                ProductoAutocompleteSection(
                    searchText = uiState.productoSearchText,
                    buscando = uiState.productoBuscando,
                    sugerencias = uiState.productoResultados.map { it.strTextoAutocomplete },
                    onValueChanged = viewModel::onProductoSearchChanged,
                    onLimpiar = viewModel::onProductoLimpiado,
                    onSugerenciaClick = { index ->
                        uiState.productoResultados.getOrNull(index)?.let(viewModel::onProductoSeleccionado)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.intPiezaVenta,
                        onValueChange = viewModel::onPiezasChanged,
                        label = { Text("Piezas") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = viewModel::onAgregarProducto,
                        enabled = !uiState.isAgregandoProducto && uiState.productoSeleccionadoId > 0
                    ) {
                        if (uiState.isAgregandoProducto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text("Agregar Producto")
                    }
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                uiState.successMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (uiState.detalles.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Productos agregados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetalleTableHeader()
                }

                itemsIndexed(uiState.detalles) { index, detalle ->
                    DetalleRow(
                        detalle = detalle,
                        isEven = index % 2 == 0,
                        onEliminarClick = viewModel::onEliminarClicked,
                        isEliminando = uiState.isEliminando
                    )
                    if (index < uiState.detalles.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                    }
                }

                item {
                    val totalDetalles = uiState.detalles.sumOf { it.decTotalVenta }
                    DetalleTableFooter(total = totalDetalles)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = viewModel::onCerrarClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar y regresar")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DatosVentaCard(
    dteFechaHoraCompra: String,
    strClaveVenta: String,
    strNombreCliente: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DatosVentaRow(label = "Fecha y hora de compra", value = dteFechaHoraCompra)
            Spacer(modifier = Modifier.height(8.dp))
            DatosVentaRow(label = "Clave de venta", value = strClaveVenta)
            Spacer(modifier = Modifier.height(8.dp))
            DatosVentaRow(label = "Cliente", value = strNombreCliente)
        }
    }
}

@Composable
private fun DatosVentaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EstadoVentaSection(
    estadosVenta: List<com.example.mobiledevsecops.domain.model.EstadoVenta>,
    estadoSeleccionadoId: Int,
    isGuardando: Boolean,
    onEstadoSeleccionado: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedEstado = estadosVenta.firstOrNull { it.id == estadoSeleccionadoId }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Estado de venta",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedEstado?.strValor ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Estado") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                estadosVenta.forEach { estado ->
                    DropdownMenuItem(
                        text = { Text(estado.strValor) },
                        onClick = {
                            onEstadoSeleccionado(estado.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (isGuardando) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardando...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProductoAutocompleteSection(
    searchText: String,
    buscando: Boolean,
    sugerencias: List<String>,
    onValueChanged: (String) -> Unit,
    onLimpiar: () -> Unit,
    onSugerenciaClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onValueChanged,
            label = { Text("Buscar producto") },
            placeholder = { Text("Escribe al menos 2 letras...") },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = onLimpiar) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar"
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        when {
            buscando -> {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buscando...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            sugerencias.isNotEmpty() -> {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column {
                        sugerencias.forEachIndexed { index, sugerencia ->
                            TextButton(
                                onClick = { onSugerenciaClick(index) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = sugerencia,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleTableHeader() {
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
            text = "Producto",
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "Número",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "Total",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun DetalleRow(
    detalle: com.example.mobiledevsecops.domain.model.VentaDetalle,
    isEven: Boolean,
    onEliminarClick: (com.example.mobiledevsecops.domain.model.VentaDetalle) -> Unit,
    isEliminando: Boolean
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
            text = detalle.strNombreProducto,
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = detalle.intPiezaVenta.toString(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "$${String.format(java.util.Locale.US, "%.2f", detalle.decTotalVenta)}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(
            onClick = { onEliminarClick(detalle) },
            enabled = !isEliminando,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun DetalleTableFooter(total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total: ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$${String.format(java.util.Locale.US, "%.2f", total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
