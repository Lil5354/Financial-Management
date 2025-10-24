package com.example.expensetracker.ui.viewmodel

import com.google.firebase.auth.FirebaseUser

/**
 * Sealed class để quản lý trạng thái authentication
 */
sealed class AuthState {
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
    object Idle : AuthState()
}




