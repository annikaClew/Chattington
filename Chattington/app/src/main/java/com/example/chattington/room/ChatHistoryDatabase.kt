package com.example.chattington.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ChatHistory::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ChatHistoryDatabase: RoomDatabase() {
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: ChatHistoryDatabase? = null

        fun getInstance(context: Context): ChatHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatHistoryDatabase::class.java,
                    "chat_history_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}