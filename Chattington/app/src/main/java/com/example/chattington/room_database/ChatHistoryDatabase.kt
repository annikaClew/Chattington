package com.example.chattington.room_database

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
    // abstract method to get the chat history dao
    abstract fun chatHistoryDao(): ChatHistoryDao

    // singleton instance of the database
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