package dev.hir05o1.example.realtime_ai.data.gpt

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

const val TAG = "GptWebSocketManager"

@Singleton
class GptWebSocketManager @Inject constructor(
    private val client: OkHttpClient
) : WebSocketListener() {

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val events: SharedFlow<String> = _events

    private lateinit var webSocket: WebSocket
    private var url = "wss://api.openai.com/v1/realtime?model=gpt-4o-realtime-preview-2025-06-03"

    fun connect() {
        if (::webSocket.isInitialized) return
        val protocols = listOf(
            "realtime",
            "openai-insecure-api-key.${""}",
            "openai-beta.realtime-v1"
        )
        try {
            val req = Request.Builder().url(url)
                .addHeader("Sec-WebSocket-Protocol", protocols.joinToString(", ")).build()
            webSocket = client.newWebSocket(req, this)
            Log.i(TAG, "WebSocket connecting... $url")
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket connection failed: ${e.message}", e)
        }
    }

    fun send(role: String = "user", text: String) {
        // 1. メッセージを会話に追加
        val createItemJson = buildJsonObject {
            put("type", "conversation.item.create")
            putJsonObject("item") {
                put("type", "message")
                put("role", role)
                putJsonArray("content") {
                    addJsonObject {
                        put("type", "input_text")
                        put("text", text)
                    }
                }
            }
        }.toString()
        webSocket.send(createItemJson)
        Log.d(TAG, "→ Create item: $createItemJson")

        // 2. レスポンス生成をリクエスト
        val responseCreateJson = buildJsonObject {
            put("type", "response.create")
            putJsonObject("response") {
                putJsonArray("modalities") {      // ← 必ず配列で
                    add("text")                   // あるいは "audio"
                }
                put("instructions", "Please respond to the user's message.")
            }
        }.toString()
        webSocket.send(responseCreateJson)
        Log.d(TAG, "→ Response create: $responseCreateJson")
    }

    /* ------------ listener ------------ */

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.i(TAG, "onOpen • headers=${response.headers}")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.v(TAG, "← $text")
        _events.tryEmit(text)          // そのまま流す／パースは Repository 側で
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.v(TAG, "← (binary) size=${bytes.size}")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "onFailure", t)
        reconnect()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "onClosing $code $reason")
        webSocket.close(code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "onClosed  $code $reason")
    }

    /* ------------ reconnect helper ------------ */

    private fun reconnect(delay: Long = 3000) {
        Log.w(TAG, "reconnect in $delay ms")
        CoroutineScope(Dispatchers.IO).launch {
            delay(delay)
            if (::webSocket.isInitialized) webSocket.cancel()
            connect()
        }
    }

    fun close() = if (::webSocket.isInitialized) webSocket.close(1000, null) else Unit

}
