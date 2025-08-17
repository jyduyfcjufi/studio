package com.example.aistudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aistudio.inference.Accelerator
import com.example.aistudio.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InferenceScreen(
    appViewModel: AppViewModel,
    settingsViewModel: SettingsViewModel,
    inferenceViewModel: InferenceViewModel = viewModel()
) {
    val appState by appViewModel.uiState.collectAsState()
    val uiState by inferenceViewModel.uiState.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()
    val listState = rememberLazyListState()

    // 当 AppViewModel 中的模型变化时，通知 InferenceViewModel
    LaunchedEffect(appState.selectedModel) {
        inferenceViewModel.onModelSelected(appState.selectedModel)
    }

    // 当聊天记录更新时，自动滚动到底部
    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.text) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.selectedModel?.name ?: "No Model Selected") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.selectedModel == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please select a model from the Models tab.", textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageBubble(message)
                    }
                }

                BottomControlPanel(
                    isGenerating = uiState.isGenerating,
                    onSendMessage = { prompt, accelerator ->
                        inferenceViewModel.sendMessage(prompt, accelerator, settings)
                    },
                    onStopGeneration = {
                        inferenceViewModel.stopGeneration()
                    }
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val bubbleColor = if (message.isFromUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(align = alignment)
    ) {
        Text(
            text = message.text,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .padding(12.dp)
        )
    }
}

@Composable
fun BottomControlPanel(
    isGenerating: Boolean,
    onSendMessage: (String, Accelerator) -> Unit,
    onStopGeneration: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var selectedAccelerator by remember { mutableStateOf(Accelerator.NNAPI) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Accelerator:")
            Accelerator.values().forEach { accelerator ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedAccelerator == accelerator,
                        onClick = { selectedAccelerator = accelerator }
                    )
                    Text(accelerator.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                enabled = !isGenerating
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (isGenerating) {
                IconButton(onClick = onStopGeneration) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop generation")
                }
            } else {
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText, selectedAccelerator)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send message")
                }
            }
        }
    }
}
