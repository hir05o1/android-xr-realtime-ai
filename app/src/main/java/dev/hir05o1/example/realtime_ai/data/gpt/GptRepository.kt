package dev.hir05o1.example.realtime_ai.data.gpt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * dataレイヤーとUIレイヤーの間のGemini関連のデータ操作を行うリポジトリ。
 */
@Singleton
class GptRepository @Inject constructor(
    private val ws: GptWebSocketManager
) {
    fun startSession() = ws.connect()
    fun sendToAi(text: String) = ws.send(text = text)

    /** text 部分だけ抽出して流す */
    fun observeAi(): Flow<String> = ws.events.mapNotNull { raw ->
        runCatching {
            val obj = Json.parseToJsonElement(raw).jsonObject
            val type = obj["type"]?.jsonPrimitive?.content

            when (type) {
                // OpenAI Realtime API のテキストレスポンス（デルタ形式）
                "response.content_part.added" -> {
                    val part = obj["part"]?.jsonObject
                    if (part?.get("type")?.jsonPrimitive?.content == "text") {
                        part["text"]?.jsonPrimitive?.content
                    } else null
                }
                "response.text.delta" -> {
                    obj["delta"]?.jsonPrimitive?.content
                }
                "response.content_part.done" -> {
                    val part = obj["part"]?.jsonObject
                    if (part?.get("type")?.jsonPrimitive?.content == "text") {
                        part["text"]?.jsonPrimitive?.content
                    } else null
                }
                // 音声転写結果
                "conversation.item.input_audio_transcription.completed" -> {
                    obj["transcript"]?.jsonPrimitive?.content
                }
                // エラーメッセージも表示
                "error" -> {
                    val error = obj["error"]?.jsonObject
                    "Error: ${error?.get("message")?.jsonPrimitive?.content ?: "Unknown error"}"
                }
                else -> null
            }
        }.getOrElse {
            // JSONパースエラーの場合、ログに出力して null を返す
            android.util.Log.w("GptRepository", "Failed to parse response: $raw")
            null
        }
    }

    fun close() = ws.close()
}
