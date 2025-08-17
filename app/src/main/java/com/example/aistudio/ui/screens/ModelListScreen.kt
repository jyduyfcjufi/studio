package com.example.aistudio.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aistudio.R
import com.example.aistudio.ui.theme.*
import com.example.aistudio.viewmodels.Compatibility
import com.example.aistudio.viewmodels.ModelInfo
import com.example.aistudio.viewmodels.ModelListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelListScreen(
    onNavigateToModel: (String) -> Unit,
    viewModel: ModelListViewModel = viewModel()
) {
    val models by viewModel.models.collectAsState()

    val importModelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.importModel(it)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.app_name)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                importModelLauncher.launch(arrayOf("application/octet-stream"))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Import Model")
            }
        }
    ) { paddingValues ->
        if (models.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Import your first model")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(models) { model ->
                    ModelListItem(
                        model = model,
                        onClick = {
                            onNavigateToModel(model.path)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelListItem(model: ModelInfo, onClick: () -> Unit) {
    val isClickable = model.compatibility == Compatibility.SUPPORTED || model.compatibility == Compatibility.PARTIAL_SUPPORT
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(model.compatibility.toColor())
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = model.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = model.compatibility.name, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun Compatibility.toColor(): Color {
    return when (this) {
        Compatibility.CHECKING -> ColorChecking
        Compatibility.SUPPORTED -> ColorSupported
        Compatibility.PARTIAL_SUPPORT -> ColorPartialSupport
        Compatibility.UNSUPPORTED -> ColorUnsupported
        Compatibility.FAILED -> ColorFailed
    }
}
