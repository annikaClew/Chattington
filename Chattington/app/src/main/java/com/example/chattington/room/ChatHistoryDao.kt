package com.example.chattington.room

import androidx.room.*

@Dao
interface ChatHistoryDao {
    // inserting one chat history
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatHistory(history: ChatHistory)

    // updating one chat history
    @Update
    fun updateChatHistory(history: ChatHistory)

    // deleting one chat history
    @Delete
    fun deleteChatHistory(history: ChatHistory)

    // deleting all chat history
    @Query("DELETE FROM chat_history")
    fun deleteAllChatHistory()

    // getting one chat history
    @Query("SELECT * FROM chat_history WHERE id = :id")
    fun getChatHistory(id: Long): ChatHistory

    // getting all chat history
    @Query("SELECT * FROM chat_history")
    fun getAllChatHistory(): List<ChatHistory>
}