package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- OpenRouter Data Models ---

@JsonClass(generateAdapter = true)
data class OpenRouterChatRequest(
    val model: String,
    val messages: List<OpenRouterChatMessage>
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatResponse(
    val id: String? = null,
    val choices: List<OpenRouterChoice>? = null,
    val error: OpenRouterErrorDetail? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoice(
    val message: OpenRouterMessageContent? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessageContent(
    val role: String? = null,
    val content: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterErrorDetail(
    val message: String? = null,
    val code: Any? = null
)

// --- Gemini REST API Data Models ---

@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiErrorDetail? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorDetail(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
