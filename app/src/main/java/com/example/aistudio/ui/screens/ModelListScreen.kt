package com.example.aistudio.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aistudio.viewmodels.ModelInfo
import com.example.aistudio.viewmodels.ModelListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelListScreen(
    // onModelSelected 现在只通知父组件模型已被选择
    onModelSelected: (ModelInfo) -> Unit,
    viewModel: ModelListViewModel = viewModel()
) {
    val models by viewModel.models.collectAsState()
    var selectedModelPathForTokenizer by remember { mutableStateOf<String?>(null) }

    val importModelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { viewModel.importModel(it) } }
    )

    val associateTokenizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            val modelPath = selectedModelPathForTokenizer
            if (uri != null && modelPath != null) {
                viewModel.associateTokenizer(modelPath, uri)
            }
            selectedModelPathForTokenizer = null
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Models") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                importModelLauncher.launch(arrayOf("*/*"))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Import Model")
            }
        }
    ) { paddingValues ->
        if (models.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = "Click '+' to import your first model")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(models) { model ->
                    ModelListItem(
                        model = model,
                        onClick = {
                            if (model.tokenizerPath != null) {
                                // 如果模型完整，则调用回调
                                onModelSelected(model)
                            } else {
                                // 否则，触发 Tokenizer 选择流程
                                selectedModelPathForTokenizer = model.path
                                associateTokenizerLauncher.launch(arrayOf("*/*"))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelListItem(model: ModelInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = model.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                if (model.tokenizerPath == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tokenizer needed - Click to select",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Text(
                        text = "Ready to chat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
