package com.example.chattington.recycler_view

class Message(val message: String, val sentBy: String) {
    override fun toString(): String {
        return message
    }
    companion object {
        const val SENT_BY_ME = "me"
        const val SENT_BY_BOT = "bot"
    }
}