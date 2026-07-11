package com.example.mobiledevsecops.ui.empleadoactualizar

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadoActualizarScreen(
    id: Int,
    rowVersion: String,
    onNavigateBack: () -> Unit,
    onEmpleadoActualizado: () -> Unit,
    onError: () -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: EmpleadoActualizarViewModel = koinViewModel(
        parameters = { parametersOf(EmpleadoActualizarParams(id, rowVersion)) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var tipoDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EmpleadoActualizarEvent.NavigateBack -> onNavigateBack()
                is EmpleadoActualizarEvent.EmpleadoActualizado -> onEmpleadoActualizado()
                is EmpleadoActualizarEvent.Error -> onError()
                is EmpleadoActualizarEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onDismissError()
        }
    }

    LaunchedEffect(uiState.loadError) {
        uiState.loadError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onDismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Actualizar Empleado") },
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

            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = viewModel::onNombreChanged,
                label = { Text("Nombre *") },
                isError = uiState.nombreError != null,
                supportingText = uiState.nombreError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.aPaterno,
                onValueChange = viewModel::onAPaternoChanged,
                label = { Text("Apellido Paterno") },
                isError = uiState.aPaternoError != null,
                supportingText = uiState.aPaternoError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.aMaterno,
                onValueChange = viewModel::onAMaternoChanged,
                label = { Text("Apellido Materno") },
                isError = uiState.aMaternoError != null,
                supportingText = uiState.aMaternoError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.curp,
                onValueChange = { viewModel.onCurpChanged(it.uppercase()) },
                label = { Text("CURP") },
                isError = uiState.curpError != null,
                supportingText = uiState.curpError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            val selectedTipoLabel = uiState.tiposEmpleado.find { it.id == uiState.idTipoEmpleado }?.strValor ?: "Seleccionar..."

            TextButton(
                onClick = { tipoDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tipo Empleado: $selectedTipoLabel")
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = tipoDropdownExpanded,
                onDismissRequest = { tipoDropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin tipo") },
                    onClick = {
                        tipoDropdownExpanded = false
                        viewModel.onTipoEmpleadoChanged(null)
                    }
                )
                uiState.tiposEmpleado.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text("${tipo.strValor} - ${tipo.strDescripcion}") },
                        onClick = {
                            tipoDropdownExpanded = false
                            viewModel.onTipoEmpleadoChanged(tipo.id)
                        }
                    )
                }
            }

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
                    onClick = viewModel::onActualizarClicked,
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
                    Text("Actualizar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
