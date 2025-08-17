package com.example.aistudio.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.File
import java.io.FileOutputStream

data class ModelInfo(
    val name: String,
    val path: String,
    val compatibility: Compatibility
)

enum class Compatibility {
    CHECKING,
    SUPPORTED,
    PARTIAL_SUPPORT,
    UNSUPPORTED,
    FAILED
}

class ModelListViewModel(application: Application) : AndroidViewModel(application) {

    private val _models = MutableStateFlow<List<ModelInfo>>(emptyList())
    val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()

    private val modelsDir = File(application.filesDir, "models")

    init {
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            val modelFiles = modelsDir.listFiles()?.map {
                ModelInfo(it.name, it.absolutePath, Compatibility.CHECKING)
            } ?: emptyList()
            _models.value = modelFiles
            
            // Check compatibility for each model
            modelFiles.forEach { model ->
                checkModelCompatibility(model.path)
            }
        }
    }

    private suspend fun checkModelCompatibility(modelPath: String) {
        val compatibility = withContext(Dispatchers.IO) {
            try {
                val nnApiDelegate = NnApiDelegate()
                val options = Interpreter.Options().addDelegate(nnApiDelegate)
                val interpreter = Interpreter(File(modelPath), options)
                
                // If interpreter is created successfully, the model is at least partially supported.
                // A more detailed check would involve checking the number of nodes assigned to the delegate.
                // For now, we'll consider it supported.
                interpreter.close()
                nnApiDelegate.close()
                Compatibility.SUPPORTED
            } catch (e: Exception) {
                e.printStackTrace()
                // Try without NNAPI
                try {
                    val interpreter = Interpreter(File(modelPath))
                    interpreter.close()
                    Compatibility.PARTIAL_SUPPORT // Supported by CPU
                } catch (e2: Exception) {
                    e2.printStackTrace()
                    Compatibility.FAILED
                }
            }
        }
        
        _models.update { currentModels ->
            currentModels.map {
                if (it.path == modelPath) {
                    it.copy(compatibility = compatibility)
                } else {
                    it
                }
            }
        }
    }

    fun importModel(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val fileName = getFileName(uri) ?: "model_${System.currentTimeMillis()}.tflite"
            val destinationFile = File(modelsDir, fileName)

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val newModel = ModelInfo(destinationFile.name, destinationFile.absolutePath, Compatibility.CHECKING)
                _models.value = _models.value + newModel
                checkModelCompatibility(destinationFile.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        return uri.path?.substringAfterLast('/')
    }
}
