package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.entity.ChatMessageEntity
import com.example.expensetracker.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()
    
    init {
        loadChatHistory()
    }
    
    private fun loadChatHistory() {
        viewModelScope.launch {
            chatRepository.getChatMessages().collect { messages ->
                _chatMessages.value = messages
            }
        }
    }
    
    fun updateInput(input: String) {
        _currentInput.value = input
    }
    
    fun sendMessage(message: String? = null) {
        val messageToSend = message ?: _currentInput.value
        if (messageToSend.isBlank()) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val result = chatRepository.sendMessage(messageToSend)
                
                if (result.isSuccess) {
                    _currentInput.value = ""
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Có lỗi xảy ra"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Có lỗi xảy ra"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearChatHistory() {
        viewModelScope.launch {
            chatRepository.clearChatHistory()
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }
}
