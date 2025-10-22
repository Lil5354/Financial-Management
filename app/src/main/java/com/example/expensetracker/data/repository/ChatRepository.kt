package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.ChatMessageDao
import com.example.expensetracker.data.entity.ChatMessageEntity
import com.example.expensetracker.data.service.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val geminiService: GeminiService
) {
    
    fun getChatMessages(sessionId: String = "default"): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getChatMessagesBySession(sessionId)
    }
    
    suspend fun sendMessage(
        content: String,
        sessionId: String = "default"
    ): Result<String> {
        return try {
            // Lưu tin nhắn của người dùng
            val userMessage = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                content = content,
                isUser = true,
                timestamp = Date(),
                sessionId = sessionId
            )
            chatMessageDao.insertChatMessage(userMessage)
            
            // Lấy lịch sử chat để gửi cho AI
            val chatHistory = chatMessageDao.getChatMessagesBySession(sessionId)
                .map { messages ->
                    messages.filter { !it.isUser }
                        .zip(messages.filter { it.isUser })
                        .map { (aiMsg, userMsg) -> userMsg.content to aiMsg.content }
                }
            
            // Gọi Gemini API để tạo phản hồi
            val chatHistoryList = chatHistory.map { it.takeLast(10) } // Chỉ lấy 10 tin nhắn gần nhất
            val result = geminiService.generateResponse(content, emptyList()) // Tạm thời không dùng history
            
            if (result.isSuccess) {
                val aiResponse = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    content = result.getOrNull() ?: "Xin lỗi, tôi không thể trả lời lúc này.",
                    isUser = false,
                    timestamp = Date(),
                    sessionId = sessionId
                )
                chatMessageDao.insertChatMessage(aiResponse)
                Result.success(aiResponse.content)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearChatHistory(sessionId: String = "default") {
        chatMessageDao.deleteChatMessagesBySession(sessionId)
    }
    
    suspend fun deleteMessage(messageId: String) {
        // Tìm và xóa tin nhắn
        val messages = chatMessageDao.getRecentChatMessages(100)
        messages.collect { messageList ->
            val messageToDelete = messageList.find { it.id == messageId }
            messageToDelete?.let {
                chatMessageDao.deleteChatMessage(it)
            }
        }
    }
}
