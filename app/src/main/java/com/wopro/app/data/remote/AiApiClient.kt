package com.wopro.app.data.remote

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Satu pesan percakapan (OpenAI-compatible). */
data class ChatMessageDto(val role: String = "", val content: String = "")

private data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val max_tokens: Int = 2048
)

private data class ChatCompletionResponse(
    val choices: List<ChatChoice> = emptyList()
)

private data class ChatChoice(val message: ChatMessageDto? = null)

/**
 * Klien AI Assistant — memanggil apinex.bond (OpenAI-compatible /v1/chat/completions).
 * Model: deepseek free flash (free/deepseek-v4-flash-0731).
 */
object AiApiClient {

    private const val BASE_URL = "https://api.apinex.bond/v1/chat/completions"
    private const val API_KEY = "sk-apx14b9b014881d6871115e97ec94eeeb6cd721362d3bc4e6a"
    private const val MODEL = "free/deepseek-v4-flash-0731"

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS) // LLM bisa butuh waktu lebih lama
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Kirim riwayat percakapan, kembalikan teks balasan assistant. */
    suspend fun chat(messages: List<ChatMessageDto>): String = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(
            ChatCompletionRequest(model = MODEL, messages = messages)
        )
        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IOException("HTTP ${resp.code}: ${body.take(200)}")
            }
            val parsed = gson.fromJson(body, ChatCompletionResponse::class.java)
            parsed.choices.firstOrNull()?.message?.content?.trim()
                ?: throw IOException("Respons kosong dari AI")
        }
    }
}
