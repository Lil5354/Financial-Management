package com.example.expensetracker.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.expensetracker.data.dao.ChatMessageDao
import com.example.expensetracker.data.entity.ChatMessageEntity
import com.example.expensetracker.data.converter.DateConverter

@Database(
    entities = [ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class ExpenseTrackerDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
}
