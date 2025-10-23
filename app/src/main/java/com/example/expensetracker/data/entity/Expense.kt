package com.example.expensetracker.data.entity

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * Data class cho Expense entity
 * Sử dụng với Firestore database
 */
data class Expense(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Long = 0L,
    val category: String = "",
    val date: Date = Date(),
    val note: String = "",
    @get:PropertyName("expense")
    @set:PropertyName("expense")
    var isExpense: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
