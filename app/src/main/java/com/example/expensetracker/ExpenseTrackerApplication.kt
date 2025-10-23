package com.example.expensetracker

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class với Hilt
 * Cần thiết để Hilt hoạt động trong toàn bộ ứng dụng
 */
@HiltAndroidApp
class ExpenseTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this)
    }
}
