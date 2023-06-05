package com.example.chattington.recycler_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattington.activities.MainActivity
import com.example.chattington.R
import com.example.chattington.fragments.ChatFragment
import com.example.chattington.room_database.ChatHistory
import com.example.chattington.room_database.ChatHistoryDatabase

class ChatAdapter(private val chatList: List<Chat>) :
    RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val chatView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return MyViewHolder(chatView)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chat = chatList[position]
        holder.bind(chat)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chatTitle: TextView = itemView.findViewById(R.id.tv_ChatTitle)
        // get a reference to the delete button
        private val deleteButton: ImageView = itemView.findViewById(R.id.iv_DeleteChatIcon)

        init {
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chat = chatList[position]

                    // initialize the room database
                    val db = ChatHistoryDatabase.getInstance(itemView.context.applicationContext)
                    val chatHistoryDao = db.chatHistoryDao()

                    // delete the chat from the database with the id
                    //chatHistoryDao.deleteChatHistory(chat.id)

                    // update the dataset by creating a new list without the deleted chat
                    chatList.toMutableList().removeAt(position)

                    // notify the adapter of the removed item
                    notifyItemRemoved(position)
                }
            }

            // when the user clicks on a chat, open the chat fragment and pass the chat id
            itemView.setOnClickListener {
                val chat = chatList[adapterPosition]
                val fragment = ChatFragment()

                // Pass the conversation id to the chat fragment
                val bundle = Bundle()
                bundle.putLong("chat_id", chat.id)
                fragment.arguments = bundle

                // Start the chat fragment
                val transaction = (itemView.context as MainActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fContainer, fragment)
                transaction.addToBackStack(null)
                transaction.commit() // Commit the fragment transaction
            }
        }

        fun bind(chat: Chat) {
            chatTitle.text = "Chat #${chatList.indexOf(chat) + 1}: ${chat.title}"
        }
    }

    interface OnItemClickListener {
        fun onItemClick(chat: Chat)
    }
}
