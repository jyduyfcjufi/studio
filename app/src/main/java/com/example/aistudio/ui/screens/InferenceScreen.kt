package com.example.aistudio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aistudio.viewmodels.InferenceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InferenceScreen(
    modelPath: String,
    viewModel: InferenceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(modelPath) {
        viewModel.loadModel(modelPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Run Inference") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!uiState.isModelLoaded) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load model.", color = MaterialTheme.colorScheme.error)
                }
            } else {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Input Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.runInference(inputText) },
                    enabled = !uiState.isInferring,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (uiState.isInferring) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Run")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Output:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    Text(text = uiState.outputText)
                }
            }
        }
    }
}
