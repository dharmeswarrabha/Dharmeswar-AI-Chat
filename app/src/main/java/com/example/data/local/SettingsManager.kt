package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val openRouterApiKey: String = "",
    val geminiApiKey: String = "",
    val model: String = "google/gemini-2.5-pro",
    val systemPrompt: String = "You are a helpful, knowledgeable, and friendly AI assistant."
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dharmeswar_ai_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val savedOpenRouterKey = prefs.getString(KEY_OPENROUTER_KEY, "") ?: ""
        val savedGeminiKey = prefs.getString(KEY_GEMINI_KEY, "") ?: ""
        val effectiveGeminiKey = savedGeminiKey.ifBlank { buildConfigKey }
        val savedModel = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        val savedSystemPrompt = prefs.getString(KEY_SYSTEM_PROMPT, DEFAULT_SYSTEM_PROMPT) ?: DEFAULT_SYSTEM_PROMPT

        return AppSettings(
            openRouterApiKey = savedOpenRouterKey,
            geminiApiKey = effectiveGeminiKey,
            model = savedModel,
            systemPrompt = savedSystemPrompt
        )
    }

    fun updateSettings(
        openRouterKey: String,
        geminiKey: String,
        model: String,
        systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    ) {
        prefs.edit()
            .putString(KEY_OPENROUTER_KEY, openRouterKey.trim())
            .putString(KEY_GEMINI_KEY, geminiKey.trim())
            .putString(KEY_MODEL, model.trim().ifEmpty { DEFAULT_MODEL })
            .putString(KEY_SYSTEM_PROMPT, systemPrompt.trim())
            .apply()

        _settings.value = AppSettings(
            openRouterApiKey = openRouterKey.trim(),
            geminiApiKey = geminiKey.trim(),
            model = model.trim().ifEmpty { DEFAULT_MODEL },
            systemPrompt = systemPrompt.trim()
        )
    }

    companion object {
        const val KEY_OPENROUTER_KEY = "key_openrouter_api"
        const val KEY_GEMINI_KEY = "key_gemini_api"
        const val KEY_MODEL = "key_model_name"
        const val KEY_SYSTEM_PROMPT = "key_system_prompt"

        const val DEFAULT_MODEL = "google/gemini-2.5-pro"
        const val DEFAULT_SYSTEM_PROMPT = "You are a helpful, knowledgeable, and concise AI assistant."

        val POPULAR_MODELS = listOf(
            "google/gemini-2.5-pro",
            "google/gemini-2.5-flash",
            "openai/gpt-4o",
            "anthropic/claude-3.5-sonnet",
            "meta-llama/llama-3.3-70b-instruct",
            "deepseek/deepseek-r1"
        )
    }
}
