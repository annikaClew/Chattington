package com.example.chattington.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.chattington.recycler_view.Message

@Entity(tableName = "chat_history")
@TypeConverters(Converters::class)
data class ChatHistory (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "messages") val messages: List<Message>
)