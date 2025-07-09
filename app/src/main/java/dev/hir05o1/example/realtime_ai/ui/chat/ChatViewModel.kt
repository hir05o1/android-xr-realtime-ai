package dev.hir05o1.example.realtime_ai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hir05o1.example.realtime_ai.data.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<String>>(emptyList())
    val uiState: StateFlow<List<String>> = _uiState.asStateFlow()

    init {
        repo.initConnection()                          // 1 度だけ接続
        viewModelScope.launch {
            repo.observeMessages().collect { msg ->
                _uiState.update { it + msg }           // 受信を UI へ
            }
        }
    }

    // viewModelが破棄されるとき
    override fun onCleared() {
        super.onCleared()
        // WebSocketの接続を閉じる
        repo.closeConnection()
    }

    fun send(text: String) = repo.sendMessage(text)
}

