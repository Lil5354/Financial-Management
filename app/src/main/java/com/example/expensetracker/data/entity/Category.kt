package com.example.expensetracker.data.entity

import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Data class cho Category entity
 * Sử dụng với Firestore database
 */
data class Category(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val icon: String = "",
    val color: String = "#10B981",
    val isDefault: Boolean = false,
    val createdAt: Date = Date()
)




