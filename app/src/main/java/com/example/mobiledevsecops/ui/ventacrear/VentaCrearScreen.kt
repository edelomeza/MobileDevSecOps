package com.example.mobiledevsecops.ui.ventacrear

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaCrearScreen(
    onNavigateBack: () -> Unit,
    onVentaCreada: () -> Unit,
    onError: () -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: VentaCrearViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VentaCrearEvent.NavigateBack -> onNavigateBack()
                is VentaCrearEvent.VentaCreada -> onVentaCreada()
                is VentaCrearEvent.Error -> onError()
                is VentaCrearEvent.SessionExpired -> onSessionExpired()
                is VentaCrearEvent.ShowSnackbar -> {
                    // Mensaje informativo reservado para futuros escenarios
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Venta") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onCancelarClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancelar"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cliente",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampoAutocompletado(
                value = uiState.clienteSearchText,
                label = "Buscar cliente",
                buscando = uiState.clienteBuscando,
                sugerencias = uiState.clienteResultados.map { it.strNombreCliente },
                error = uiState.clienteError,
                onValueChanged = viewModel::onClienteSearchChanged,
                onLimpiar = viewModel::onClienteLimpiado,
                onSugerenciaClick = { index ->
                    uiState.clienteResultados.getOrNull(index)?.let(viewModel::onClienteSeleccionado)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Usuario",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampoAutocompletado(
                value = uiState.usuarioSearchText,
                label = "Buscar usuario",
                buscando = uiState.usuarioBuscando,
                sugerencias = uiState.usuarioResultados.map { it.strNombre },
                error = uiState.usuarioError,
                onValueChanged = viewModel::onUsuarioSearchChanged,
                onLimpiar = viewModel::onUsuarioLimpiado,
                onSugerenciaClick = { index ->
                    uiState.usuarioResultados.getOrNull(index)?.let(viewModel::onUsuarioSeleccionado)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = viewModel::onCancelarClicked,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancelar")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = viewModel::onGuardarClicked,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Guardar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CampoAutocompletado(
    value: String,
    label: String,
    buscando: Boolean,
    sugerencias: List<String>,
    error: String?,
    onValueChanged: (String) -> Unit,
    onLimpiar: () -> Unit,
    onSugerenciaClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            label = { Text(label) },
            placeholder = { Text("Escribe al menos 2 letras...") },
            isError = error != null,
            supportingText = error?.let {
                { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = onLimpiar) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar selección"
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
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