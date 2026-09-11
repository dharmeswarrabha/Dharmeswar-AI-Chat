package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppSettings
import com.example.data.local.ChatMessage
import com.example.data.local.ChatSession
import com.example.data.local.SettingsManager
import com.example.data.remote.AiChatRepository
import com.example.data.remote.AiResponseResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val chatDao = database.chatDao()
    private val settingsManager = SettingsManager(application)
    private val aiRepository = AiChatRepository()

    val settings: StateFlow<AppSettings> = settingsManager.settings

    val sessions: StateFlow<List<ChatSession>> = chatDao.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    val messages: StateFlow<List<ChatMessage>> = _currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) {
                chatDao.getMessagesForChat(sessionId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    init {
        // Initialize with existing session or create first
        viewModelScope.launch {
            sessions.collect { sessionList ->
                if (_currentSessionId.value == null && sessionList.isNotEmpty()) {
                    _currentSessionId.value = sessionList.first().id
                }
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun openSettings() {
        _showSettingsDialog.value = true
    }

    fun closeSettings() {
        _showSettingsDialog.value = false
    }

    fun updateSettings(openRouterKey: String, geminiKey: String, model: String, systemPrompt: String) {
        settingsManager.updateSettings(openRouterKey, geminiKey, model, systemPrompt)
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun createNewChat(): String {
        val newId = UUID.randomUUID().toString()
        val currentModel = settings.value.model
        viewModelScope.launch {
            val session = ChatSession(
                id = newId,
                title = "New Conversation",
                model = currentModel
            )
            chatDao.insertSession(session)
            _currentSessionId.value = newId
        }
        return newId
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatDao.deleteSessionById(sessionId)
            if (_currentSessionId.value == sessionId) {
                val remaining = sessions.value.filter { it.id != sessionId }
                _currentSessionId.value = remaining.firstOrNull()?.id
            }
        }
    }

    fun sendMessage(promptOverride: String? = null) {
        val text = (promptOverride ?: _inputText.value).trim()
        if (text.isBlank() || _isGenerating.value) return

        if (promptOverride == null) {
            _inputText.value = ""
        }

        viewModelScope.launch {
            var activeChatId = _currentSessionId.value
            if (activeChatId == null) {
                activeChatId = UUID.randomUUID().toString()
                val session = ChatSession(
                    id = activeChatId,
                    title = if (text.length > 30) text.take(30) + "..." else text,
                    model = settings.value.model
                )
                chatDao.insertSession(session)
                _currentSessionId.value = activeChatId
            }

            // Save user message to database
            val userMsg = ChatMessage(
                chatId = activeChatId,
                role = "user",
                content = text
            )
            chatDao.insertMessage(userMsg)

            // Update session title if this is the first message
            val currentMsgs = messages.value
            if (currentMsgs.isEmpty()) {
                val existingSession = chatDao.getSessionById(activeChatId)
                if (existingSession != null && existingSession.title == "New Conversation") {
                    val newTitle = if (text.length > 30) text.take(30) + "..." else text
                    chatDao.updateSession(existingSession.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
                }
            }

            _isGenerating.value = true

            // Send request to AI repository
            val history = currentMsgs + userMsg
            val currentSettings = settings.value

            val result = aiRepository.sendMessage(
                messages = history,
                openRouterKey = currentSettings.openRouterApiKey,
                geminiKey = currentSettings.geminiApiKey,
                modelName = currentSettings.model,
                systemPrompt = currentSettings.systemPrompt
            )

            when (result) {
                is AiResponseResult.Success -> {
                    val aiMsg = ChatMessage(
                        chatId = activeChatId,
                        role = "assistant",
                        content = result.text
                    )
                    chatDao.insertMessage(aiMsg)
                }
                is AiResponseResult.Error -> {
                    val errorMsg = ChatMessage(
                        chatId = activeChatId,
                        role = "error",
                        content = result.message
                    )
                    chatDao.insertMessage(errorMsg)

                    if (result.isApiKeyMissing) {
                        _showSettingsDialog.value = true
                    }
                }
            }

            _isGenerating.value = false
        }
    }
}
