package com.example.chattington.room

import androidx.room.TypeConverter
import com.example.chattington.recycler_view.Message
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type


class Converters {
    @TypeConverter
    fun fromMessageList(messages: List<Message?>?): String? {
        val gson = Gson()
        return gson.toJson(messages)
    }

    @TypeConverter
    fun toMessageList(messagesString: String?): List<Message?>? {
        val gson = Gson()
        val type: Type = object : TypeToken<List<Message?>?>() {}.type
        return gson.fromJson(messagesString, type)
    }
}