package com.example.aistudio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aistudio.viewmodels.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inference Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Temperature Setting
            SettingSlider(
                label = "Temperature",
                value = settings.temperature,
                onValueChange = { viewModel.setTemperature(it) },
                valueRange = 0.1f..1.0f,
                steps = 8, // 9 steps for values like 0.1, 0.2, ... 1.0
                formatValue = { "%.1f".format(it) }
            )

            // Top-K Setting
            SettingSlider(
                label = "Top-K",
                value = settings.topK.toFloat(),
                onValueChange = { viewModel.setTopK(it.roundToInt()) },
                valueRange = 1f..100f,
                steps = 98,
                formatValue = { "%.0f".format(it) }
            )

            // Max New Tokens Setting
            SettingSlider(
                label = "Max New Tokens",
                value = settings.maxNewTokens.toFloat(),
                onValueChange = { viewModel.setMaxNewTokens(it.roundToInt()) },
                valueRange = 64f..1024f,
                steps = ((1024 - 64) / 64) - 1, // Steps of 64
                formatValue = { "%.0f".format(it) }
            )
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    formatValue: (Float) -> String
) {
    var sliderValue by remember(value) { mutableStateOf(value) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(formatValue(sliderValue), style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue) },
            valueRange = valueRange,
            steps = steps
        )
    }
}
