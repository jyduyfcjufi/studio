package com.example.aistudio.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 全局应用状态的数据类
 * @param selectedModel 用户当前选择用于聊天的模型
 */
data class AppState(
    val selectedModel: ModelInfo? = null
)

/**
 * 在多个界面之间共享状态的 ViewModel.
 * 它的生命周期将与 Activity 绑定.
 */
class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppState())
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()

    /**
     * 当用户在模型列表中选择一个模型时调用.
     * @param model 被选中的模型信息.
     */
    fun selectModel(model: ModelInfo) {
        // 确保模型是可用的 (已配对 Tokenizer)
        if (model.tokenizerPath != null) {
            _uiState.update { it.copy(selectedModel = model) }
        }
    }
}
