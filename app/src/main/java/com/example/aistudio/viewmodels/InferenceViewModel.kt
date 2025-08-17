package com.example.aistudio.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset

class InferenceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InferenceUiState())
    val uiState: StateFlow<InferenceUiState> = _uiState.asStateFlow()

    private var interpreter: Interpreter? = null

    fun loadModel(modelPath: String) {
        viewModelScope.launch {
            _uiState.value = InferenceUiState(isLoading = true)
            val modelLoaded = withContext(Dispatchers.IO) {
                try {
                    val nnApiDelegate = NnApiDelegate()
                    val options = Interpreter.Options().addDelegate(nnApiDelegate)
                    interpreter = Interpreter(File(modelPath), options)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        // Fallback to CPU
                        interpreter = Interpreter(File(modelPath))
                        true
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                        false
                    }
                }
            }
            _uiState.value = InferenceUiState(isModelLoaded = modelLoaded)
        }
    }

    fun runInference(inputText: String) {
        if (interpreter == null) {
            _uiState.value = _uiState.value.copy(outputText = "Error: Model not loaded.")
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = _uiState.value.copy(isInferring = true)
            try {
                // This is a placeholder for input/output processing.
                // Real models require specific tokenization and tensor shapes.
                // We'll start with a simple byte-in, byte-out assumption.
                val inputBuffer = ByteBuffer.wrap(inputText.toByteArray(Charset.defaultCharset()))
                
                // Assuming the model has 1 output tensor, and it's a byte array.
                val outputBuffer = ByteBuffer.allocate(1024) 
                val outputMap = mapOf(0 to outputBuffer)

                interpreter?.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputMap)

                outputBuffer.flip()
                val outputBytes = ByteArray(outputBuffer.remaining())
                outputBuffer.get(outputBytes)
                
                val outputText = String(outputBytes, Charset.defaultCharset()).trim()

                _uiState.value = _uiState.value.copy(isInferring = false, outputText = outputText)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isInferring = false, outputText = "Inference failed: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        interpreter?.close()
    }
}

data class InferenceUiState(
    val isLoading: Boolean = false,
    val isModelLoaded: Boolean = false,
    val isInferring: Boolean = false,
    val inputText: String = "",
    val outputText: String = ""
)
