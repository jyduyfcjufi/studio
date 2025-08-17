package com.example.aistudio.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 代表设置项的数据类
 */
data class InferenceSettings(
    val temperature: Float = 0.8f,
    val topK: Int = 40,
    val maxNewTokens: Int = 256
)

/**
 * 管理推理设置的 ViewModel.
 * 使用 SharedPreferences 进行数据持久化.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("inference_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(InferenceSettings())
    val settings: StateFlow<InferenceSettings> = _settings.asStateFlow()

    companion object {
        const val KEY_TEMPERATURE = "temperature"
        const val KEY_TOP_K = "top_k"
        const val KEY_MAX_NEW_TOKENS = "max_new_tokens"
    }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val temperature = prefs.getFloat(KEY_TEMPERATURE, 0.8f)
        val topK = prefs.getInt(KEY_TOP_K, 40)
        val maxNewTokens = prefs.getInt(KEY_MAX_NEW_TOKENS, 256)
        _settings.value = InferenceSettings(temperature, topK, maxNewTokens)
    }

    fun setTemperature(temperature: Float) {
        viewModelScope.launch {
            prefs.edit().putFloat(KEY_TEMPERATURE, temperature).apply()
            _settings.update { it.copy(temperature = temperature) }
        }
    }

    fun setTopK(topK: Int) {
        viewModelScope.launch {
            prefs.edit().putInt(KEY_TOP_K, topK).apply()
            _settings.update { it.copy(topK = topK) }
        }
    }

    fun setMaxNewTokens(maxTokens: Int) {
        viewModelScope.launch {
            prefs.edit().putInt(KEY_MAX_NEW_TOKENS, maxTokens).apply()
            _settings.update { it.copy(maxNewTokens = maxTokens) }
        }
    }
}
