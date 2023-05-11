package com.example.chattington.room

import androidx.room.*

@Dao
interface ChatHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatHistory(history: ChatHistory)

    @Update
    fun updateChatHistory(history: ChatHistory)

    @Delete
    fun deleteChatHistory(history: ChatHistory)

    @Query("SELECT * FROM chat_history WHERE title = :title")
    fun getChatHistory(title: String): ChatHistory

    @Query("SELECT * FROM chat_history")
    fun getAllChatHistory(): List<ChatHistory>
}