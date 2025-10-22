package com.example.expensetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Date,
    val sessionId: String = "default" // Để phân biệt các phiên chat khác nhau
)
