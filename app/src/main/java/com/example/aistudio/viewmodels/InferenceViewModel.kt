package com.example.aistudio.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aistudio.inference.Accelerator
import com.example.aistudio.inference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

data class InferenceUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val selectedModel: ModelInfo? = null,
    val performanceStats: String = ""
)

class InferenceViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(InferenceUiState())
    val uiState: StateFlow<InferenceUiState> = _uiState.asStateFlow()

    private var llmInference: LlmInference? = null
    private var generationJob: Job? = null

    fun onModelSelected(model: ModelInfo?) {
        _uiState.update { it.copy(selectedModel = model, messages = emptyList()) }
        if (model == null) {
            llmInference?.close()
            llmInference = null
        }
    }

    fun sendMessage(prompt: String, accelerator: Accelerator, settings: InferenceSettings) {
        val currentModel = _uiState.value.selectedModel
        if (currentModel?.tokenizerPath == null) {
            return
        }

        generationJob?.cancel()

        _uiState.update { currentState ->
            val newMessages = currentState.messages.toMutableList().apply {
                add(ChatMessage(prompt, true))
                add(ChatMessage("", false))
            }
            currentState.copy(messages = newMessages)
        }

        generationJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                llmInference?.close()
                llmInference = LlmInference(
                    context = getApplication(),
                    modelPath = currentModel.path,
                    tokenizerPath = currentModel.tokenizerPath,
                    accelerator = accelerator,
                    settings = settings
                )

                val responseFlow = llmInference!!.generate(prompt)
                
                responseFlow
                    .onStart { _uiState.update { it.copy(isGenerating = true) } }
                    .onCompletion { _uiState.update { it.copy(isGenerating = false) } }
                    .catch { e ->
                        e.printStackTrace()
                        updateLastMessage("生成失败: ${e.message}")
                    }
                    .collect { token ->
                        updateLastMessage(token, append = true)
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                updateLastMessage("错误: ${e.message}")
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        _uiState.update { it.copy(isGenerating = false) }
    }

    private fun updateLastMessage(text: String, append: Boolean = false) {
        _uiState.update { currentState ->
            val lastMessageIndex = currentState.messages.lastIndex
            if (lastMessageIndex != -1) {
                val updatedMessages = currentState.messages.toMutableList()
                val lastMessage = updatedMessages[lastMessageIndex]
                val newText = if (append) lastMessage.text + text else text
                updatedMessages[lastMessageIndex] = lastMessage.copy(text = newText)
                currentState.copy(messages = updatedMessages)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmInference?.close()
    }
}