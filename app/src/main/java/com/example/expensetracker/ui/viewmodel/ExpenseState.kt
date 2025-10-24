package com.example.expensetracker.ui.viewmodel

import com.example.expensetracker.data.entity.Expense

/**
 * Sealed class để quản lý các trạng thái của Expense operations
 */
sealed class ExpenseState {
    object Loading : ExpenseState()
    data class Success(val expenses: List<Expense>) : ExpenseState()
    data class Error(val message: String) : ExpenseState()
    object Idle : ExpenseState()
}

/**
 * Sealed class để quản lý các trạng thái của CRUD operations
 */
sealed class CrudState {
    object Loading : CrudState()
    object Success : CrudState()
    data class Error(val message: String) : CrudState()
    object Idle : CrudState()
}




