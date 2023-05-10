package com.example.chattington.room

import androidx.room.*
import com.example.chattington.recycler_view.Message

@Dao
interface ChatHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatHistory(history: ChatHistory)

    @Update
    fun updateChatHistory(history: ChatHistory)

    @Delete
    fun deleteChatHistory(history: ChatHistory)

    @Query("SELECT * FROM chat_history")
    fun loadAllMessages(): MutableList<ChatHistory>
}