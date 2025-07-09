package dev.hir05o1.example.realtime_ai.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val client: OkHttpClient,
) : WebSocketListener() {

    companion object {
        private const val TAG = "WebSocketMgr"
    }

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val events: SharedFlow<String> = _events

    private lateinit var webSocket: WebSocket
    private var url: String = "wss://echo.websocket.org"

    /* ---------- Public API ---------- */

    fun connect(targetUrl: String) {
        url = targetUrl
        if (::webSocket.isInitialized) {
            Log.w(TAG, "connect() called but already connected")
            return
        }
        Log.i(TAG, "Connecting → $url")
        webSocket = client.newWebSocket(Request.Builder().url(url).build(), this)
    }

    fun send(text: String): Boolean {
        val ok = ::webSocket.isInitialized && webSocket.send(text)
        Log.d(TAG, "Send \"$text\" | success=$ok")
        return ok
    }

    fun close(code: Int = 1000, reason: String? = null) {
        Log.i(TAG, "Closing ($code) ${reason.orEmpty()}")
        if (::webSocket.isInitialized) webSocket.close(code, reason)
    }

    /* ---------- WebSocketListener ---------- */

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.i(
            TAG,
            "onOpen • protocol=${response.header("Sec-WebSocket-Protocol")} • headers=${response.headers}"
        )
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.v(TAG, "onMessage (text): $text")
        _events.tryEmit(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.v(TAG, "onMessage (binary): size=${bytes.size}")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "onClosing ($code) $reason")
        webSocket.close(code, reason)         // ACK
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "onClosed  ($code) $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "onFailure • response=$response", t)
        reconnect()
    }

    /* ---------- Reconnect helper ---------- */

    private fun reconnect(delayMillis: Long = 3000) {
        Log.w(TAG, "Reconnecting in $delayMillis ms…")
        CoroutineScope(Dispatchers.IO).launch {
            delay(delayMillis)
            if (::webSocket.isInitialized) {
                webSocket.cancel()
                Log.d(TAG, "Old WebSocket cancelled")
            }
            connect(url)
        }
    }
}
