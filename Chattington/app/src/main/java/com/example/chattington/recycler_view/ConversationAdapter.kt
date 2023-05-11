package com.example.chattington.recycler_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattington.R

class ConversationAdapter(private val conversationList: List<Conversation>) :
    RecyclerView.Adapter<ConversationAdapter.MyViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

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
        holder.conversationTitle.text = conversation.title
    }

    interface OnItemClickListener {
        fun onItemClick(conversation: Conversation)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val conversationTitle: TextView = itemView.findViewById(R.id.tv_ConversationTitle)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val conversation = conversationList[position]
                    onItemClickListener?.onItemClick(conversation)
                }
            }
        }
    }


}
