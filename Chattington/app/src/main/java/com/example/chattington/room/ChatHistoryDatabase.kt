package com.example.chattington.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ChatHistory::class],
    version = 1
)

abstract class ChatHistoryDatabase: RoomDatabase() {
    abstract fun chatHistoryDao(): ChatHistoryDao
}