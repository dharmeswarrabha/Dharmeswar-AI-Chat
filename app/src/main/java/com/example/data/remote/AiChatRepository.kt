package com.example.data.remote

import com.example.data.local.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AiResponseResult {
    data class Success(val text: String) : AiResponseResult()
    data class Error(val message: String, val isApiKeyMissing: Boolean = false) : AiResponseResult()
}

class AiChatRepository(
    private val openRouterApi: OpenRouterApi = NetworkClient.openRouterApi,
    private val geminiApi: GeminiDirectApi = NetworkClient.geminiApi
) {
    suspend fun sendMessage(
        messages: List<ChatMessage>,
        openRouterKey: String,
        geminiKey: String,
        modelName: String,
        systemPrompt: String
    ): AiResponseResult = withContext(Dispatchers.IO) {
        val trimmedOpenRouterKey = openRouterKey.trim()
        val trimmedGeminiKey = geminiKey.trim()

        // 1. Try OpenRouter if key is present
        if (trimmedOpenRouterKey.isNotBlank()) {
            return@withContext sendViaOpenRouter(
                messages = messages,
                apiKey = trimmedOpenRouterKey,
                modelName = modelName,
                systemPrompt = systemPrompt
            )
        }

        // 2. Try Gemini API directly if Gemini key is present
        if (trimmedGeminiKey.isNotBlank() && !trimmedGeminiKey.contains("MY_GEMINI_API_KEY")) {
            return@withContext sendViaGemini(
                messages = messages,
                apiKey = trimmedGeminiKey,
                modelName = modelName,
                systemPrompt = systemPrompt
            )
        }

        // 3. No valid keys found
        AiResponseResult.Error(
            message = "Please configure your OpenRouter API Key or Gemini API Key in Settings to start chatting.",
            isApiKeyMissing = true
        )
    }

    private suspend fun sendViaOpenRouter(
        messages: List<ChatMessage>,
        apiKey: String,
        modelName: String,
        systemPrompt: String
    ): AiResponseResult {
        return try {
            val apiMessages = mutableListOf<OpenRouterChatMessage>()
            if (systemPrompt.isNotBlank()) {
                apiMessages.add(OpenRouterChatMessage(role = "system", content = systemPrompt))
            }
            messages.forEach { msg ->
                val role = if (msg.role == "user") "user" else "assistant"
                apiMessages.add(OpenRouterChatMessage(role = role, content = msg.content))
            }

            val request = OpenRouterChatRequest(
                model = modelName.ifBlank { "google/gemini-2.5-pro" },
                messages = apiMessages
            )

            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer $apiKey"
            val response = openRouterApi.createChatCompletion(
                authorization = authHeader,
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                val content = body?.choices?.firstOrNull()?.message?.content
                if (!content.isNullOrBlank()) {
                    AiResponseResult.Success(content)
                } else if (body?.error?.message != null) {
                    AiResponseResult.Error("OpenRouter Error: ${body.error.message}")
                } else {
                    AiResponseResult.Error("Received empty response from AI model.")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                AiResponseResult.Error("OpenRouter request failed (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            AiResponseResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private suspend fun sendViaGemini(
        messages: List<ChatMessage>,
        apiKey: String,
        modelName: String,
        systemPrompt: String
    ): AiResponseResult {
        return try {
            // Strip any prefix like "google/" for direct Gemini endpoint, e.g. "google/gemini-2.5-pro" -> "gemini-2.5-pro"
            val resolvedModel = modelName
                .substringAfter("google/")
                .substringAfter("models/")
                .ifBlank { "gemini-2.5-flash" }

            val geminiContents = messages.map { msg ->
                GeminiContent(
                    role = if (msg.role == "user") "user" else "model",
                    parts = listOf(GeminiPart(text = msg.content))
                )
            }

            val systemInstruction = if (systemPrompt.isNotBlank()) {
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = systemPrompt))
                )
            } else null

            val request = GeminiGenerateRequest(
                contents = geminiContents,
                systemInstruction = systemInstruction
            )

            val response = geminiApi.generateContent(
                model = resolvedModel,
                apiKey = apiKey,
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                val content = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!content.isNullOrBlank()) {
                    AiResponseResult.Success(content)
                } else if (body?.error?.message != null) {
                    AiResponseResult.Error("Gemini Error: ${body.error.message}")
                } else {
                    AiResponseResult.Error("Received empty response from Gemini model.")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                AiResponseResult.Error("Gemini request failed (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            AiResponseResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
