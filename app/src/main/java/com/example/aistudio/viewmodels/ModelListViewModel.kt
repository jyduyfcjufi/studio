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
import java.io.File
import java.io.FileOutputStream

/**
 * 更新后的 ModelInfo，包含一个可选的 tokenizerPath
 */
data class ModelInfo(
    val name: String,
    val path: String,
    val tokenizerPath: String? = null, // 可以为空，表示 Tokenizer 尚未配对
    val compatibility: Compatibility = Compatibility.CHECKING
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
    private val tokenizersDir = File(application.filesDir, "tokenizers")

    init {
        modelsDir.mkdirs()
        tokenizersDir.mkdirs()
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            // TODO: 需要实现一个更健壮的机制来持久化 tokenizer 的配对关系
            // 目前的简单实现是：只加载模型目录，tokenizer 的配对关系是临时的
            val modelFiles = modelsDir.listFiles()?.map {
                ModelInfo(name = it.name, path = it.absolutePath)
            } ?: emptyList()
            _models.value = modelFiles
        }
    }

    /**
     * 导入一个新的模型文件.
     */
    fun importModel(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val fileName = getFileName(uri) ?: "model_${System.currentTimeMillis()}.tflite"
            val destinationFile = File(modelsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            val newModel = ModelInfo(destinationFile.name, destinationFile.absolutePath)
            _models.update { it + newModel }
        }
    }

    /**
     * 为一个已存在的模型配对一个 Tokenizer 文件.
     */
    fun associateTokenizer(modelPath: String, tokenizerUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val fileName = getFileName(tokenizerUri) ?: "tokenizer_${System.currentTimeMillis()}.txt"
            val destinationFile = File(tokenizersDir, fileName)

            context.contentResolver.openInputStream(tokenizerUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            // 更新模型列表，为指定的模型添加 tokenizer 路径
            _models.update { currentModels ->
                currentModels.map { model ->
                    if (model.path == modelPath) {
                        model.copy(tokenizerPath = destinationFile.absolutePath)
                    } else {
                        model
                    }
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        // 尝试从 URI 中获取文件名
        return uri.path?.substringAfterLast('/')
    }
}