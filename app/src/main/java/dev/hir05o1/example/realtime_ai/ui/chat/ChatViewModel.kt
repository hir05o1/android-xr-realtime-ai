package dev.hir05o1.example.realtime_ai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hir05o1.example.realtime_ai.data.gpt.GptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Chat + Gemini Live API を 1 つの ViewModel で扱う実装例
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val openAiRepo: GptRepository   // ★ 追加
) : ViewModel() {

    data class ChatMessage(
        val text: String,
        val isUser: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _uiState = MutableStateFlow<List<ChatMessage>>(emptyList())
    val uiState: StateFlow<List<ChatMessage>> = _uiState.asStateFlow()

    init {
        openAiRepo.startSession()               // ★ OpenAI 接続

        viewModelScope.launch {
            // OpenAI の応答を受け取る
            openAiRepo.observeAi().collect { text ->
                if (text.isNotBlank()) {
                    _uiState.update {
                        it + ChatMessage(text = text, isUser = false)
                    }
                }
            }
        }
    }

    fun send(text: String) {
        // ユーザーのメッセージをUIに表示
        _uiState.update {
            it + ChatMessage(text = text, isUser = true)
        }
        // OpenAIに送信
        openAiRepo.sendToAi(text)
    }

    override fun onCleared() {
        super.onCleared()
        openAiRepo.close()
    }
}
