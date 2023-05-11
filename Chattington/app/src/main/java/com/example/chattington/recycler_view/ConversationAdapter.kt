package com.example.chattington.recycler_view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattington.MainActivity
import com.example.chattington.R
import com.example.chattington.fragments.ChatFragment

class ConversationAdapter(private val conversationList: List<Conversation>) :
    RecyclerView.Adapter<ConversationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val chatView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return MyViewHolder(chatView)
    }

    override fun getItemCount(): Int {
        return conversationList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val conversation = conversationList[position]
        holder.bind(conversation)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val conversationTitle: TextView = itemView.findViewById(R.id.tv_ConversationTitle)

        init {
            itemView.setOnClickListener {
                val conversation = conversationList[adapterPosition]
                val fragment = ChatFragment()

                // Pass the conversation id to the chat fragment
                val bundle = Bundle()
                bundle.putLong("conversation_id", conversation.id)
                fragment.arguments = bundle

                // Start the chat fragment
                val transaction = (itemView.context as MainActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fContainer, fragment)
                transaction.addToBackStack(null)
                transaction.commit() // Commit the fragment transaction
            }
        }

        fun bind(conversation: Conversation) {
            conversationTitle.text = "Chat #${conversation.id}: ${conversation.title}"
        }
    }

    interface OnItemClickListener {
        fun onItemClick(conversation: Conversation)
    }
}
