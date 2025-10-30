package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.example.expensetracker.data.dao.ChatMessageDao
import com.example.expensetracker.data.database.ExpenseTrackerDatabase
import com.example.expensetracker.data.repository.ChatRepository
import com.example.expensetracker.data.repository.FirebaseRepository
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.ProfileRepository
import com.example.expensetracker.data.service.GeminiService
import com.example.expensetracker.data.service.LanguageManager
import com.example.expensetracker.data.service.ExcelExportService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideExpenseTrackerDatabase(@ApplicationContext context: Context): ExpenseTrackerDatabase {
        return Room.databaseBuilder(
            context,
            ExpenseTrackerDatabase::class.java,
            "expense_tracker_database"
        ).build()
    }
    
    @Provides
    fun provideChatMessageDao(database: ExpenseTrackerDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FirebaseRepository {
        return FirebaseRepository(firestore, auth)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return AuthRepository(auth)
    }
    
    @Provides
    @Singleton
    fun provideProfileRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ProfileRepository {
        return ProfileRepository(firestore, auth)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    
    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService {
        return GeminiService()
    }
    
    @Provides
    @Singleton
    fun provideLanguageManager(@ApplicationContext context: Context): LanguageManager {
        return LanguageManager(context)
    }
    
    @Provides
    @Singleton
    fun provideExcelExportService(@ApplicationContext context: Context): ExcelExportService {
        return ExcelExportService(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideChatRepository(
        chatMessageDao: ChatMessageDao,
        geminiService: GeminiService
    ): ChatRepository {
        return ChatRepository(chatMessageDao, geminiService)
    }
}
