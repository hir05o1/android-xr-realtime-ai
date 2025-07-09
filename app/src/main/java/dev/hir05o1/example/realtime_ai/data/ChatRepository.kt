package dev.hir05o1.example.realtime_ai.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val ws: WebSocketManager
) {
    fun observeMessages(): Flow<String> = ws.events
    fun sendMessage(message: String) = ws.send(message)
    fun initConnection() = ws.connect("wss://echo.websocket.org")
    fun closeConnection(code: Int = 1000, reason: String? = "Chat ended normally") {
        ws.close(code, reason)
    }
}

