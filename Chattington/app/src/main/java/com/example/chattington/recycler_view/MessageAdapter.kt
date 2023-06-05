package com.example.chattington.recycler_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattington.R

class MessageAdapter(private val messageList: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val chatView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, null)
        return MyViewHolder(chatView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messageList[position]
        if (message.sentBy == Message.SENT_BY_ME) {
            holder.leftChatView.visibility = View.GONE
            holder.rightChatView.visibility = View.VISIBLE
            holder.rightTextView.text = message.message
        } else {
            holder.rightChatView.visibility = View.GONE
            holder.leftChatView.visibility = View.VISIBLE
            holder.leftTextView.text = message.message
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatView: LinearLayout = itemView.findViewById(R.id.ll_LeftChatView)
        val rightChatView: LinearLayout = itemView.findViewById(R.id.ll_RightChatView)
        val leftTextView: TextView = itemView.findViewById(R.id.tv_LeftChatText)
        val rightTextView: TextView = itemView.findViewById(R.id.tv_RightChatText)
    }
}
